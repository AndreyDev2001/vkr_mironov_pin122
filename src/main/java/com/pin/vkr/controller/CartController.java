package com.pin.vkr.controller;

import com.pin.vkr.model.Cart;
import com.pin.vkr.model.CartItem;
import com.pin.vkr.model.Product;
import com.pin.vkr.repository.OrderRepository;
import com.pin.vkr.repository.ProductRepository;
import com.pin.vkr.service.PickupPointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PickupPointService pickupPointService;

    public CartController(ProductRepository productRepository, OrderRepository orderRepository, PickupPointService pickupPointService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.pickupPointService = pickupPointService;
    }

    /**
     * Отображение содержимого корзины
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = getCartFromSession(session);
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    /**
     * Добавление товара в корзину
     * Проверяет наличие товара на складе перед добавлением
     */
    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // Проверка: хватает ли товара на складе
            if (product.getQuantity() < quantity) {
                redirectAttributes.addFlashAttribute("error",
                        "Недостаточно товара на складе. Доступно: " + product.getQuantity() + " шт.");
                return "redirect:/products/" + id;
            }

            // Получаем корзину из сессии
            Cart cart = getCartFromSession(session);

            // Создаем элемент корзины
            CartItem item = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity,
                    product.getImageUrl()
            );

            // Добавляем в корзину (если товар уже есть, количество суммируется)
            cart.addItem(item);

            // Сохраняем обновленную корзину в сессию
            session.setAttribute("cart", cart);

            redirectAttributes.addFlashAttribute("success", "Товар \"" + product.getName() + "\" добавлен в корзину");
        } else {
            redirectAttributes.addFlashAttribute("error", "Товар не найден");
        }

        return "redirect:/cart";
    }

    /**
     * Удаление товара из корзины
     */
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Cart cart = getCartFromSession(session);
        cart.removeItem(id);
        session.setAttribute("cart", cart);

        redirectAttributes.addFlashAttribute("success", "Товар удален из корзины");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String initiateCheckout(HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {
        Cart cart = getCartFromSession(session);
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Корзина пуста");
            return "redirect:/cart";
        }
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Необходимо войти");
            return "redirect:/login";
        }
        // Перенаправляем на страницу выбора пункта
        return "redirect:/cart/choose-pickup";
    }

    @GetMapping("/choose-pickup")
    public String choosePickupPage(HttpSession session, Model model, Principal principal) {
        Cart cart = getCartFromSession(session);
        if (cart.getItems().isEmpty()) return "redirect:/cart";

        model.addAttribute("cart", cart);
        model.addAttribute("pickupPoints", pickupPointService.findAll());
        return "cart/choose-pickup";
    }

    @PostMapping("/confirm-order")
    public String confirmOrder(@RequestParam Long pickupPointId,
                               HttpSession session,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            Cart cart = getCartFromSession(session);
            // Вызываем обновленный метод репозитория с передачей ID пункта
            orderRepository.createOrderAndItems(principal.getName(), cart, pickupPointId);

            cart.clear();
            session.setAttribute("cart", cart);

            redirectAttributes.addFlashAttribute("success", "Заказ успешно оформлен!");
            return "redirect:/cart/success"; // Новая страница успеха
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/cart/choose-pickup";
        }
    }

    @GetMapping("/success")
    public String successPage() {
        return "cart/success";
    }

    /**
     * Вспомогательный метод для получения корзины из сессии.
     * Если корзины нет, создает новую.
     */
    private Cart getCartFromSession(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}
