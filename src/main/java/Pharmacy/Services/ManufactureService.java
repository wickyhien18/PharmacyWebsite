package Pharmacy.Services;

import Pharmacy.Entities.Manufacturers;
import Pharmacy.Repositories.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Manufacturers update(Integer id, Manufacturers manufacturers) {
        Manufacturers manufacturers1 = manufacturerRepository.findByIdDetail(id);
        manufacturers1.setmanufacturerName(manufacturers.getmanufacturerName());
        manufacturers1.setCountry(manufacturers.getCountry());

        return manufacturerRepository.save(manufacturers1);
    }

    public void delete(Integer id) {
        manufacturerRepository.deleteById(id.longValue());
    }
}
