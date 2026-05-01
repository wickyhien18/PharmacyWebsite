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

    public Optional<Manufacturers> findById(Integer id) {
        return manufacturerRepository.findByIdDetail(id);
    }

    public Manufacturers insert(Manufacturers manufacturers) {
        return manufacturerRepository.save(manufacturers);
    }

    public Manufacturers update(Integer id, Manufacturers manufacturers) {
        Manufacturers manufacturers1 = manufacturerRepository
                .findByIdDetail(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhà sản xuất", Long.valueOf(id)));
        manufacturers1.setManufacturerName(manufacturers.getManufacturerName());
        manufacturers1.setCountry(manufacturers.getCountry());

        return manufacturerRepository.save(manufacturers1);
    }

    public void delete(Integer id) {
        manufacturerRepository.deleteById(id.longValue());
    }
}
