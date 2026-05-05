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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name="Categories API")
@RequiredArgsConstructor
public class CategoriesController {

    private CategoryService categoryService;

    @GetMapping("/")
    @Operation(summary = "List of categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAll()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "List of Categories by Slug")
    public ResponseEntity<ApiResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getBySlug(slug)));
    }

    @PostMapping("/")
    @Operation(summary = "Create Category")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Category")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Category")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Delete Successfully"));
    }
}
