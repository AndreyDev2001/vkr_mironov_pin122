package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> items = new ArrayList<>();

    // Добавить товар
    public void addItem(CartItem item) {
        for (CartItem existingItem : items) {
            if (existingItem.getProductId().equals(item.getProductId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    // Удалить товар
    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    // Очистить корзину
    public void clear() {
        items.clear();
    }

    // Получить общее количество товаров
    public int getTotalCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Получить общую сумму
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
