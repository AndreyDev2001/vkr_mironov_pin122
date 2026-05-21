package com.pin.vkr.controller;

import com.pin.vkr.model.Order;
import com.pin.vkr.repository.OrderRepository;
import com.pin.vkr.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ProfileController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder, OrderRepository orderRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        var user = userService.findByUsername(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("user", user.get());
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String phone,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        var user = userService.findByUsername(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        try {
            userService.updateProfile(user.get().getId(), email, phone, null);
            redirectAttributes.addFlashAttribute("success", "Данные обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Новые пароли не совпадают");
            return "redirect:/profile";
        }

        var user = userService.findByUsername(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        try {
            // Сервис сам проверит старый пароль и сложность нового
            userService.changePassword(user.get().getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Пароль изменён");
        } catch (RuntimeException e) {
            // Ловим ошибку из сервиса (неверный пароль или слишком простой новый)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/orders")
    public String userOrders(Principal principal, @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Long userId = userService.getUserIdByUsername(principal.getName());
        List<Order> orders;

        // Логика дат по умолчанию
        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().minusYears(1).format(DateTimeFormatter.ISO_DATE);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }

        // Вызываем НОВЫЙ метод репозитория с фильтрацией
        try {
            orders = orderRepository.findByUserIdAndPeriod(userId, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            orders = List.of(); // В случае ошибки показываем пустой список
            model.addAttribute("error", "Ошибка при загрузке заказов: " + e.getMessage());
        }

        model.addAttribute("orders", orders);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "orders";
    }
}
