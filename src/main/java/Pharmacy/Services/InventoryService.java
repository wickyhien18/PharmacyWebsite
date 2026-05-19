package Pharmacy.Services;

import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.InventoryLogRepository;
import Pharmacy.Repositories.InventoryRepository;
import Pharmacy.Repositories.MedicineRepository;
import Pharmacy.DTO.Request.ImportStockRequest;
import Pharmacy.DTO.Response.InventoryResponse;
import Pharmacy.Entities.Inventory;
import Pharmacy.Entities.InventoryLog;
import Pharmacy.Entities.Medicines;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * Class InventoryService.
 * Provides functionality and data modeling for InventoryService.
 */
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final MedicineRepository medicineRepository;

    // ================================================================
    // NHẬP KHO — Admin thêm hàng vào kho
    // ================================================================
    @Transactional
    /**
     * Import stock.
     *
     * @param req the req
     * @return the InventoryResponse result
     */
    public InventoryResponse importStock(ImportStockRequest req) {
        Medicines medicine = medicineRepository.findById(req.medicineId())
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't exist"));

        Inventory inv = inventoryRepository
                .findByMedicineId(req.medicineId())
                .orElseGet(() -> {
                    // Tạo inventory nếu chưa có (trường hợp tạo thuốc thủ công)
                    Inventory newInv = Inventory.builder()
                            .medicines(medicine).quantity(0).build();
                    return inventoryRepository.save(newInv);
                });

        int prevQty = inv.getQuantity();
        int newQty  = prevQty + req.quantity();

        inv.setQuantity(newQty);
        inv.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inv);

        // Nếu trước đó hết hàng → chuyển lại ACTIVE
        if (prevQty == 0 && medicine.getStatus() == Medicines.Status.OUT_OF_STOCK) {
            medicine.setStatus(Medicines.Status.ACTIVE);
            medicineRepository.save(medicine);
        }

        // Ghi log nhập kho
        inventoryLogRepository.save(InventoryLog.builder()
                .medicines(medicine)
                .changeType(InventoryLog.ChangeType.IMPORT)
                .quantity(req.quantity())
                .previousQuantity(prevQty)
                .newQuantity(newQty)
                .note(req.note() != null ? req.note() : "ImportStock")
                .build());

        return toResponse(inv);
    }

    // ================================================================
    // XEM TỒN KHO — theo medicineId
    // ================================================================
    @Transactional(readOnly = true)
    /**
     * Retrieves stock.
     *
     * @param medicineId the medicineId
     * @return the InventoryResponse result
     */
    public InventoryResponse getStock(Long medicineId) {
        Inventory inv = inventoryRepository
                .findByMedicineId(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found medicine in Inventory"));
        return toResponse(inv);
    }

    // ================================================================
    // LỊCH SỬ NHẬP XUẤT KHO
    // ================================================================
    @Transactional(readOnly = true)
    /**
     * Retrieves logs.
     *
     * @param medicineId the medicineId
     * @return the List<InventoryLog> result
     */
    public List<InventoryLog> getLogs(Long medicineId) {
        return inventoryLogRepository
                .findByMedicineIdOrderByCreatedAtDesc(medicineId);
    }

    /**
     * To response.
     *
     * @param inv the inv
     * @return the InventoryResponse result
     */
    private InventoryResponse toResponse(Inventory inv) {
        return new InventoryResponse(
                inv.getMedicines().getMedicineId(),
                inv.getMedicines().getMedicineName(),
                inv.getQuantity(),
                inv.getLastUpdated()
        );
    }
}