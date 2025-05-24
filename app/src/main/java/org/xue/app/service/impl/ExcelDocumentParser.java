package org.xue.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xue.app.service.DocumentParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Excel文档解析器 (支持.xls和.xlsx)
 * 将Excel数据转换为JSON格式存储
 */
@Slf4j
@Component
public class ExcelDocumentParser implements DocumentParser {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(String fileName, String mimeType) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".xls") || 
               lowerFileName.endsWith(".xlsx") ||
               "application/vnd.ms-excel".equals(mimeType) ||
               "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType);
    }

    @Override
    public String parseDocument(InputStream inputStream, String fileName) throws IOException {
        log.info("开始解析Excel文档: {}", fileName);
        
        try {
            Workbook workbook;
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            
            Map<String, Object> excelData = parseExcelToJson(workbook, fileName);
            String jsonResult = objectMapper.writeValueAsString(excelData);
            
            workbook.close();
            
            log.info("Excel文档 {} 解析完成，转换为JSON格式，长度: {}", fileName, jsonResult.length());
            return jsonResult;
            
        } catch (Exception e) {
            log.error("解析Excel文档 {} 失败: {}", fileName, e.getMessage(), e);
            throw new IOException("Excel文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将Excel工作簿转换为JSON格式
     */
    private Map<String, Object> parseExcelToJson(Workbook workbook, String fileName) {
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("type", "excel");
        result.put("sheets", new ArrayList<>());
        
        List<Map<String, Object>> sheets = (List<Map<String, Object>>) result.get("sheets");
        
        // 限制处理的工作表数量
        int sheetCount = Math.min(workbook.getNumberOfSheets(), 10);
        
        for (int i = 0; i < sheetCount; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            Map<String, Object> sheetData = parseSheet(sheet, i);
            sheets.add(sheetData);
        }
        
        return result;
    }

    /**
     * 解析单个工作表
     */
    private Map<String, Object> parseSheet(Sheet sheet, int sheetIndex) {
        Map<String, Object> sheetData = new HashMap<>();
        sheetData.put("sheetName", sheet.getSheetName());
        sheetData.put("sheetIndex", sheetIndex);
        sheetData.put("data", new ArrayList<>());
        
        List<List<Object>> data = (List<List<Object>>) sheetData.get("data");
        
        // 限制处理的行数（最多100行）
        int lastRowNum = Math.min(sheet.getLastRowNum(), 99);
        
        for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            List<Object> rowData = new ArrayList<>();
            
            // 限制处理的列数（最多20列）
            int lastCellNum = Math.min(row.getLastCellNum(), 20);
            
            for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                Object cellValue = getCellValue(cell);
                rowData.add(cellValue);
            }
            
            // 只添加非空行
            if (!isEmptyRow(rowData)) {
                data.add(rowData);
            }
        }
        
        sheetData.put("rowCount", data.size());
        return sheetData;
    }

    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，返回Long；否则返回Double
                    if (numericValue == (long) numericValue) {
                        return (long) numericValue;
                    } else {
                        return numericValue;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return "FORMULA_ERROR";
                }
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    /**
     * 检查行是否为空
     */
    private boolean isEmptyRow(List<Object> rowData) {
        return rowData.stream().allMatch(Objects::isNull);
    }

    @Override
    public String getParserType() {
        return "Excel";
    }
} 