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

    public Categories insert(Categories categories) {
        return categoryRepository.save(categories);
    }


    public void delete(Integer id) {
        categoryRepository.deleteById(id.longValue());
    }
}
