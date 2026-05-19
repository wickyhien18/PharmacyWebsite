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

// Indicates that this class is a REST controller handling HTTP requests.
@RestController
// Maps HTTP requests to the controller or handler method.
@RequestMapping("/api")
@Tag(name="Categories API")
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
/**
 * Class CategoriesController.
 * Provides functionality and data modeling for CategoriesController.
 */
public class CategoriesController {

    private final CategoryService categoryService;

    // Maps HTTP GET requests to this handler method.
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

    // Maps HTTP GET requests to this handler method.
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

    // Maps HTTP POST requests to this handler method.
    @PostMapping("/admin/categories/")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Category")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.create(request)));
    }

    // Maps HTTP PUT requests to this handler method.
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Category")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.update(id, request)));
    }

    // Maps HTTP DELETE requests to this handler method.
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
