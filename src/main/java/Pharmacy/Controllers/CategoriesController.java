package Pharmacy.Controllers;

import Pharmacy.DTO.Request.CreateCategoryRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.CategoryResponse;
import Pharmacy.Services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name="Categories API")
@RequiredArgsConstructor
/**
 * Class CategoriesController.
 * Provides functionality and data modeling for CategoriesController.
 */
public class CategoriesController {

    private final CategoryService categoryService;

    @GetMapping("/categories/")
    @Operation(summary = "List of categories")
    /**
     * Retrieves all.
     *
     * @return the ResponseEntity<ApiResponse<List<CategoryResponse>>> result
     */
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAll()));
    }

    @GetMapping("/categories/{slug}")
    @Operation(summary = "List of Categories by Slug")
    /**
     * Retrieves by slug.
     *
     * @param slug the slug
     * @return the ResponseEntity<ApiResponse<CategoryResponse>> result
     */
    public ResponseEntity<ApiResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getBySlug(slug)));
    }

    @PostMapping("/admin/categories/")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Category")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.create(request)));
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Category")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.update(id, request)));
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Category")
    /**
     * Deletes .
     *
     * @param id the id
     * @return the ResponseEntity<?> result
     */
    public ResponseEntity<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Delete Category Successfully"));
    }
}
