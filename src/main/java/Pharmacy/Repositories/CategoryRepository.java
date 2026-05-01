package Pharmacy.Repositories;

import Pharmacy.Entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Categories, Long> {

    @Query(value = "SELECT * FROM categories WHERE category_id = :id", nativeQuery = true)
    Optional<Categories> findByIdDetail(@Param("id") Integer id);
}
