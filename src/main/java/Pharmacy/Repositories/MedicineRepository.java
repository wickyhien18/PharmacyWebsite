package Pharmacy.Repositories;

import Pharmacy.Entities.Medicines;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicines,Long> {

    Optional<Medicines> findBySlugAndDeletedAtIsNull(String slug);
    boolean existsBySlug(String slug);

    // Tìm kiếm + filter — JPQL đơn giản, dễ giải thích trong phỏng vấn
    @Query("""
           SELECT m FROM Medicines m
           WHERE m.deletedAt IS NULL
             AND (:keyword IS NULL OR LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:categoryId IS NULL OR m.categories.categoryId = :categoryId)
             AND (:manufacturerId IS NULL OR m.manufacturers.manufacturerId = :manufacturerId)
             AND (:status IS NULL OR m.status = :status)
           """)
    Page<Medicines> search(
            @Param("keyword")        String keyword,
            @Param("categoryId")     Long categoryId,
            @Param("manufacturerId") Long manufacturerId,
            @Param("status")         Medicines.Status status,
            Pageable pageable);
}
