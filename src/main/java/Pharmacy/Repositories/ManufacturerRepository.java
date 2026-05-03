package Pharmacy.Repositories;

import Pharmacy.Entities.Manufacturers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturers, Long> {

    List<Manufacturers> findAllByOrderByNameAsc();
    boolean existsByName(String name);
}
