package com.pin.vkr.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Review {
    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private List<Reply> replies;
}
