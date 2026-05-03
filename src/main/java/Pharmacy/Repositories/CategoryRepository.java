package Pharmacy.Repositories;

import Pharmacy.Entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Long> {

    List<Categories> findAllByOrderByCategoryNameAsc();
    Optional<Categories> findByCategorySlug(String slug);
    boolean existsByCategorySlug(String slug);
    boolean existsByCategorySlugAndCategoryIdNot(String slug, Long id);
}
