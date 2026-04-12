package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Carts;
import com.example.Pharmacy.Repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public List<Carts> getAll() {
        return cartRepository.getAll();
    }

    public Carts insert(Carts Carts) {
        return cartRepository.save(Carts);
    }

    public Carts update(Integer id, Carts carts) {
        Carts carts1 = cartRepository.findByIdDetail(id);
        carts1.setCartItems(carts.getCartItems());
        return cartRepository.save(carts1);
    }

    public void delete(Integer id) {
        cartRepository.deleteById(id.longValue());
    }
}
