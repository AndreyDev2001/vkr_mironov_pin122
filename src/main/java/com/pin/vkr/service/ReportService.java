package com.pin.vkr.service;

import com.pin.vkr.model.ReportDTO;
import com.pin.vkr.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ReportService {
    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public ReportDTO generateReport(String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");

        ReportDTO report = new ReportDTO();
        report.setStartDate(startDateStr);
        report.setEndDate(endDateStr);
        report.setTotalRevenue(orderRepository.getRevenueByPeriod(startDate, endDate));
        report.setTotalOrders(orderRepository.getOrdersCountByPeriod(startDate, endDate));
        report.setTotalItemsSold(orderRepository.getItemsSoldByPeriod(startDate, endDate));
        report.setProductSales(orderRepository.getProductSalesByPeriod(startDate, endDate));
        report.setCategorySales(orderRepository.getCategorySalesByPeriod(startDate, endDate));

        return report;
    }
}
