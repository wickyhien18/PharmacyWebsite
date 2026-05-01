package Pharmacy.Repositories;

import Pharmacy.Entities.Manufacturers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManufacturerRepository extends JpaRepository<Manufacturers, Long> {

    @Query(value = "SELECT * FROM manufacturers WHERE manufacturer_id = :id", nativeQuery = true)
    Manufacturers findByIdDetail(@Param("id") Integer id);
}
