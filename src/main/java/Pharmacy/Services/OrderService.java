package Pharmacy.Services;

import Pharmacy.Entities.Orders;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    public List<Orders> getAll() {
        return orderRepository.findAll();
    }

    public Orders insert(Orders Orders) {
        return orderRepository.save(Orders);
    }

    public void delete(Integer id) {
        orderRepository.deleteById(id.longValue());
    }
}
