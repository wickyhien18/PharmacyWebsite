//package Pharmacy.Services;
//
//import Pharmacy.DTO.Request.CreateUpdateMedicineRequest;
//import Pharmacy.DTO.Response.MedicineResponse;
//import Pharmacy.Entities.Categories;
//import Pharmacy.Entities.Manufacturers;
//import Pharmacy.Entities.Medicines;
//import Pharmacy.Exceptions.ResourceNotFoundException;
//import Pharmacy.Repositories.CategoryRepository;
//import Pharmacy.Repositories.ManufacturerRepository;
//import Pharmacy.Repositories.MedicineRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class MedicineService {
//
//    @Autowired
//    private MedicineRepository medicineRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Autowired
//    private ManufacturerRepository manufacturerRepository;
//
//    @Transactional(readOnly = true)
//    public List<MedicineResponse> getAll() {
//        return medicineRepository
//                .findAll()
//                .stream()
//                .map(this::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    public MedicineResponse getById(@PathVariable Long id) {
//
//        Medicines medicines = medicineRepository
//                .findById(id)
//                .orElseThrow(() -> ResourceNotFoundException.of("Medicine",id));
//
//        return toResponse(medicines);
//    }
//
//    public MedicineResponse insert(CreateUpdateMedicineRequest request) {
//        Categories categories = categoryRepository
//                .findById(request.categoryId())
//                .orElseThrow(() -> ResourceNotFoundException.of("Category",request.categoryId()));
//
//        Manufacturers manufacturers =  manufacturerRepository
//                .findById(request.manufacturerId())
//                .orElseThrow(() -> ResourceNotFoundException.of("Manufacturer", request.manufacturerId()));
//
//        Medicines medicines = Medicines.builder()
//                .medicineName(request.medicinesName())
//                .categories(categories)
//                .manufacturers(manufacturers)
//                .description(request.description())
//                .price(request.price())
//                .expireDate(request.expireDate())
//                .status(request.status())
//                .build();
//
//        medicineRepository.save(medicines);
//        return toResponse(medicines);
//    }
//
//    public MedicineResponse update(Long id, CreateUpdateMedicineRequest request) {
//        Medicines medicines = medicineRepository
//                .findById(id)
//                .orElseThrow(() -> ResourceNotFoundException.of("Medicine", id));
//
//        Categories categories = categoryRepository
//                .findById(request.categoryId())
//                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId());
//
//                Manufacturers manufacturers =  manufacturerRepository
//                .findById(request.manufacturerId())
//                .orElseThrow(() -> ResourceNotFoundException.of("Manufacturer", request.manufacturerId()));
//
//
//        medicines = Medicines.builder()
//                .medicineName(request.medicinesName())
//                .categories(categories)
//                .manufacturers(manufacturers)
//                .description(request.description())
//                .price(request.price())
//                .expireDate(request.expireDate())
//                .status(request.status())
//                .build();
//        return medicineRepository.save(medicines);
//    }
//
//    public void delete(Integer id) {
//        medicineRepository.deleteById(id.longValue());
//    }
//
//    private MedicineResponse toResponse(Medicines medicines) {
//        return new MedicineResponse(medicines.getMedicineName(),
//                medicines.getCategories().getCategoryName(),
//                medicines.getManufacturers().getManufacturerName(),
//                medicines.getPrice(),
//                );
//    }
//}
