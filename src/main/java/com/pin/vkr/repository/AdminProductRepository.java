package com.pin.vkr.repository;

import com.pin.vkr.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import com.pin.vkr.model.Category;

@Repository
public class AdminProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdminProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAll() {
        String sql = """
            SELECT p.id, p.name, p.description, p.characteristics, p.price, 
                   p.quantity, p.image_url, p.category_id, c.name as category_name,
                   COALESCE(AVG(r.rating), 0) as average_rating
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            GROUP BY p.id, c.name
            ORDER BY p.id
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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
        });
    }

    public Optional<Product> findById(Long id) {
        String sql = """
            SELECT p.id, p.name, p.description, p.characteristics, p.price, 
                   p.quantity, p.image_url, p.category_id, c.name as category_name
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.id = ?
            """;
        try {
            Product product = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setCharacteristics(rs.getString("characteristics"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setImageUrl(rs.getString("image_url"));
                p.setCategoryId(rs.getLong("category_id"));
                p.setCategoryName(rs.getString("category_name"));
                return p;
            }, id);
            return Optional.of(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Product save(Product product) {
        String sql = """
            INSERT INTO products (name, description, characteristics, price, quantity, image_url, category_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getCharacteristics());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getCategoryId());
            return ps;
        }, keyHolder);
        product.setId(keyHolder.getKey().longValue());
        return product;
    }

    public Product update(Product product) {
        String sql = """
            UPDATE products 
            SET name = ?, description = ?, characteristics = ?, price = ?, 
                quantity = ?, image_url = ?, category_id = ?
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, product.getName(), product.getDescription(),
                product.getCharacteristics(), product.getPrice(),
                product.getQuantity(), product.getImageUrl(),
                product.getCategoryId(), product.getId());
        return product;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

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
}
