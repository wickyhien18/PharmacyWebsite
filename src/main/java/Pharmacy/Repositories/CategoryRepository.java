package Pharmacy.Repositories;

import Pharmacy.Entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Indicates that this class is a Data Access Object (DAO) interfacing with the database.
@Repository
/**
 * Repository interface for CategoryRepository.
 * This class is used to map data and handle basic structure.
 */
public interface CategoryRepository extends JpaRepository<Categories, Long> {

    List<Categories> findAllByOrderByCategoryNameAsc();
    Optional<Categories> findByCategorySlug(String slug);
    boolean existsByCategorySlug(String slug);
    boolean existsByCategorySlugAndCategoryIdNot(String slug, Long id);
}
