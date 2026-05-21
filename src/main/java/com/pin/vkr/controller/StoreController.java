package com.pin.vkr.controller;

import com.pin.vkr.model.Category;
import com.pin.vkr.model.Product;
import com.pin.vkr.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StoreController {
    private final ProductRepository productRepository;

    public StoreController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home(Model model ,@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String sort) {
        // Получаем список товаров с учетом категории и сортировки
        List<Product> products = productRepository.findAll(categoryId, sort);

        // Получаем список категорий для фильтра
        List<Category> categories = productRepository.findAllCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("currentSort", sort); // Передаем текущую сортировку в шаблон для подсветки кнопок
        return "store";
    }
}
