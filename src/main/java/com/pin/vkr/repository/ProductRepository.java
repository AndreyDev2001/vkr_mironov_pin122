package com.pin.vkr.repository;

import com.pin.vkr.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.pin.vkr.model.Product;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Получение всех товаров с категориями и рейтингом
    public List<Product> findAll(Long categoryId, String sortByPrice) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.name, p.description, p.characteristics, p.price, 
                   p.quantity, p.image_url, p.category_id, c.name as category_name,
                   COALESCE(AVG(r.rating), 0) as average_rating
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            WHERE 1=1
            """);

        // Добавляем фильтр по категории, если он есть
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
        }

        sql.append(" GROUP BY p.id, c.name");

        // Добавляем сортировку
        if ("asc".equalsIgnoreCase(sortByPrice)) {
            sql.append(" ORDER BY p.price ASC");
        } else if ("desc".equalsIgnoreCase(sortByPrice)) {
            sql.append(" ORDER BY p.price DESC");
        } else {
            sql.append(" ORDER BY p.id"); // По умолчанию сортировка по ID
        }

        return jdbcTemplate.query(sql.toString(), new ProductRowMapper(),
                categoryId != null ? new Object[]{categoryId} : new Object[]{});
    }

    // Получение товаров по категории
    public List<Product> findByCategory(Long categoryId) {
        String sql = """
            SELECT p.id, p.name, p.description, p.characteristics, p.price, 
                   p.quantity, p.image_url, p.category_id, c.name as category_name,
                   COALESCE(AVG(r.rating), 0) as average_rating
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            WHERE p.category_id = ?
            GROUP BY p.id, c.name
            """;
        return jdbcTemplate.query(sql, new ProductRowMapper(), categoryId);
    }

    // Получение товара по ID с полной информацией
    public Optional<Product> findById(Long id) {
        String sql = """
            SELECT p.id, p.name, p.description, p.characteristics, p.price, 
                   p.quantity, p.image_url, p.category_id, c.name as category_name,
                   COALESCE(AVG(r.rating), 0) as average_rating
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            WHERE p.id = ?
            GROUP BY p.id, c.name
            """;
        try {
            Product product = jdbcTemplate.queryForObject(sql, new ProductRowMapper(), id);
            return Optional.of(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Получение всех категорий
    public List<Category> findAllCategories() {
        String sql = "SELECT id, name, description FROM categories ORDER BY name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Category category = new Category();
            category.setId(rs.getLong("id"));
            category.setName(rs.getString("name"));
            category.setDescription(rs.getString("description"));
            return category;
        });
    }

    private static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setCharacteristics(rs.getString("characteristics"));
            product.setPrice(rs.getBigDecimal("price"));
            product.setQuantity(rs.getInt("quantity"));
            product.setImageUrl(rs.getString("image_url"));
            product.setCategoryId(rs.getLong("category_id"));
            product.setCategoryName(rs.getString("category_name"));
            product.setAverageRating(rs.getDouble("average_rating"));
            return product;
        }
    }
}
