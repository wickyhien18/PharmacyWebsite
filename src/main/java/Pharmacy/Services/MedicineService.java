package Pharmacy.Services;

import Pharmacy.Entities.Medicines;
import Pharmacy.Repositories.MedicineRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    public List<Medicines> getAll() {
        return medicineRepository.findAll();
    }

    public Medicines getById(@PathVariable Integer id) {
        return medicineRepository.findByIdDetail(id);
    }

    public List<Medicines> getByName(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return medicineRepository.findAll();
        }
        return medicineRepository.findByName(name);
    }

    public Medicines insert(Medicines medicines) {
        return medicineRepository.save(medicines);
    }

    public Medicines update(Integer id, Medicines medicines) {
        Medicines medicines1 = getById(id);
        medicines1.setMedicine_name(medicines.getMedicine_name());
        medicines1.setMedicine_image(medicines.getMedicine_image());
        medicines1.setDescription(medicines.getDescription());
        medicines1.setPrice(medicines.getPrice());
        medicines1.setQuantity(medicines.getQuantity());
        return medicineRepository.save(medicines1);
    }

    public void delete(Integer id) {
        medicineRepository.deleteById(id.longValue());
    }
}
