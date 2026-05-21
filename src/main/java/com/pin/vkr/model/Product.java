package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private String characteristics;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private Double averageRating;
    private Long categoryId;
    private String categoryName;
}
