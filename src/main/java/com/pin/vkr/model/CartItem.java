package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItem {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;

    public CartItem() {}

    public CartItem(Long productId, String name, BigDecimal price, Integer quantity, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Метод для получения общей стоимости позиции
    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(quantity));
    }
}
