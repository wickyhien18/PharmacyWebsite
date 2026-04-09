package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Manufacturers;
import com.example.Pharmacy.Repositories.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ManufactureService {
    
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    
    public List<Manufacturers> getAll() {
        return manufacturerRepository.findAll();
    }

    public Manufacturers insert(Manufacturers manufacturers) {
        return manufacturerRepository.save(manufacturers);
    }

    public Manufacturers update(Integer id, Manufacturers manufacturers) {
        Manufacturers manufacturers1 = manufacturerRepository.findByIdDetail(id);
        manufacturers1.setManufacturer_name(manufacturers.getManufacturer_name());
        manufacturers1.setCountry(manufacturers.getCountry());

        return manufacturerRepository.save(manufacturers1);
    }

    public void delete(Integer id) {
        manufacturerRepository.deleteById(id.longValue());
    }
}
