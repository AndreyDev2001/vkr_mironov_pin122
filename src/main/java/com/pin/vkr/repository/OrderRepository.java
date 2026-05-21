package com.pin.vkr.repository;

import com.pin.vkr.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Создание заказа
    public Long createOrder(Long userId, BigDecimal totalAmount) {
        String sql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'COMPLETED') RETURNING id";
        return jdbcTemplate.queryForObject(sql, Long.class, userId, totalAmount);
    }

    // Создание элемента заказа
    public void createOrderItem(Long orderId, Long productId, Integer quantity, BigDecimal price) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, orderId, productId, quantity, price);
    }

    // Получение всех заказов
    public List<Order> findAll() {
        String sql = """
            SELECT o.id, o.user_id, u.username, o.total_amount, o.status, o.created_at
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.id
            ORDER BY o.created_at DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setUserId(rs.getLong("user_id"));
            order.setUsername(rs.getString("username"));
            order.setTotalAmount(rs.getBigDecimal("total_amount"));
            order.setStatus(rs.getString("status"));
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return order;
        });
    }

    // Отчёт: выручка за период
    public BigDecimal getRevenueByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0) 
            FROM orders 
            WHERE created_at BETWEEN ? AND ? AND status = 'COMPLETED'
            """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, startDate, endDate);
    }

    // Отчёт: количество заказов за период
    public Integer getOrdersCountByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM orders 
            WHERE created_at BETWEEN ? AND ? AND status = 'COMPLETED'
            """;
        return jdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
    }

    // Отчёт: количество проданных товаров за период
    public Integer getItemsSoldByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            WHERE o.created_at BETWEEN ? AND ? AND o.status = 'COMPLETED'
            """;
        return jdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
    }

    // Отчёт: продажи по товарам за период
    public List<ProductSalesDTO> getProductSalesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT p.name as product_name, 
                   SUM(oi.quantity) as quantity_sold,
                   SUM(oi.quantity * oi.price) as revenue
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.created_at BETWEEN ? AND ? AND o.status = 'COMPLETED'
            GROUP BY p.id, p.name
            ORDER BY revenue DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductSalesDTO dto = new ProductSalesDTO();
            dto.setProductName(rs.getString("product_name"));
            dto.setQuantitySold(rs.getInt("quantity_sold"));
            dto.setRevenue(rs.getBigDecimal("revenue"));
            return dto;
        }, startDate, endDate);
    }

    // Отчёт: продажи по категориям за период
    public List<CategorySalesDTO> getCategorySalesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT c.name as category_name, 
                   SUM(oi.quantity) as quantity_sold,
                   SUM(oi.quantity * oi.price) as revenue
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.created_at BETWEEN ? AND ? AND o.status = 'COMPLETED'
            GROUP BY c.id, c.name
            ORDER BY revenue DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CategorySalesDTO dto = new CategorySalesDTO();
            dto.setCategoryName(rs.getString("category_name"));
            dto.setQuantitySold(rs.getInt("quantity_sold"));
            dto.setRevenue(rs.getBigDecimal("revenue"));
            return dto;
        }, startDate, endDate);
    }

    @Transactional
    public void createOrderAndItems(String username, Cart cart, Long pickupPointId) {
        // 1. Находим ID пользователя
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
        if (userId == null) throw new RuntimeException("Пользователь не найден");

        BigDecimal totalAmount = cart.getTotalAmount();

        // 2. Создаем заказ с указанием пункта выдачи
        String orderSql = "INSERT INTO orders (user_id, total_amount, status, pickup_point_id) VALUES (?, ?, 'COMPLETED', ?) RETURNING id";
        Long orderId = jdbcTemplate.queryForObject(orderSql, Long.class, userId, totalAmount, pickupPointId);

        // 3. Обработка товаров
        for (CartItem item : cart.getItems()) {
            // Позиции заказа
            jdbcTemplate.update("INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)",
                    orderId, item.getProductId(), item.getQuantity(), item.getPrice());

            // Списание со склада
            int rows = jdbcTemplate.update("UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?",
                    item.getQuantity(), item.getProductId(), item.getQuantity());

            if (rows == 0) throw new RuntimeException("Недостаточно товара: " + item.getName());
        }
    }

    /**
     * Получение заказов пользователя за определенный период
     */
    public List<Order> findByUserIdAndPeriod(Long userId, String startDate, String endDate) {
        String sql = """
            SELECT o.id, o.user_id, o.total_amount, o.status, o.created_at, 
                   o.pickup_point_id, pp.name as pickup_point_name
            FROM orders o
            LEFT JOIN pickup_points pp ON o.pickup_point_id = pp.id
            WHERE o.user_id = ?
              AND o.created_at >= ?::timestamp
              AND o.created_at < (?::date + INTERVAL '1 day')::timestamp
            ORDER BY o.created_at DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setUserId(rs.getLong("user_id"));
            order.setTotalAmount(rs.getBigDecimal("total_amount"));
            order.setStatus(rs.getString("status"));
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            order.setPickupPointId(rs.getLong("pickup_point_id"));
            order.setPickupPointName(rs.getString("pickup_point_name"));
            return order;
        }, userId, startDate, endDate);
    }
}
