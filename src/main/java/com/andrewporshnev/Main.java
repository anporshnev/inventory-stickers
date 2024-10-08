package com.andrewporshnev;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, DocumentException {
        List<Device> data = getData();

        if (data.isEmpty()) {
            throw new RuntimeException("Нет данных для экспорта");
        }

        exportToPdf(data);
        printNotifyMessage();
    }

    private static List<Device> getData() throws IOException {
        List<Device> data = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(new File("data.xlsx"))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                String[] targetRowItems = new String[2];
                for (int i = 0; i < 2; i++) {
                    targetRowItems[i] = formatter.formatCellValue(row.getCell(i));
                }
                if (targetRowItems[0].isBlank()) continue;
                data.add(new Device(targetRowItems[0], targetRowItems[1]));
            }
        }
        return data;
    }

    private static void exportToPdf(List<Device> data) throws DocumentException, IOException {
        Document document = new Document();
        document.setMargins(20, 20, 20, 20);

        //Используем stream для того, чтобы ресурс был доступен также при запуске jar
        InputStream is = Main.class.getResourceAsStream("/ArialRegular.ttf");

        if (is == null) {
            throw new RuntimeException("Не найден файл шрифта");
        }

        byte[] bytes = is.readAllBytes();
        BaseFont baseFont = BaseFont.createFont("ArialRegular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                true, bytes, null);
        PdfWriter.getInstance(document, new FileOutputStream("inventory-stickers.pdf"));

        document.open();
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        data.forEach(device -> table.addCell(computeCell(device, baseFont)));
        table.getDefaultCell().setBorderWidth(0);
        table.completeRow();
        document.add(table);
        document.close();
    }

    private static PdfPCell computeCell(Device device, BaseFont baseFont) {
        Font mainFont = new Font(baseFont, 14, Font.NORMAL);
        Font descriptionFont = new Font(baseFont, 9, Font.NORMAL);

        PdfPTable innerTable = new PdfPTable(1);
        PdfPCell parentCell = new PdfPCell(innerTable);
        parentCell.setBorderWidth(2);

        PdfPCell titleCell = new PdfPCell(new Phrase("Инвентарный номер", mainFont));
        titleCell.setPaddingTop(10);
        titleCell.setPaddingBottom(12);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell numberCell = new PdfPCell(new Phrase(device.getNumber(), mainFont));
        numberCell.setPaddingTop(10);
        numberCell.setPaddingBottom(12);
        numberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numberCell.setBorderWidth(1);
        numberCell.setBorderWidthBottom(2);

        PdfPCell descriptionCell = new PdfPCell(new Phrase(device.getDescription(), descriptionFont));
        descriptionCell.setPaddingBottom(3);

        innerTable.addCell(titleCell);
        innerTable.addCell(numberCell);
        innerTable.addCell(descriptionCell);

        return parentCell;
    }

    private static void printNotifyMessage() {
        String message = "Экспорт успешно завершен!";
        String sb = "-".repeat(message.length()) +
                "\n" +
                message +
                "\n" +
                "-".repeat(message.length());
        System.out.println(sb);
    }
}