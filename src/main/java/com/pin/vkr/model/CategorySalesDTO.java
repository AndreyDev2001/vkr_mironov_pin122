package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CategorySalesDTO {
    private String categoryName;
    private Integer quantitySold;
    private BigDecimal revenue;
}
