package Pharmacy.Services;

import Pharmacy.DTO.Request.CreateManufacturerRequest;
import Pharmacy.DTO.Response.ManufacturerResponse;
import Pharmacy.Entities.Manufacturers;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ConflictException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.ManufacturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Indicates that this class provides business logic and acts as a service.
@Service
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
/**
 * Class ManufacturerService.
 * Provides functionality and data modeling for ManufacturerService.
 */
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    // Defines transaction boundaries for this method/class.
    @Transactional(readOnly = true)
    /**
     * Retrieves all.
     *
     * @return the List<ManufacturerResponse> result
     */
    public List<ManufacturerResponse> getAll() {
        return manufacturerRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Creates a new Create.
     *
     * @param req the req
     * @return the ManufacturerResponse result
     */
    public ManufacturerResponse create(CreateManufacturerRequest req) {
        if (manufacturerRepository.existsByName(req.manufacturerName()))
            throw new ConflictException("Manufacturer '" + req.manufacturerName() + "' has been existed");

        Manufacturers saved = manufacturerRepository.save(
                Manufacturers.builder()
                        .manufacturerName(req.manufacturerName())
                        .country(req.country()).build());
        return toResponse(saved);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Updates an existing .
     *
     * @param id the id
     * @param req the req
     * @return the ManufacturerResponse result
     */
    public ManufacturerResponse update(Long id, CreateManufacturerRequest req) {
        Manufacturers m = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer isn't exist"));
        m.setManufacturerName(req.manufacturerName());
        m.setCountry(req.country());
        return toResponse(manufacturerRepository.save(m));
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Deletes .
     *
     * @param id the id
     */
    public void delete(Long id) {
        if (!manufacturerRepository.existsById(id))
            throw new ResourceNotFoundException("Manufacturer isn't exist");
        manufacturerRepository.deleteById(id);
    }

    /**
     * To response.
     *
     * @param m the m
     * @return the ManufacturerResponse result
     */
    public ManufacturerResponse toResponse(Manufacturers m) {
        return new ManufacturerResponse(m.getManufacturerId(), m.getManufacturerName(), m.getCountry());
    }
}
