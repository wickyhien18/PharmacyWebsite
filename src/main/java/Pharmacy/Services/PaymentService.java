package Pharmacy.Services;

import Pharmacy.Config.VNPayUtil;
import Pharmacy.DTO.Response.PaymentResponse;
import Pharmacy.Entities.Orders;
import Pharmacy.Entities.Payments;
import Pharmacy.Entities.Shipment;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.OrderRepository;
import Pharmacy.Repositories.PaymentRepository;
import Pharmacy.Repositories.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Class PaymentService.
 * Provides functionality and data modeling for PaymentService.
 */
public class PaymentService {

    private final PaymentRepository  paymentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final VNPayUtil vnPayUtil;

    // ================================================================
    // CREATE VNPAY PAYMENT URL
    // ================================================================
    /**
     * Creates a new vnpay url.
     *
     * @param orderId the orderId
     * @param clientIp the clientIp
     * @return the String result
     */
    public String createVNPayUrl(Long orderId, String clientIp) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order does not exist"));

        if (order.getPaymentStatus() == Orders.PaymentStatus.PAID)
            throw new BusinessException("This order has been paid");

        if (order.getOrderStatus() == Orders.OrderStatus.CANCELLED)
            throw new BusinessException("Order has been cancelled");

        Payments payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment information not found"));

        if (payment.getPaymentMethod() != Payments.PaymentMethod.VNPAY)
            throw new BusinessException("This order does not use VNPay payment");

        // Increase attempt_count — track how many times the user tries
        payment.setAttemptCount(payment.getAttemptCount() + 1);

        // Set expired_at: VNPay link expires after 15 minutes
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        paymentRepository.save(payment);

        long amountVND = order.getTotalPrice().longValue();
        String orderInfo = "Thanh toan don hang " + order.getOrderCode();

        return vnPayUtil.createPaymentUrl(orderId, amountVND, orderInfo, clientIp);
    }

    // ================================================================
    // HANDLING RETURN URL — VNPay redirects users back after payment
    //
    // Note: return-url is only used to display results to the user
    // DO NOT update the DB based on return-url because the user can fake it
    // Updating the DB should really be done in the IPN (below).
    // ================================================================
    @Transactional
    /**
     * Handle return.
     *
     * @param params the params
     * @return the PaymentResponse result
     */
    public PaymentResponse handleReturn(Map<String, String> params) {
        // Verify the signature first
        if (!vnPayUtil.verifyCallback(params))
            throw new BusinessException("Invalid signature");

        Long orderId = vnPayUtil.extractOrderId(params);
        if (orderId == null)
            throw new BusinessException("Order cannot be determined");

        Payments payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Only logs at return-url, does not update DB
        // DB will be updated at IPN (handleIPN below)
        boolean success = vnPayUtil.isSuccess(params);
        log.info("VNPay return: orderId={}, success={}, txn={}",
                orderId, success, params.get("vnp_TransactionNo"));

        return toResponse(payment);
    }

    // ================================================================
    // IPN PROCESSING — VNPay calls server-to-server to notify the final result
    //
    // This is the correct place to update the DB
    // IPN does not go through the browser → the user cannot intervene
    // ================================================================
    @Transactional
    /**
     * Handle ipn.
     *
     * @param params the params
     * @return the String result
     */
    public String handleIPN(Map<String, String> params) {
        // 1. Verify signature
        if (!vnPayUtil.verifyCallback(params)) {
            log.warn("VNPay IPN: invalid signature, params={}", params);
            return "{'RspCode':'97','Message':'Invalid Checksum'}";
        }

        // 2. Get orderId
        Long orderId = vnPayUtil.extractOrderId(params);
        if (orderId == null) {
            return "{'RspCode':'01','Message':'Order not found'}";
        }

        // 3. Find payment in DB
        Payments payment = paymentRepository.findByOrderOrderId(orderId).orElse(null);
        if (payment == null) {
            return "{'RspCode':'01','Message':'Order not found'}";
        }

        // 4. Check if it has been processed yet — avoid processing twice
        if (payment.getStatus() != Payments.PaymentStatus.PENDING) {
            log.info("VNPay IPN: orderId={} has been processed", orderId);
            return "{'RspCode':'02','Message':'Order already confirmed'}";
        }

        // 4. Check if the link has expired
        if (payment.getExpiredAt() != null
                && payment.getExpiredAt().isBefore(LocalDateTime.now())) {
            log.warn("VNPay IPN: payment link has expired, orderId={}", orderId);
            return "{'RspCode':'99','Message':'Payment link expired'}";
        }

        // 5. Check if the amount matches
        long expectedAmount = payment.getAmount().longValue() * 100;
        long receivedAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        if (expectedAmount != receivedAmount) {
            log.warn("VNPay IPN: amount does not match. Expected={}, Received={}",
                    expectedAmount, receivedAmount);
            return "{'RspCode':'04','Message':'Invalid Amount'}";
        }

        // 6. Save the raw callback before processing — easier to debug later
        try {
            payment.setRawCallback(new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(params));
        } catch (Exception e) {
            log.warn("Unable to serialize raw callback: {}", e.getMessage());
        }

        // 7. Update DB according to results
        Orders order = payment.getOrders();
        if (vnPayUtil.isSuccess(params)) {
            // Payment successful
            payment.setStatus(Payments.PaymentStatus.SUCCESS);
            payment.setTransactionCode(vnPayUtil.getTransactionCode(params));
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setPaymentStatus(Orders.PaymentStatus.PAID);
            order.setOrderStatus(Orders.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // Create shipment once payment has been confirmed
            if (shipmentRepository.findByOrderId(orderId).isEmpty()) {
                shipmentRepository.save(Shipment.builder()
                        .orders(order)
                        .status(Shipment.ShipmentStatus.PENDING)
                        .build());
            }

            log.info("VNPay IPN: successful payment orderId={}", orderId);
        } else {
            // Payment failed
            payment.setStatus(Payments.PaymentStatus.FAILED);
            paymentRepository.save(payment);

            order.setPaymentStatus(Orders.PaymentStatus.FAILED);
            orderRepository.save(order);

            log.info("VNPay IPN: payment failed orderId={}, code={}",
                    orderId, params.get("vnp_ResponseCode"));
        }

        // VNPay requires this exact format to be returned to know the server has received it
        return "{'RspCode':'00','Message':'Confirm Success'}";
    }

    // ================================================================
    // GET APPLICANT PAYMENT INFORMATION
    // ================================================================
    @Transactional(readOnly = true)
    /**
     * Retrieves by order id.
     *
     * @param orderId the orderId
     * @return the PaymentResponse result
     */
    public PaymentResponse getByOrderId(Long orderId) {
        Payments payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return toResponse(payment);
    }

    // ================================================================
    // MAPPER
    // ================================================================
    /**
     * To response.
     *
     * @param p the p
     * @return the PaymentResponse result
     */
    private PaymentResponse toResponse(Payments p) {
        return new PaymentResponse(
                p.getPaymentId(),
                p.getPaymentMethod().name(),
                p.getAmount(),
                p.getStatus().name(),
                p.getTransactionCode(),
                p.getPaidAt(),
                p.getExpiredAt(),
                p.getAttemptCount()
        );
    }
}
