package com.pin.vkr.service;

import com.pin.vkr.model.Product;
import com.pin.vkr.repository.AdminProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.pin.vkr.model.Category;

@Service
public class AdminProductService {
    private final AdminProductRepository productRepository;

    public AdminProductService(AdminProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product save(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Product product) {
        validateProduct(product);
        return productRepository.update(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public List<Category> findAllCategories() {
        return productRepository.findAllCategories();
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new RuntimeException("Название товара обязательно");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Цена должна быть больше 0");
        }
        if (product.getQuantity() == null || product.getQuantity() < 0) {
            throw new RuntimeException("Количество не может быть отрицательным");
        }
    }
}
