package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private Long pickupPointId;
    private String pickupPointName;
}
