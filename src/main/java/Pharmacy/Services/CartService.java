package Pharmacy.Services;

import Pharmacy.Entities.Carts;
import Pharmacy.Repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public List<Carts> getAll() {
        return cartRepository.findAll();
    }

    public Carts insert(Carts Carts) {
        return cartRepository.save(Carts);
    }


    public void delete(Integer id) {
        cartRepository.deleteById(id.longValue());
    }
}
