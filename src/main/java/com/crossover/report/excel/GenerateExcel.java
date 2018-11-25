package com.crossover.report.excel;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class GenerateExcel {
    XSSFWorkbook workbook;

    int columnCount = 0;

    int rowCount = 0;

    @PostConstruct
    public void init() {
        workbook = new XSSFWorkbook();
    }

    public void print(List<Map<String, Object>> bookData, String sheetName) {

        XSSFSheet sheet = getSheets(sheetName);
        if(bookData.size() > 0) {
            rowCount = 0;
            columnCount = 0;
            final Row headerRow = sheet.createRow(++rowCount);
            bookData.get(0).keySet().forEach(key -> {
                Cell cell = headerRow.createCell(++columnCount);
                cell.setCellValue(key);
            });
            for (Map<String, Object> aBook : bookData) {
                Row row = sheet.createRow(++rowCount);
                columnCount = 0;
                aBook.forEach((key, value) -> {
                    Cell cell = row.createCell(++columnCount);
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).intValue());
                    } else {
                         cell.setCellValue(value.toString());
                    }
                });

            }
        }
    }

    public void print(String sheetName, List<String> lists, int row) {
        XSSFSheet sheet = getSheets(sheetName);
        DataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
        CellRangeAddressList addressList = new  CellRangeAddressList(row,row,0,0);

        DataValidationConstraint constraint =validationHelper.createFormulaListConstraint("$A$5:$A$"+(5+lists.size()+1)) ;
        /*(validationHelper.createExplicitListConstraint(lists.stream()
                .toArray(String[]::new));    */
        DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(dataValidation);
    }

    private XSSFSheet getSheets(String sheetName) {
        XSSFSheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        return sheet;
    }

    public void createDrawing(ByteArrayOutputStream chartOut, String sheetName, int height, int width) {
        createDrawing(chartOut, sheetName, height, width, 5);
    }
    public void createDrawing(ByteArrayOutputStream chartOut, String sheetName, int height, int width, int row, String name){
        XSSFSheet sheet = getSheets(sheetName);
        Row myRow = sheet.createRow(row*25);
        myRow.createCell(0).setCellValue(name);
        createDrawing(chartOut, sheetName, height, width, row*25);
        /*XSSFName name2 = workbook.createName();
        name2.setNameName(name);
        name2.setRefersToFormula("'ValidationData'!$E$"+(row+1));  */
    }

    public void createDrawing(ByteArrayOutputStream chartOut, String sheetName, int height, int width, int row) {
        int my_picture_id = workbook.addPicture(chartOut.toByteArray(), Workbook.PICTURE_TYPE_JPEG);

        XSSFSheet sheet = getSheets(sheetName);

        /* Create the drawing container */
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        /* Create an anchor point */
        final CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor my_anchor = helper.createClientAnchor();
        my_anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        /* Define top left corner, and we can resize picture suitable from there */
        my_anchor.setCol1(4);
        my_anchor.setRow1(row);
        my_anchor.setRow2(row);
        my_anchor.setCol2(6);

        my_anchor.setDx2(Units.toEMU(width)); // dx = left + wanted width
        my_anchor.setDy2(Units.toEMU(height)); // dy= top + wanted height

        /* Invoke createPicture and pass the anchor point and ID */
        XSSFPicture my_picture = drawing.createPicture(my_anchor, my_picture_id);
        /* Call resize method, which resizes the image */
        my_picture.resize();
    }

    public void writeData() {
        try (FileOutputStream outputStream = new FileOutputStream("Gemba-Walk.xlsx")) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}