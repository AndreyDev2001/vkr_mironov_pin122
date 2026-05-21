package com.pin.vkr.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReportDTO {
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalItemsSold;
    private List<ProductSalesDTO> productSales;
    private List<CategorySalesDTO> categorySales;
    private String startDate;
    private String endDate;
}
