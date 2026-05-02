package Pharmacy.Services;

import Pharmacy.DTO.Request.CreateUpdateMedicineRequest;
import Pharmacy.DTO.Response.MedicineResponse;
import Pharmacy.Entities.Categories;
import Pharmacy.Entities.Manufacturers;
import Pharmacy.Entities.Medicines;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ManufactureService manufactureService;

    @Transactional(readOnly = true)
    public List<MedicineResponse> getAll() {
        return medicineRepository
                .getAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MedicineResponse getById(@PathVariable Integer id) {

        Medicines medicines = medicineRepository
                .findByIdDetail(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", Long.valueOf(id)));

        return toResponse(medicines);
    }

    public List<Medicines> getByName(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return medicineRepository.findAll();
        }
        return medicineRepository.findByName(name);
    }

    public MedicineResponse insert(CreateUpdateMedicineRequest request) {
        Categories categories = categoryService
                .findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category",Long.valueOf(request.categoryId())));

        Manufacturers manufacturers =  manufactureService
                .findById(request.manufacturerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Manufacturer", Long.valueOf(request.manufacturerId())));

        Medicines medicines = Medicines.builder()
                .medicineName(request.medicineName())
                .categories(categories)
                .manufacturers(manufacturers)
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .build();

        medicineRepository.save(medicines);
        return toResponse(medicines);
    }

    public MedicineResponse update(Integer id, CreateUpdateMedicineRequest request) {
        Medicines medicines = medicineRepository
                .findByIdDetail(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", Long.valueOf(id)));

        Categories categories = categoryRepository
                .findByIdDetail(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", Long.valueOf(Long.valueOf(id))));


        medicines1.setMedicineName(medicines.getMedicineName());
        medicines1.setMedicineImage(medicines.getMedicineImage());
        medicines1.setDescription(medicines.getDescription());
        medicines1.setPrice(medicines.getPrice());
        medicines1.setQuantity(medicines.getQuantity());
        return medicineRepository.save(medicines1);
    }

    public void delete(Integer id) {
        medicineRepository.deleteById(id.longValue());
    }

    private MedicineResponse toResponse(Medicines medicines) {
        return new MedicineResponse(medicines.getMedicineName(),
                medicines.getCategories().getCategoryName(),
                medicines.getManufacturers().getManufacturerName(),
                medicines.getPrice(),
                medicines.getQuantity());
    }
}
