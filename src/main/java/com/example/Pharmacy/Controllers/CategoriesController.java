package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Categories;
import com.example.Pharmacy.Repositories.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name="Categories API" ,description="Quản lý loại thuốc")
public class CategoriesController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    @Operation(summary = "Lấy danh sách loại thuốc")
    public List<Categories> getAll() {
        return categoryRepository.findAll();
    }
}
