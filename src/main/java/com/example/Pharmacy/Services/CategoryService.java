package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Categories;
import com.example.Pharmacy.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Categories> getAll() {
        return categoryRepository.findAll();
    }

    public Categories insert(Categories categories) {
        return categoryRepository.save(categories);
    }

    public Categories update(Integer id, Categories categories) {
        Categories categories1 = categoryRepository.findByIdDetail(id);
        categories1.setCategory_name(categories.getCategory_name());
        categories1.setDescription(categories.getDescription());
        return categoryRepository.save(categories1);
    }

    public void delete(Integer id) {
        categoryRepository.deleteById(id.longValue());
    }
}
