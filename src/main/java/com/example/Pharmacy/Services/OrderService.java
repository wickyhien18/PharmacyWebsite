package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Orders;
import com.example.Pharmacy.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    public List<Orders> getAll() {
        return orderRepository.getAll();
    }

    public Orders insert(Orders Orders) {
        return orderRepository.save(Orders);
    }

    public Orders update(Integer id, Orders orders) {
        Orders orders1 = orderRepository.findByIdDetail(id);
        orders1.setOrderItems(orders.getOrderItems());
        orders1.setStatus(orders.getStatus());
        orders1.setTotal_price(orders.getTotal_price());
        return orderRepository.save(orders1);
    }

    public void delete(Integer id) {
        orderRepository.deleteById(id.longValue());
    }
}
