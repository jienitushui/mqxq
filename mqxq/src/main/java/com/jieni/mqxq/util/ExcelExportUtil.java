package com.jieni.mqxq.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具类
 * 
 * 基于Apache POI实现Excel文件的生成和导出功能
 * 支持自定义表头、数据行样式、多种数据类型的格式化输出
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class ExcelExportUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 导出Excel到HTTP响应流
     * 
     * @param response HTTP响应对象
     * @param fileName 文件名（不含扩展名）
     * @param sheetName 工作表名称
     * @param headers 表头列表
     * @param dataKeys 数据键列表（与表头对应）
     * @param dataList 数据列表
     */
    public static void exportToResponse(HttpServletResponse response, 
                                       String fileName, 
                                       String sheetName,
                                       List<String> headers, 
                                       List<String> dataKeys, 
                                       List<Map<String, Object>> dataList) {
        try (Workbook workbook = createWorkbook(sheetName, headers, dataKeys, dataList)) {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            
            // 写入响应流
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("Excel导出成功: {}", fileName);
        } catch (IOException e) {
            log.error("Excel导出失败: {}", fileName, e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出Excel到本地文件
     * 
     * @param filePath 文件完整路径
     * @param sheetName 工作表名称
     * @param headers 表头列表
     * @param dataKeys 数据键列表
     * @param dataList 数据列表
     * @return 文件路径
     */
    public static String exportToFile(String filePath,
                                     String sheetName,
                                     List<String> headers,
                                     List<String> dataKeys,
                                     List<Map<String, Object>> dataList) {
        try (Workbook workbook = createWorkbook(sheetName, headers, dataKeys, dataList);
             FileOutputStream fos = new FileOutputStream(filePath)) {
            
            workbook.write(fos);
            log.info("Excel文件保存成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Excel文件保存失败: {}", filePath, e);
            throw new RuntimeException("Excel文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 将本地生成的Excel写入HTTP响应并删除临时文件
     *
     * @param response HTTP响应对象
     * @param filePath 已生成的临时文件完整路径
     * @param downloadFileName 下载时显示的文件名（可不带扩展名）
     */
    public static void writeFileToResponseAndDelete(HttpServletResponse response,
                                                    String filePath,
                                                    String downloadFileName) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("导出文件不存在或已被删除");
        }

        String safeFileName = downloadFileName.endsWith(".xlsx")
                ? downloadFileName
                : downloadFileName + ".xlsx";

        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setContentLengthLong(file.length());

            String encodedName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"");

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            log.info("Excel文件写入响应成功: {}", safeFileName);
        } catch (IOException e) {
            log.error("写入响应流失败: {}", safeFileName, e);
            throw new RuntimeException("写入响应流失败: " + e.getMessage());
        } finally {
            if (file.exists() && !file.delete()) {
                log.warn("临时导出文件删除失败: {}", filePath);
            } else {
                log.info("临时导出文件已删除: {}", filePath);
            }
        }
    }

    /**
     * 创建Workbook对象
     */
    private static Workbook createWorkbook(String sheetName,
                                          List<String> headers,
                                          List<String> dataKeys,
                                          List<Map<String, Object>> dataList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            
            // 自动调整列宽
            sheet.setColumnWidth(i, 4000);
        }
        
        // 填充数据
        if (dataList != null && !dataList.isEmpty()) {
            for (int i = 0; i < dataList.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                Map<String, Object> dataMap = dataList.get(i);
                
                for (int j = 0; j < dataKeys.size(); j++) {
                    Cell cell = dataRow.createCell(j);
                    Object value = dataMap.get(dataKeys.get(j));
                    
                    if (value == null) {
                        cell.setCellValue("");
                        cell.setCellStyle(dataStyle);
                    } else if (value instanceof Date) {
                        cell.setCellValue(DATE_FORMAT.format((Date) value));
                        cell.setCellStyle(dateStyle);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                        cell.setCellStyle(dataStyle);
                    } else {
                        cell.setCellValue(value.toString());
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
        }
        
        return workbook;
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    /**
     * 创建数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置自动换行
        style.setWrapText(true);
        
        return style;
    }

    /**
     * 创建日期样式
     */
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 生成带时间戳的文件名
     * 
     * @param prefix 文件名前缀
     * @return 文件名（不含扩展名）
     */
    public static String generateFileName(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return prefix + "_" + sdf.format(new Date());
    }
}
