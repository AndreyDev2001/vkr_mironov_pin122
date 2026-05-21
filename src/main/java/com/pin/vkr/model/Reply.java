package com.pin.vkr.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reply {
    private Long id;
    private Long reviewId;
    private Long userId;
    private String username;
    private String text;
    private LocalDateTime createdAt;
}
