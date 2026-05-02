package Pharmacy.Services;

import Pharmacy.Entities.Categories;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Categories> getAll() {
        return categoryRepository.findAll();
    }

    public Categories findById(Integer id) {

        return categoryRepository
                .findByIdDetail(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", Long.valueOf(Long.valueOf(id))));

    }

    public Categories insert(Categories categories) {
        return categoryRepository.save(categories);
    }

    public Categories update(Integer id, Categories categories) {
        Categories categories1 = categoryRepository.findById(id);
        categories1.setCategoryName(categories.getCategoryName());
        categories1.setDescription(categories.getDescription());
        return categoryRepository.save(categories1);
    }

    public void delete(Integer id) {
        categoryRepository.deleteById(id.longValue());
    }
}
