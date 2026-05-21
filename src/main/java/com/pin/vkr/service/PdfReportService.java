package com.pin.vkr.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.pin.vkr.model.CategorySalesDTO;
import com.pin.vkr.model.ProductSalesDTO;
import com.pin.vkr.model.ReportDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class PdfReportService {
    private BaseFont baseFont;

    public PdfReportService() {
        try {
            InputStream fontStream = new ClassPathResource("fonts/OpenSans-Regular.ttf").getInputStream();
            byte[] fontBytes = fontStream.readAllBytes();

            baseFont = BaseFont.createFont(
                    "OpenSans-Regular.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontBytes,
                    null,
                    false
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации шрифта: " + e.getMessage());
        }
    }

    public byte[] generateReportPdf(ReportDTO report) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Заголовок
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Paragraph title = new Paragraph("Отчёт по продажам", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Период
            Font periodFont = new Font(baseFont, 12, Font.NORMAL);
            Paragraph period = new Paragraph(
                    "Период: " + report.getStartDate() + " - " + report.getEndDate(),
                    periodFont
            );
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(30);
            document.add(period);

            // Основные показатели
            Font summaryFont = new Font(baseFont, 14, Font.BOLD);
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10);
            summaryTable.setSpacingAfter(30);

            addSummaryCell(summaryTable, "Выручка",
                    report.getTotalRevenue().toString() + " ₽", summaryFont);
            addSummaryCell(summaryTable, "Заказы",
                    report.getTotalOrders().toString(), summaryFont);
            addSummaryCell(summaryTable, "Продано товаров",
                    report.getTotalItemsSold().toString(), summaryFont);

            document.add(summaryTable);

            // Продажи по товарам
            if (report.getProductSales() != null && !report.getProductSales().isEmpty()) {
                Font sectionFont = new Font(baseFont, 14, Font.BOLD);
                Paragraph productsTitle = new Paragraph("Продажи по товарам", sectionFont);
                productsTitle.setSpacingAfter(10);
                document.add(productsTitle);

                PdfPTable productsTable = new PdfPTable(3);
                productsTable.setWidthPercentage(100);
                productsTable.setWidths(new float[]{3, 1, 2});

                productsTable.addCell(createHeaderCell("Товар"));
                productsTable.addCell(createHeaderCell("Кол-во"));
                productsTable.addCell(createHeaderCell("Выручка"));

                for (ProductSalesDTO product : report.getProductSales()) {
                    productsTable.addCell(new PdfPCell(new Phrase(product.getProductName(), new Font(baseFont, 10, Font.NORMAL))));
                    productsTable.addCell(new PdfPCell(new Phrase(product.getQuantitySold().toString(), new Font(baseFont, 10, Font.NORMAL))));
                    productsTable.addCell(new PdfPCell(new Phrase(product.getRevenue() + " ₽", new Font(baseFont, 10, Font.NORMAL))));
                }

                document.add(productsTable);
            }

            // Продажи по категориям
            if (report.getCategorySales() != null && !report.getCategorySales().isEmpty()) {
                Font sectionFont = new Font(baseFont, 14, Font.BOLD);
                Paragraph categoriesTitle = new Paragraph("Продажи по категориям", sectionFont);
                categoriesTitle.setSpacingBefore(20);
                categoriesTitle.setSpacingAfter(10);
                document.add(categoriesTitle);

                PdfPTable categoriesTable = new PdfPTable(3);
                categoriesTable.setWidthPercentage(100);
                categoriesTable.setWidths(new float[]{3, 1, 2});

                categoriesTable.addCell(createHeaderCell("Категория"));
                categoriesTable.addCell(createHeaderCell("Кол-во"));
                categoriesTable.addCell(createHeaderCell("Выручка"));

                for (CategorySalesDTO category : report.getCategorySales()) {
                    categoriesTable.addCell(new PdfPCell(new Phrase(category.getCategoryName(), new Font(baseFont, 10, Font.NORMAL))));
                    categoriesTable.addCell(new PdfPCell(new Phrase(category.getQuantitySold().toString(), new Font(baseFont, 10, Font.NORMAL))));
                    categoriesTable.addCell(new PdfPCell(new Phrase(category.getRevenue() + " ₽", new Font(baseFont, 10, Font.NORMAL))));
                }

                document.add(categoriesTable);
            }

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка генерации PDF: " + e.getMessage());
        }

        return outputStream.toByteArray();
    }

    private void addSummaryCell(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);

        Paragraph p = new Paragraph();
        p.add(new Phrase(label, new Font(baseFont, 10, Font.NORMAL)));
        p.add(new Phrase("\n"));
        p.add(new Phrase(value, font));

        cell.addElement(p);
        table.addCell(cell);
    }

    private PdfPCell createHeaderCell(String text) {
        Font headerFont = new Font(baseFont, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setPadding(8);
        return cell;
    }
}
