package vn.iuh.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterException;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportWriter {
    public static void exportTableToExcel(TableModel model, String filePath, String fileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(fileName);

        // Tạo style cho header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // --- Ghi header ---
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < model.getColumnCount(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(model.getColumnName(col));
            cell.setCellStyle(headerStyle);
        }

        // --- Ghi dữ liệu ---
        for (int row = 0; row < model.getRowCount(); row++) {
            Row dataRow = sheet.createRow(row + 1);
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object value = model.getValueAt(row, col);
                Cell cell = dataRow.createCell(col);
                if (value instanceof Number)
                    cell.setCellValue(((Number) value).doubleValue());
                else
                    cell.setCellValue(value != null ? value.toString() : "");
            }
        }

        // Auto-size cột
        for (int i = 0; i < model.getColumnCount(); i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
        File file = new File(filePath);
        if (file.exists()) {
            Desktop.getDesktop().open(file);
        }
    }

    public static void printPdf(String pdfPath) {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {

            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPageable(new PDFPageable(document));

            // Hiển thị giao diện máy in (Print Dialog)
            if (printerJob.printDialog()) {
                printerJob.print();  // người dùng nhấn Print
            } else {
                System.out.println("Hủy in!");
            }

        } catch (IOException | PrinterException e) {
            e.printStackTrace();
        }
    }

}
