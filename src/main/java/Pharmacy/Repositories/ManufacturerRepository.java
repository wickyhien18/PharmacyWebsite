package Pharmacy.Repositories;

import Pharmacy.Entities.Manufacturers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturers, Long> {
    @Query("SELECT m FROM Manufacturers m ORDER BY m.manufacturerName")
    List<Manufacturers> findAllByOrderByNameAsc();

    @Query("SELECT COUNT(m) > 0 FROM Manufacturers m WHERE m.manufacturerName = :manufacturerName")
    boolean existsByName(@Param("manufacturerName") String name);
}
