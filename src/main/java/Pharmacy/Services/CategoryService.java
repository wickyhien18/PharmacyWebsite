package Pharmacy.Services;

import Pharmacy.DTO.Request.CreateCategoryRequest;
import Pharmacy.DTO.Response.CategoryResponse;
import Pharmacy.Entities.Categories;
import Pharmacy.Exceptions.BusinessException;
import Pharmacy.Exceptions.ConflictException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
/**
 * Class CategoryService.
 * Provides functionality and data modeling for CategoryService.
 */
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    /**
     * Retrieves all.
     *
     * @return the List<CategoryResponse> result
     */
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAllByOrderByCategoryNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves by slug.
     *
     * @param slug the slug
     * @return the CategoryResponse result
     */
    public CategoryResponse getBySlug(String slug) {
        return categoryRepository.findByCategorySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category isn't exist: " + slug));
    }

    @Transactional
    /**
     * Creates a new Create.
     *
     * @param req the req
     * @return the CategoryResponse result
     */
    public CategoryResponse create(CreateCategoryRequest req) {
        if (categoryRepository.existsByCategorySlug(req.slug()))
            throw new ConflictException("Slug '" + req.slug() + "' has been existed");

        Categories saved = categoryRepository.save(
                Categories.builder()
                        .categoryName(req.categoryName())
                        .categorySlug(req.slug()).build());
        return toResponse(saved);
    }

    @Transactional
    /**
     * Updates an existing .
     *
     * @param id the id
     * @param req the req
     * @return the CategoryResponse result
     */
    public CategoryResponse update(Long id, CreateCategoryRequest req) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category isn't exist"));

        if (categoryRepository.existsByCategorySlugAndCategoryIdNot(req.slug(), id))
            throw new ConflictException("Slug '" + req.slug() + "' has been used");

        category.setCategoryName(req.categoryName());
        category.setCategorySlug(req.slug());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    /**
     * Deletes .
     *
     * @param id the id
     */
    public void delete(Long id) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Category isn't exist");
        categoryRepository.deleteById(id);
    }

    /**
     * To response.
     *
     * @param c the c
     * @return the CategoryResponse result
     */
    public CategoryResponse toResponse(Categories c) {
        return new CategoryResponse(c.getCategoryId(), c.getCategoryName(), c.getCategorySlug());
    }
}
