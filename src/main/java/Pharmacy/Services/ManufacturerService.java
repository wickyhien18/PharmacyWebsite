package Pharmacy.Services;

import Pharmacy.DTO.Request.CreateManufacturerRequest;
import Pharmacy.DTO.Response.ManufacturerResponse;
import Pharmacy.Entities.Manufacturers;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.ManufacturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    @Transactional(readOnly = true)
    public List<ManufacturerResponse> getAll() {
        return manufacturerRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ManufacturerResponse create(CreateManufacturerRequest req) {
        if (manufacturerRepository.existsByName(req.manufacturerName()))
            throw new BusinessException("Manufacturer '" + req.manufacturerName() + "' has been existed");

        Manufacturers saved = manufacturerRepository.save(
                Manufacturers.builder()
                        .manufacturerName(req.manufacturerName())
                        .country(req.country()).build());
        return toResponse(saved);
    }

    @Transactional
    public ManufacturerResponse update(Long id, CreateManufacturerRequest req) {
        Manufacturers m = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer isn't exist"));
        m.setManufacturerName(req.manufacturerName());
        m.setCountry(req.country());
        return toResponse(manufacturerRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        if (!manufacturerRepository.existsById(id))
            throw new ResourceNotFoundException("Manufacturer isn't exist");
        manufacturerRepository.deleteById(id);
    }

    public ManufacturerResponse toResponse(Manufacturers m) {
        return new ManufacturerResponse(m.getManufacturerId(), m.getManufacturerName(), m.getCountry());
    }
}
