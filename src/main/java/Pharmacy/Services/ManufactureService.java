package Pharmacy.Services;

import Pharmacy.Entities.Manufacturers;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ManufactureService {
    
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    
    public List<Manufacturers> getAll() {
        return manufacturerRepository.findAll();
    }

    public Manufacturers insert(Manufacturers manufacturers) {
        return manufacturerRepository.save(manufacturers);
    }


    public void delete(Integer id) {
        manufacturerRepository.deleteById(id.longValue());
    }
}
