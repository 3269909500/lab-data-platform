package com.sewage.monitor.service;

import com.sewage.monitor.entity.LabDailyStatistics;
import com.sewage.monitor.mapper.LabDailyStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报表导出服务
 * 功能：生成Excel格式的统计报表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final LabDailyStatisticsMapper dailyStatisticsMapper;

    /**
     * 生成日统计报表（Excel格式）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    public byte[] generateDailyStatisticsReport(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("📊 开始生成日统计报表 - 开始日期: {}, 结束日期: {}", startDate, endDate);

        // 1. 查询数据
        List<LabDailyStatistics> statisticsList = dailyStatisticsMapper.selectByDateRange(startDate, endDate);
        log.info("查询到 {} 条统计数据", statisticsList.size());

        // 2. 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("日统计报表");

        // 3. 设置列宽
        sheet.setColumnWidth(0, 3000);  // 日期
        sheet.setColumnWidth(1, 5000);  // 实验室名称
        sheet.setColumnWidth(2, 3000);  // 平均温度
        sheet.setColumnWidth(3, 3000);  // 平均湿度
        sheet.setColumnWidth(4, 3000);  // 平均PM2.5
        sheet.setColumnWidth(5, 3000);  // 平均CO2
        sheet.setColumnWidth(6, 3000);  // 告警次数
        sheet.setColumnWidth(7, 3000);  // 预约人数
        sheet.setColumnWidth(8, 3000);  // 签到人数
        sheet.setColumnWidth(9, 3000);  // 使用率

        // 4. 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        // 5. 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "日期", "实验室名称", "平均温度(℃)", "平均湿度(%)", "平均PM2.5(μg/m³)",
            "平均CO2(ppm)", "告警次数", "预约人数", "签到人数", "使用率(%)"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 6. 填充数据
        int rowNum = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (LabDailyStatistics stats : statisticsList) {
            Row row = sheet.createRow(rowNum++);

            // 日期
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(stats.getStatDate().format(dateFormatter));
            cell0.setCellStyle(dataStyle);

            // 实验室名称
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(stats.getLabName());
            cell1.setCellStyle(dataStyle);

            // 平均温度
            Cell cell2 = row.createCell(2);
            if (stats.getAvgTemperature() != null) {
                cell2.setCellValue(stats.getAvgTemperature().doubleValue());
            }
            cell2.setCellStyle(dataStyle);

            // 平均湿度
            Cell cell3 = row.createCell(3);
            if (stats.getAvgHumidity() != null) {
                cell3.setCellValue(stats.getAvgHumidity().doubleValue());
            }
            cell3.setCellStyle(dataStyle);

            // 平均PM2.5
            Cell cell4 = row.createCell(4);
            if (stats.getAvgPm25() != null) {
                cell4.setCellValue(stats.getAvgPm25().doubleValue());
            }
            cell4.setCellStyle(dataStyle);

            // 平均CO2
            Cell cell5 = row.createCell(5);
            if (stats.getAvgCo2() != null) {
                cell5.setCellValue(stats.getAvgCo2().doubleValue());
            }
            cell5.setCellStyle(dataStyle);

            // 告警次数
            Cell cell6 = row.createCell(6);
            if (stats.getAlarmCount() != null) {
                cell6.setCellValue(stats.getAlarmCount());
            }
            cell6.setCellStyle(dataStyle);

            // 预约人数
            Cell cell7 = row.createCell(7);
            if (stats.getReservationCount() != null) {
                cell7.setCellValue(stats.getReservationCount());
            }
            cell7.setCellStyle(dataStyle);

            // 签到人数
            Cell cell8 = row.createCell(8);
            if (stats.getAttendanceCount() != null) {
                cell8.setCellValue(stats.getAttendanceCount());
            }
            cell8.setCellStyle(dataStyle);

            // 使用率
            Cell cell9 = row.createCell(9);
            if (stats.getUsageRate() != null) {
                cell9.setCellValue(stats.getUsageRate().doubleValue());
            }
            cell9.setCellStyle(percentStyle);
        }

        // 7. 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] excelBytes = outputStream.toByteArray();
        log.info("✅ 报表生成完成，大小: {} KB", excelBytes.length / 1024);

        return excelBytes;
    }

    /**
     * 保存报表到文件系统
     *
     * @param excelBytes Excel字节数组
     * @param fileName 文件名
     * @return 文件路径
     */
    public String saveReportToFile(byte[] excelBytes, String fileName) throws IOException {
        // 确保reports目录存在
        String reportsDir = "reports";
        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        String filePath = reportsDir + File.separator + fileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(excelBytes);
        }

        log.info("📁 报表已保存到: {}", filePath);
        return filePath;
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐
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
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建百分比样式
     */
    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }
}
