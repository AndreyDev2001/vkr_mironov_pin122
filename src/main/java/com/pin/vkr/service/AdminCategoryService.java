package com.pin.vkr.service;

import com.pin.vkr.model.Category;
import com.pin.vkr.repository.AdminCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AdminCategoryService {
    private final AdminCategoryRepository categoryRepository;

    public AdminCategoryService(AdminCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category save(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Название категории обязательно");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Название категории обязательно");
        }
        return categoryRepository.update(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
