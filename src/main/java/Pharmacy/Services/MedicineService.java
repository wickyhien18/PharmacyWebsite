package Pharmacy.Services;

import Pharmacy.DTO.Request.CreateUpdateMedicineRequest;
import Pharmacy.DTO.Response.CategoryResponse;
import Pharmacy.DTO.Response.ManufacturerResponse;
import Pharmacy.DTO.Response.MedicineResponse;
import Pharmacy.Entities.Categories;
import Pharmacy.Entities.Inventory;
import Pharmacy.Entities.Manufacturers;
import Pharmacy.Entities.Medicines;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.CategoryRepository;
import Pharmacy.Repositories.InventoryRepository;
import Pharmacy.Repositories.ManufacturerRepository;
import Pharmacy.Repositories.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Indicates that this class provides business logic and acts as a service.
@Service
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
/**
 * Class MedicineService.
 * Provides functionality and data modeling for MedicineService.
 */
public class MedicineService {

    private final MedicineRepository medicineRepository;

    private final CategoryRepository categoryRepository;

    private final ManufacturerRepository manufacturerRepository;

    private final InventoryRepository inventoryRepository;

    // Defines transaction boundaries for this method/class.
    @Transactional(readOnly = true)
    public Page<MedicineResponse> search(
            String keyword,
            Long categoryId,
            Long manufacturerId,
            String status,
            Pageable pageable) {

        Medicines.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Medicines.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid Status. Use: ACTIVE, INACTIVE, OUT_OF_STOCK");
            }
        }
        return medicineRepository
                .search(keyword, categoryId, manufacturerId, statusEnum, pageable)
                .map(this::toResponse);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional(readOnly = true)
    /**
     * Retrieves by slug.
     *
     * @param slug the slug
     * @return the MedicineResponse result
     */
    public MedicineResponse getBySlug(String slug) {
        Medicines medicine = medicineRepository.findByMedicineSlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't found: " + slug));
        return toResponse(medicine);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional(readOnly = true)
    /**
     * Retrieves by id.
     *
     * @param id the id
     * @return the MedicineResponse result
     */
    public MedicineResponse getById(Long id) {
        Medicines medicine = medicineRepository.findById(id)
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine isn't found: " + id));
        return toResponse(medicine);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Creates a new Create.
     *
     * @param req the req
     * @return the MedicineResponse result
     */
    public MedicineResponse create(CreateUpdateMedicineRequest req) {

        // Automatically generate slug from name, if duplicate, add number
        String slug = resolveSlug(req.medicinesName());

        // Validate category + manufacturer exists
        Categories category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category isn't exist"));
        }

        Manufacturers manufacturer = null;
        if (req.manufacturerId() != null) {
            manufacturer = manufacturerRepository.findById(req.manufacturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manufaturer isn't exist"));
        }

        Medicines medicine = Medicines.builder()
                .medicineName(req.medicinesName())
                .medicineSlug(slug)
                .description(req.description())
                .price(req.price())
                .unit(req.unit() != null ? req.unit() : "Pillbox")
                .categories(category)
                .manufacturers(manufacturer)
                .expireDate(req.expireDate())
                .status(Medicines.Status.ACTIVE)
                .build();

        medicineRepository.save(medicine);

        // Create inventory with quantity = 0 when creating new medicine
        // Import inventory later through InventoryService
        Inventory inventory = Inventory.builder()
                .medicines(medicine)
                .quantity(0)
                .build();
        inventoryRepository.save(inventory);

        return toResponse(medicine);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Updates an existing .
     *
     * @param id the id
     * @param req the req
     * @return the MedicineResponse result
     */
    public MedicineResponse update(Long id, CreateUpdateMedicineRequest req) {
        Medicines medicine = medicineRepository.findById(id)
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Not found medicine id: " + id));

        if (req.medicinesName()        != null) medicine.setMedicineName(req.medicinesName());
        if (req.description() != null) medicine.setDescription(req.description());
        if (req.price()       != null) medicine.setPrice(req.price());
        if (req.unit()        != null) medicine.setUnit(req.unit());
        if (req.expireDate()  != null) medicine.setExpireDate(req.expireDate());

        if (req.status() != null) {
            try {
                medicine.setStatus(Medicines.Status.valueOf(req.status().toString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid Status. Use: ACTIVE, INACTIVE, OUT_OF_STOCK");
            }
        }

        if (req.categoryId() != null) {
            medicine.setCategories(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category isn't exist")));
        }

        if (req.manufacturerId() != null) {
            medicine.setManufacturers(manufacturerRepository.findById(req.manufacturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manufacturer isn't exist")));
        }

        return toResponse(medicineRepository.save(medicine));
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Deletes .
     *
     * @param id the id
     */
    public void delete(Long id) {
        Medicines medicine = medicineRepository.findById(id)
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Not found medicine id: " + id));

        medicine.setDeletedAt(java.time.LocalDateTime.now());
        medicine.setStatus(Medicines.Status.INACTIVE);
        medicineRepository.save(medicine);
    }

    /**
     * To response.
     *
     * @param m the m
     * @return the MedicineResponse result
     */
    public MedicineResponse toResponse(Medicines m) {
        // Get inventory from the inventory table
        Integer stock = inventoryRepository
                .findByMedicineId(m.getMedicineId())
                .map(Inventory::getQuantity)
                .orElse(0);

        CategoryResponse categoryRes = m.getCategories() != null
                ? new CategoryResponse(
                m.getCategories().getCategoryId(),
                m.getCategories().getCategoryName(),
                m.getCategories().getCategorySlug())
                : null;

        ManufacturerResponse manufacturerRes = m.getManufacturers() != null
                ? new ManufacturerResponse(
                m.getManufacturers().getManufacturerId(),
                m.getManufacturers().getManufacturerName(),
                m.getManufacturers().getCountry())
                : null;

        return new MedicineResponse(
                m.getMedicineId(),
                m.getMedicineName(),
                m.getMedicineSlug(),
                m.getDescription(),
                m.getPrice(),
                m.getUnit(),
                m.getStatus().name(),
                m.getExpireDate(),
                stock,
                categoryRes,
                manufacturerRes,
                m.getCreatedAt()
        );
    }

    // Convert Vietnamese to slug: "Vitamin C 1000mg" → "vitamin-c-1000mg"
    /**
     * Generate slug.
     *
     * @param input the input
     * @return the String result
     */
    private String generateSlug(String input) {
        //Split Signed character into 2 characters
        // a -> a + '
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized).replaceAll("")
                //Delete sign from signed character
                .toLowerCase()
                .replaceAll("D", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return slug;
    }

    // If the slug is the same, add the number yourself: "vitamin-c" → "vitamin-c-2" → "vitamin-c-3"
    /**
     * Resolve slug.
     *
     * @param name the name
     * @return the String result
     */
    private String resolveSlug(String name) {
        String base = generateSlug(name);
        String slug = base;
        int counter = 2;
        while (medicineRepository.existsByMedicineSlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
