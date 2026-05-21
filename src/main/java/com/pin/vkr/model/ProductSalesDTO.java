package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSalesDTO {
    private String productName;
    private Integer quantitySold;
    private BigDecimal revenue;
}
