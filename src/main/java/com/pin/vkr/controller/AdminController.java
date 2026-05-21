package com.pin.vkr.controller;

import com.pin.vkr.model.Category;
import com.pin.vkr.model.PickupPoint;
import com.pin.vkr.model.Product;
import com.pin.vkr.model.ReportDTO;
import com.pin.vkr.service.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminProductService productService;
    private final AdminCategoryService categoryService;
    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final PickupPointService pickupPointService;

    public AdminController(AdminProductService productService,
                           AdminCategoryService categoryService,
                           ReportService reportService,
                           PdfReportService pdfReportService, PickupPointService pickupPointService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
        this.pickupPointService = pickupPointService;
    }

    // Главная страница админ-панели
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", categoryService.findAll());

        // Данные для дашборда (последние 30 дней)
        String endDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE);
        ReportDTO report = reportService.generateReport(startDate, endDate);
        model.addAttribute("dashboardReport", report);

        return "admin/dashboard";
    }

    // ========== ТОВАРЫ ==========

    @GetMapping("/products")
    public String productsPage(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", productService.findAllCategories());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.findAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            if (product.getId() == null) {
                productService.save(product);
                redirectAttributes.addFlashAttribute("success", "Товар добавлен");
            } else {
                productService.update(product);
                redirectAttributes.addFlashAttribute("success", "Товар обновлён");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.findAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Товар удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ========== КАТЕГОРИИ ==========

    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (category.getId() == null) {
                categoryService.save(category);
                redirectAttributes.addFlashAttribute("success", "Категория добавлена");
            } else {
                categoryService.update(category);
                redirectAttributes.addFlashAttribute("success", "Категория обновлена");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        model.addAttribute("category", category);
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Категория удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ========== ОТЧЁТЫ ==========

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        model.addAttribute("startDate", LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("endDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        return "admin/reports";
    }

    @PostMapping("/reports/generate")
    public String generateReport(@RequestParam String startDate,
                                 @RequestParam String endDate,
                                 Model model) {
        ReportDTO report = reportService.generateReport(startDate, endDate);
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/reports";
    }

    @PostMapping("/reports/pdf")
    public ResponseEntity<ByteArrayResource> generatePdfReport(@RequestParam String startDate,
                                                               @RequestParam String endDate) {
        ReportDTO report = reportService.generateReport(startDate, endDate);
        byte[] pdfContent = pdfReportService.generateReportPdf(report);

        ByteArrayResource resource = new ByteArrayResource(pdfContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report_" + startDate + "_to_" + endDate + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfContent.length)
                .body(resource);
    }

    // ========== ПУНКТЫ ВЫДАЧИ ==========

    // Список пунктов выдачи
    @GetMapping("/pickup-points")
    public String pickupPointsPage(Model model) {
        model.addAttribute("points", pickupPointService.findAll());
        return "admin/pickup-points";
    }

    // Форма создания нового пункта
    @GetMapping("/pickup-points/new")
    public String newPickupPointForm(Model model) {
        model.addAttribute("point", new PickupPoint());
        return "admin/pickup-point-form";
    }

    // Сохранение (создание или обновление)
    @PostMapping("/pickup-points")
    public String savePickupPoint(@ModelAttribute PickupPoint point, RedirectAttributes redirectAttributes) {
        try {
            if (point.getId() == null) {
                pickupPointService.save(point);
                redirectAttributes.addFlashAttribute("success", "Пункт выдачи добавлен");
            } else {
                pickupPointService.update(point);
                redirectAttributes.addFlashAttribute("success", "Пункт выдачи обновлён");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/pickup-points";
    }

    // Форма редактирования
    @GetMapping("/pickup-points/{id}/edit")
    public String editPickupPoint(@PathVariable Long id, Model model) {
        PickupPoint point = pickupPointService.findById(id)
                .orElseThrow(() -> new RuntimeException("Пункт выдачи не найден"));
        model.addAttribute("point", point);
        return "admin/pickup-point-form";
    }

    // Удаление
    @PostMapping("/pickup-points/{id}/delete")
    public String deletePickupPoint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // В реальном проекте нужно проверить, есть ли активные заказы в этом пункте
            pickupPointService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Пункт выдачи удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/pickup-points";
    }
}
