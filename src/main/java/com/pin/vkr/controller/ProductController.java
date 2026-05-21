package com.pin.vkr.controller;

import com.pin.vkr.model.Product;
import com.pin.vkr.model.Reply;
import com.pin.vkr.model.Review;
import com.pin.vkr.repository.ProductRepository;
import com.pin.vkr.repository.ReviewRepository;
import com.pin.vkr.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/products")

public class ProductController {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    public ProductController(ProductRepository productRepository,
                             ReviewRepository reviewRepository,
                             UserService userService) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }

    // Главная страница магазина с фильтрацией по категории
    @GetMapping("/")
    public String storePage(@RequestParam(required = false) Long categoryId, Model model) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategory(categoryId);
            model.addAttribute("selectedCategory", categoryId);
        } else {
            products = productRepository.findAll(null, null);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", productRepository.findAllCategories());
        return "store";
    }

    // Страница отдельного товара
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model, Principal principal) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        List<Review> reviews = reviewRepository.findByProductId(id);

        // Загружаем ответы для каждого отзыва
        reviews.forEach(review -> {
            List<Reply> replies = reviewRepository.findByReviewId(review.getId());
            review.setReplies(replies);
        });

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("review", new Review());
        model.addAttribute("reply", new Reply());

        if (principal != null) {
            var user = userService.findByUsername(principal.getName());
            user.ifPresent(u -> model.addAttribute("currentUserId", u.getId()));
        }

        return "product-detail";
    }

    // Добавление отзыва
    @PostMapping("/{id}/review")
    public String addReview(@PathVariable Long id,
                            @ModelAttribute Review review,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Необходимо войти для оставления отзыва");
            return "redirect:/login";
        }

        var user = userService.findByUsername(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        review.setProductId(id);
        review.setUserId(user.get().getId());

        try {
            reviewRepository.save(review);
            redirectAttributes.addFlashAttribute("success", "Отзыв добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении отзыва");
        }

        return "redirect:/products/" + id;
    }

    // Добавление ответа на отзыв
    @PostMapping("/review/{reviewId}/reply")
    public String addReply(@PathVariable Long reviewId,
                           @ModelAttribute Reply reply,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        var user = userService.findByUsername(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        reply.setReviewId(reviewId);
        reply.setUserId(user.get().getId());

        try {
            reviewRepository.saveReply(reply);
            redirectAttributes.addFlashAttribute("success", "Ответ добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении ответа");
        }

        // Получаем ID товара для редиректа
        List<Review> reviews = reviewRepository.findByProductId(
                reviewRepository.findByReviewId(reviewId).isEmpty() ? 0L :
                        reviewRepository.findByReviewId(reviewId).get(0).getReviewId()
        );

        return "redirect:/products/" + getProductIdByReviewId(reviewId);
    }

    private Long getProductIdByReviewId(Long reviewId) {
        return 1L;
    }
}
