package com.alliance.diceanalytics.utility;

import com.alliance.diceanalytics.constant.ApiResponse;
import com.alliance.diceanalytics.constant.ExcelSummary;
import com.alliance.diceanalytics.exception.ServiceException;
import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.model.ReportMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonQLDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
@Component
public class ExportUtil {
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.encryptPS}")
    private String FILE_ENCRYPT_PASSWORD;

    @Value("#{'${spring.savePlus.referral.programme.daily.report.header}'.split(',')}")
    private List<String> SAVEPLUS_REFERRAL_PROGRAMME_DAILY_REPORT_HEADER;

    @Value("#{'${spring.savePlus.referral.programme.weekly.report.header}'.split(',')}")
    private List<String> SAVEPLUS_REFERRAL_PROGRAMME_WEEKLY_REPORT_HEADER;

    @Value("#{'${spring.eKYC.personal.loan.solo.cc.weekly.report.header}'.split(',')}")
    private List<String> EKYC_PERSONAL_LOAN_SOLO_CC_WEEKLY_REPORT_HEADER;

    @Value("#{'${spring.eKYC.personal.loan.solo.cc.montly.report.header}'.split(',')}")
    private List<String> EKYC_PERSONAL_LOAN_SOLO_CC_MONTLY_REPORT_HEADER;

    @Value("#{'${spring.eKYC.savePlus.new.customer.cross.sell.weekly.report.header}'.split(',')}")
    private List<String> EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_WEEKLY_REPORT_HEADER;

    @Value("#{'${spring.eKYC.savePlus.new.customer.cross.sell.monthly.report.header}'.split(',')}")
    private List<String> EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_MONTHLY_REPORT_HEADER;

    private static List<String> currencyFields = Arrays.asList("currency");

    private static List<String> percentageFields = Arrays.asList("percentage");


    //For Reports in Reporting Service (WhatsappChatbot and PersonalInfo):
    public <E> byte[] exportExcelV2(Object data, Class<E> mappedClass, String type, Locale locale)
            throws NoSuchFieldException, SecurityException, IOException, ServiceException, GeneralSecurityException {
        return exportExcelV2(data, mappedClass, type, locale, null, null, null, null);
    }

    public <E> byte[] exportExcelV2(Object data, Class<E> mappedClass, String type, Locale locale,
                                    List<String> excludedFields, List<ExcelSummary> excelSummaries, List<Date> dateRange, List<ExcelSummary> bottomSummary)
            throws IOException, NoSuchFieldException, SecurityException, ServiceException, GeneralSecurityException {

        List<E> list = (List<E>) data;

        if (list.isEmpty() || list == null) {
            log.error("export - data not found : {}", data);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_CONFLICT, "excel.export.error.data.notFound");
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Sheet 1");
        DataFormat datafrmt = workbook.createDataFormat();
        XSSFCellStyle boldStyle = workbook.createCellStyle();
        XSSFFont boldFont= workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);

        setSummary(workbook, spreadsheet, list, excelSummaries, boldStyle, datafrmt, dateRange, mappedClass);

        // data header
        int headerCellCreated = 0;
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerCellStyle.setFont(boldFont);

        for (int i = 0; i < list.get(0).getClass().getDeclaredFields().length; i++) {
            Row row = spreadsheet.getLastRowNum() >= 0 ?  spreadsheet.getRow(spreadsheet.getLastRowNum()) : spreadsheet.createRow(i);
            Field[] fields = list.get(0).getClass().getDeclaredFields();

            if (excludedFields != null && excludedFields.contains(fields[i].getName())) {
                continue;
            }

            Cell cell = row.createCell(headerCellCreated++);

            String headername = "";

            if(!type.equals("POJOHEADER")) {

                headername = messageSource.getMessage("excel.headers." + type + "." + fields[i].getName(), null,
                        locale);

            }else {

                String[] headerTitle = StringUtils.splitByCharacterTypeCamelCase(fields[i].getName());
                String header = StringUtils.join(headerTitle, " ");
                String headerCapitalize = StringUtils.capitalize(header);

                headername = messageSource.getMessage(headerCapitalize, null, locale);

            }


            cell.setCellStyle(headerCellStyle);
            cell.setCellValue(headername);
            spreadsheet.setColumnWidth(i, (headername.length() + 3) * 256);
        }

        // data items
        XSSFCellStyle currencyStyle = workbook.createCellStyle();
        XSSFCellStyle percentageStyle = workbook.createCellStyle();
        percentageStyle.setDataFormat(datafrmt.getFormat(BuiltinFormats.getBuiltinFormat(0xa)));
        currencyStyle.setDataFormat(datafrmt.getFormat("+\"RM\"#,##0.00;-\"RM\"#,##0.00"));

        list.forEach((item) -> {
            Row newRow = spreadsheet.createRow(spreadsheet.getLastRowNum() + 1);
            int cellCreated = 0;
            Field[] fields = item.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Object value;

                if (excludedFields != null && excludedFields.contains(fields[i].getName())) {
                    continue;
                }
                try {
                    fields[i].setAccessible(true);
                    value = fields[i].get(item);
                    if (value != null) {
                        if (currencyFields.contains(fields[i].getName())) {
                            Double amount = Double.parseDouble(value.toString().replaceAll("RM ", "").replaceAll(",", ""));

                            Cell cell = newRow.createCell(cellCreated++);
                            cell.setCellStyle(currencyStyle);
                            cell.setCellValue(amount);
                        }else if (percentageFields.contains(fields[i].getName())) {
                            Cell cell = newRow.createCell(cellCreated++);
                            cell.setCellStyle(percentageStyle);
                            cell.setCellValue(String.format("%s%%", value.toString()));
                        } else if (fields[i].getName().contains("customerAccountNo")||fields[i].getName().contains("leadAccountNo")){
                            newRow.createCell(cellCreated++).setCellValue("");
                        } else {
                            newRow.createCell(cellCreated++).setCellValue(value.toString());
                        }
                    } else {
                        newRow.createCell(cellCreated++).setCellValue("-");
                    }

                } catch (IllegalArgumentException ex) {
                    log.error("exportExcel - IllegalArgumentException: {}", ex);
                } catch (IllegalAccessException ex) {
                    log.error("exportExcel - IllegalAccessException: {}", ex);
                }
            }
        });

        //setSummary(workbook, spreadsheet, list, bottomSummary, boldStyle, datafrmt, dateRange, mappedClass);


        // for(int i = 0; i < headerCellCreated; i++) {
        //     try {
        //         spreadsheet.autoSizeColumn(i);
        //     }catch(Exception ex) {
        //         log.error("Spreadsheet - Column: {}, Exception: {}", i, ex);
        //     }
        // }

        // Create an output stream to hold the encrypted workbook data
        ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();

        // Create an encryption key and encrypt the workbook data
        EncryptionInfo encryptionInfo = new EncryptionInfo(EncryptionMode.agile);
        Encryptor encryptor = encryptionInfo.getEncryptor();
        encryptor.confirmPassword(FILE_ENCRYPT_PASSWORD);

        // Wrap the output stream in a POIFSFileSystem to write the encrypted data to it
        POIFSFileSystem poifs = new POIFSFileSystem();
        OutputStream encryptedOutputStream = encryptor.getDataStream(poifs);
        workbook.write(encryptedOutputStream);
        encryptedOutputStream.close();

        // Write the encrypted data to the encrypted output stream
        poifs.writeFilesystem(encryptedStream);
        poifs.close();

        // Return the encrypted workbook data as a byte array
        return encryptedStream.toByteArray();
    }

    public byte[] exportPersonalInfoUpdateExcel(Object report, Class<?> reportClass, String reportName, Locale locale, List<Date> dateRange, String[] reportOneColumnThree, String[] reportTwoColumnThree) throws IOException {
        if (report == null) {
            throw new IllegalArgumentException("Report object cannot be null");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(reportName);
            // Create title row with report information
            XSSFRow titleRow = sheet.createRow(1);
            XSSFCell titleCell = titleRow.createCell(2); // Start from column 1 to leave the first column blank
            titleCell.setCellValue("Personal Information Update");

            // Apply cell style to make title bold and big
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 20);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // set style for sub title for bold
            XSSFCellStyle subTitleStyle = workbook.createCellStyle();
            XSSFFont subTitleFont = workbook.createFont();
            subTitleFont.setBold(true);
            subTitleFont.setFontHeightInPoints((short) 11);
            subTitleStyle.setFont(subTitleFont);

            // Create duration row with report name
            XSSFRow reportNameRow = sheet.createRow(3);
            XSSFCell reportNameCell = reportNameRow.createCell(2);
            reportNameCell.setCellValue("Report ID/Name : Personal Information Update Performance Report (Weekly)");
            reportNameCell.setCellStyle(subTitleStyle);

            // Create duration row with report duration
            XSSFRow durationRow = sheet.createRow(4);
            XSSFCell durationCell = durationRow.createCell(2);
            durationCell.setCellValue("Duration: "+DateUtil.formatDateToString(dateRange.get(1))+" to "+DateUtil.formatDateToString(dateRange.get(2)));
            durationCell.setCellStyle(subTitleStyle);

            // Create run date row with report run date
            XSSFRow runDateRow = sheet.createRow(5);
            XSSFCell runDateCell = runDateRow.createCell(2);
            runDateCell.setCellValue("Report Run Date: "+ DateUtil.formatDateToString(dateRange.get(0)));
            runDateCell.setCellStyle(subTitleStyle);

            // Create header row with field names
            XSSFRow report1Row = sheet.createRow(7);
            XSSFCell report1Cell = report1Row.createCell(1);
            report1Cell.setCellValue("Report 1");
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 1, 2));
            report1Cell.setCellStyle(subTitleStyle);

            // Set Style for table border
            XSSFCellStyle tableStyle = workbook.createCellStyle();
            tableStyle.setBorderTop(BorderStyle.THIN);
            tableStyle.setBorderBottom(BorderStyle.THIN);
            tableStyle.setBorderLeft(BorderStyle.THIN);
            tableStyle.setBorderRight(BorderStyle.THIN);

            // Set Style for figure in table border
            XSSFCellStyle figureTableStyle = workbook.createCellStyle();
            figureTableStyle.setBorderTop(BorderStyle.THIN);
            figureTableStyle.setBorderBottom(BorderStyle.THIN);
            figureTableStyle.setBorderLeft(BorderStyle.THIN);
            figureTableStyle.setBorderRight(BorderStyle.THIN);
            figureTableStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Create table for report 1
            String[] reportOneColumnOne = { "1",
                    "A",
                    "B",
                    "C",
                    "D",
                    "E",
                    "F",
                    "2",
                    "A",
                    "B",
                    "C",
                    "D",
                    "E",
                    "F",
                    "3",
                    "A",
                    "i",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ii",
                    "B",
                    "i",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ii",
                    "C",
                    "i",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ii",
                    "D",
                    "i",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ii",
                    "",
                    "E",
                    "i",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ii",
                    "F",
                    "i",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ii"
            };

            String[] reportOneColumnTwo = { "Total number of targeted customers uploaded",
                    "Total number of customers targeted for push notification",
                    "Total number of customers tapped on bell inbox notification (post-login)",
                    "Total number of customers targeted for floating button (post-login)",
                    "Total number of customers targeted for logout",
                    "Total number of customers tapped on bell inbox notification (pre-login)",
                    "Total number of customers targeted for floating button (pre-login)",
                    "How many successfully submitted the personal information update requests",
                    "How many customers being targeted for push notification",
                    "How many customers tapped on bell inbox notification (post-login)",
                    "How many customers being targeted for floating button (post-login)",
                    "How many customers being targeted for logout",
                    "How many customers tapped on bell inbox notification (pre-login)",
                    "How many customers being targeted for floating button (pre-login)",
                    "How many failed to submit the personal information update requests",
                    "How many customers being targeted for push notification",
                    "How many targeted customers tapped on the push notification",
                    "a) How many targeted customers reached in-app message",
                    "i) How many targeted customers tapped on “Update Now”",
                    "ii) How many targeted customers tapped on “Don’t show me again”",
                    "iii) How many targeted customers tapped on “X”",
                    "b) How many targeted customers reached Personal Info eForm",
                    "i) How many targeted customers tapped on “Next”",
                    "ii) How many targeted customers tapped on “No change”",
                    "iii) How many targeted customers tapped on “X”",
                    "c) How many targeted customers reached Summary page",
                    "i) How many targeted customers tapped on “Submit”",
                    "ii) How many targeted customers tapped on “X”",
                    "How many targeted customers did not tap on the push notification",
                    "How many customers tapped on bell inbox notification (post-login)",
                    "How many targeted customers tapped on personal info engagement message in the bell inbox notification (post-login)",
                    "a) How many targeted customers reached in-app message ",
                    "i) How many targeted customers tapped on “Update Now”",
                    "ii) How many targeted customers tapped on “Don’t show me again”",
                    "iii) How many targeted customers tapped on “X”",
                    "b) How many targeted customers reached Personal Info eForm",
                    "i) How many targeted customers tapped on “Next”",
                    "ii) How many targeted customers tapped on “No change”",
                    "iii) How many targeted customers tapped on “X”",
                    "c) How many targeted customers reached Summary page",
                    "i) How many targeted customers tapped on “Submit”",
                    "ii) How many targeted customers tapped on “X”",
                    "How many targeted customers did not tap on personal info engagement message in the bell inbox notification (post-login)",
                    "How many customers being targeted for floating button (post-login)",
                    "How many targeted customers tapped on floating button (post-login)",
                    "a) How many targeted customers reached in-app message ",
                    "i) How many targeted customers tapped on “Update Now”",
                    "ii) How many targeted customers tapped on “Don’t show me again”",
                    "iii) How many targeted customers tapped on “X”",
                    "b) How many targeted customers reached Personal Info eForm",
                    "i) How many targeted customers tapped on “Next”",
                    "ii) How many targeted customers tapped on “No change”",
                    "iii) How many targeted customers tapped on “X”",
                    "c) How many targeted customers reached Summary page",
                    "i) How many targeted customers tapped on “Submit”",
                    "ii) How many targeted customers tapped on “X”",
                    "How many targeted customers did not tap on floating button (post-login)",
                    "How many customers being targeted for logout",
                    "How many targeted customers tapped on logout",
                    "a) How many targeted customers reached in-app message ",
                    "i) How many targeted customers tapped on “Update Now”",
                    "ii) How many targeted customers tapped on “Don’t show me again”",
                    "iii) How many targeted customers tapped on “X”",
                    "b) How many targeted customers reached Personal Info eForm",
                    "i) How many targeted customers tapped on “Next”",
                    "ii) How many targeted customers tapped on “No change”",
                    "iii) How many targeted customers tapped on “X”",
                    "c) How many targeted customers reached Summary page",
                    "i) How many targeted customers tapped on “Submit”",
                    "ii) How many targeted customers tapped on “X”",
                    "How many targeted customers did not tap on logout",
                    "How many customers tapped on logout in general",
                    "How many customers tapped on bell inbox notification (pre-login)",
                    "How many targeted customers tapped on personal info engagement message in the bell inbox notification (pre-login)",
                    "a) How many targeted customers reached in-app message ",
                    "i) How many targeted customers tapped on “Update Now”",
                    "ii) How many targeted customers tapped on “Don’t show me again”",
                    "iii) How many targeted customers tapped on “X”",
                    "b) How many targeted customers reached Personal Info eForm",
                    "i) How many targeted customers tapped on “Next”",
                    "ii) How many targeted customers tapped on “No change”",
                    "iii) How many targeted customers tapped on “X”",
                    "c) How many targeted customers reached Summary page",
                    "i) How many targeted customers tapped on “Submit”",
                    "ii) How many targeted customers tapped on “X”",
                    "d) How many targeted customers reached Login page",
                    "i) How many targeted customers successfully login",
                    "ii) How many targeted customers failed to login",
                    "How many targeted customers did not tap on personal info engagement message in the bell inbox notification (pre-login)",
                    "How many customers being targeted for floating button (pre-login)",
                    "How many targeted customers tapped on floating button (pre-login)",
                    "a) How many targeted customers reached in-app message ",
                    "i) How many targeted customers tapped on “Update Now”",
                    "ii) How many targeted customers tapped on “Don’t show me again”",
                    "iii) How many targeted customers tapped on “X”",
                    "b) How many targeted customers reached Personal Info eForm",
                    "i) How many targeted customers tapped on “Next”",
                    "ii) How many targeted customers tapped on “No change”",
                    "iii) How many targeted customers tapped on “X”",
                    "c) How many targeted customers reached Summary page",
                    "i) How many targeted customers tapped on “Submit”",
                    "ii) How many targeted customers tapped on “X”",
                    "d) How many targeted customers reached Login page",
                    "i) How many targeted customers successfully login",
                    "ii) How many targeted customers failed to login",
                    "How many targeted customers did not tap on floating button (pre-login)"
            };

            // generate report 1 table
            for (int i = 0; i < reportOneColumnOne.length; i++) {
                XSSFRow row = sheet.createRow(i+8);
                XSSFCell reportOneCellOne = row.createCell(1);
                reportOneCellOne.setCellValue(reportOneColumnOne[i]);
                reportOneCellOne.setCellStyle(tableStyle);

                XSSFCell reportOneCellTwo = row.createCell(2);
                reportOneCellTwo.setCellValue(reportOneColumnTwo[i]);
                reportOneCellTwo.setCellStyle(tableStyle);

                XSSFCell reportOneCellThree = row.createCell(3);
                reportOneCellThree.setCellValue(reportOneColumnThree[i]);
                reportOneCellThree.setCellStyle(figureTableStyle);
                //log.info("row {} : value - {}", i, reportOneColumnThree[i]);
            }

            // Create header row with field names
            XSSFRow report2Row = sheet.createRow(115);
            XSSFCell report2Cell = report2Row.createCell(1);
            report2Cell.setCellValue("Report 2");
            sheet.addMergedRegion(new CellRangeAddress(114, 114, 1, 2));
            report2Cell.setCellStyle(subTitleStyle);

            // Create table for report 2
            String[] reportTwoColumnOne = { "A",
                    "i",
                    "",
                    "",
                    "ii",
                    "",
                    ""
            };

            String[] reportTwoColumnTwo = { "How many customers targeted to update high risk personal information",
                    "How many with facial profile",
                    "a) How many successfully performed facial biometric",
                    "b) How many failed to perform facial biometric",
                    "How many without facial profile",
                    "a) How many managed to enroll facial profile",
                    "b) How many did not manage to enroll facial profile"
            };

            // generate report 2 table
            for (int i = 0; i < reportTwoColumnOne.length; i++) {
                XSSFRow row = sheet.createRow(i+116);
                XSSFCell reportTwoCellOne = row.createCell(1);
                reportTwoCellOne.setCellValue(reportTwoColumnOne[i]);
                reportTwoCellOne.setCellStyle(tableStyle);

                XSSFCell reportTwoCellTwo = row.createCell(2);
                reportTwoCellTwo.setCellValue(reportTwoColumnTwo[i]);
                reportTwoCellTwo.setCellStyle(tableStyle);

                XSSFCell reportTwoCellThree = row.createCell(3);
                reportTwoCellThree.setCellValue(reportTwoColumnThree[i]);
                reportTwoCellThree.setCellStyle(figureTableStyle);
            }

            // set style for end of report
            XSSFCellStyle reportEndStyle = workbook.createCellStyle();
            XSSFFont reportEndFont = workbook.createFont();
            reportEndFont.setBold(true);
            reportEndFont.setFontHeightInPoints((short) 11);
            reportEndStyle.setFont(reportEndFont);
            reportEndStyle.setAlignment(HorizontalAlignment.CENTER);

            // end of report
            XSSFRow reportEndRow = sheet.createRow(124);
            XSSFCell reportEndCell = reportEndRow.createCell(2);
            reportEndCell.setCellValue("END OF REPORT");
            reportEndCell.setCellStyle(reportEndStyle);


            // Auto-size the columns for better readability
            sheet.setColumnWidth(0, 256*2); // Column 0 is the first column
            sheet.setColumnWidth(1, 256*3); // Column 1 is the second column
            sheet.setColumnWidth(2, 256*106);
            sheet.setColumnWidth(3, 256*10);


            // Return the encrypted workbook data as a byte array
            return encryptFile(workbook);

        } catch (IOException e) {
            throw new IOException("Failed to generate Excel report", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while generating Excel report", e);
        }
    }


    //For Reports in AnalyticsService (SOLOCC AND EKYC):
    public <E> Workbook  generateExcelReport(Object data, Class<?> dataClass, String sheetName, Locale locale, List<Date> dateRange, String duration, Integer report) throws IOException, NoSuchFieldException, SecurityException, ServiceException {
        return exportExcelReport(data, dataClass, sheetName, locale, dateRange, duration, report);
    }

    public <E> Workbook exportExcelReport(Object data, Class<?> dataClass, String sheetName, Locale locale, List<Date> dateRange, String duration, Integer report) throws IOException, ServiceException {

        List<E> list = (List<E>) data;

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(sheetName);

            // Create title row with report information
            XSSFRow titleRow = sheet.createRow(1);
            XSSFCell titleCell = titleRow.createCell(1);
            if(report.equals(1)){
                titleCell.setCellValue(messageSource.getMessage("eKYC.referral.programme.aom.report.title", null, locale));
            }
            else if(report.equals(2)){
                titleCell.setCellValue(messageSource.getMessage("eKYC.personal.loan.solo.cc.report.title", null, locale));
            }
            else if(report.equals(3)){
                titleCell.setCellValue(messageSource.getMessage("eKYC.savePlus.new.customer.cross.sell.report.title", null, locale));
            }
            // Apply cell style to make title bold and big
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 22);
            titleFont.setUnderline(FontUnderline.SINGLE); // Add underline
            titleFont.setFontName("Calibri"); // Set font family to Calibri
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // set style for sub title for bold
            XSSFCellStyle subTitleStyle = workbook.createCellStyle();
            XSSFFont subTitleFont = workbook.createFont();
            subTitleFont.setBold(true);
            subTitleFont.setFontHeightInPoints((short) 11);
            subTitleStyle.setFont(subTitleFont);

            // Create sub title row with report name
            XSSFRow reportNameRow = sheet.createRow(3);
            XSSFCell reportNameCell = reportNameRow.createCell(1);
            if(report.equals(1)){
                if(duration.equals("monthly") || duration.equals("daily")){
                    reportNameCell.setCellValue(messageSource.getMessage("eKYC.referral.programme.aom.report.sub.title", new Object[]{duration}, locale));
                }
                else if(duration.equals("weekly")){
                    reportNameCell.setCellValue(messageSource.getMessage("eKYC.referral.programme.aom.report.sub.title.weekly", null, locale));
                }
            }
            else if(report.equals(2)){
                if(duration.equals("weekly")){
                    reportNameCell.setCellValue(messageSource.getMessage("eKYC.personal.loan.solo.cc.report.sub.title.weekly", null, locale));
                }
                else if(duration.equals("monthly")){
                    reportNameCell.setCellValue(messageSource.getMessage("eKYC.personal.loan.solo.cc.report.sub.title.montly", null, locale));
                }
            }
            else if(report.equals(3)){
                if(duration.equals("weekly")){
                    reportNameCell.setCellValue(messageSource.getMessage("eKYC.savePlus.new.customer.cross.sell.report.sub.title.weekly", null, locale));
                }
                else if(duration.equals("monthly")){
                    reportNameCell.setCellValue(messageSource.getMessage("eKYC.savePlus.new.customer.cross.sell.report.sub.title.montly", null, locale));
                }
            }
            reportNameCell.setCellStyle(subTitleStyle);

            // Create duration row with report duration
            XSSFRow durationRow = sheet.createRow(4);
            XSSFCell durationCell = durationRow.createCell(1);
            String[] durationDateTime = {DateUtil.formatDateToString(dateRange.get(1)), DateUtil.formatDateToString(dateRange.get(2))};
            // durationCell.setCellValue("Duration: "+DateUtil.formatDateToString(dateRange.get(1))+" to "+DateUtil.formatDateToString(dateRange.get(2)));
            durationCell.setCellValue(messageSource.getMessage("report.duration.datetime", durationDateTime, locale));
            durationCell.setCellStyle(subTitleStyle);

            // Create run date row with report run date
            XSSFRow runDateRow = sheet.createRow(5);
            XSSFCell runDateCell = runDateRow.createCell(1);
            String[] runDateTime = {DateUtil.formatDateToString(dateRange.get(0))};
            // runDateCell.setCellValue("Report Run Date: "+ DateUtil.formatDateToString(dateRange.get(0)));
            runDateCell.setCellValue(messageSource.getMessage("report.run.datetime", runDateTime, locale));
            runDateCell.setCellStyle(subTitleStyle);


            // set style for header
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setWrapText(true);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // Header row
            XSSFRow headerRow = sheet.createRow(7);
            if(report.equals(1)){
                if(duration.equals("monthly") || duration.equals("daily")){
                    for (int i = 0; i < SAVEPLUS_REFERRAL_PROGRAMME_DAILY_REPORT_HEADER.size(); i++) {
                        //log.info("value i = {} : {}",i,SAVEPLUS_REFERRAL_PROGRAMME_HEADER.get(i));
                        XSSFCell headerCell = headerRow.createCell(i+1);
                        sheet.setColumnWidth(i+1, 256*17);
                        headerCell.setCellValue(SAVEPLUS_REFERRAL_PROGRAMME_DAILY_REPORT_HEADER.get(i));
                        headerCell.setCellStyle(headerStyle);
                    }
                }
                else if(duration.equals("weekly")){
                    for (int i = 0; i < SAVEPLUS_REFERRAL_PROGRAMME_WEEKLY_REPORT_HEADER.size(); i++) {
                        //log.info("value i = {} : {}",i,SAVEPLUS_REFERRAL_PROGRAMME_HEADER.get(i));
                        XSSFCell headerCell = headerRow.createCell(i+1);
                        sheet.setColumnWidth(i+1, 256*17);
                        headerCell.setCellValue(SAVEPLUS_REFERRAL_PROGRAMME_WEEKLY_REPORT_HEADER.get(i));
                        headerCell.setCellStyle(headerStyle);
                    }
                }
            }
            else if(report.equals(2)){

                if(duration.equals("weekly")){
                    for (int i = 0; i < EKYC_PERSONAL_LOAN_SOLO_CC_WEEKLY_REPORT_HEADER.size(); i++) {
                        //log.info("value i = {} : {}",i,SAVEPLUS_REFERRAL_PROGRAMME_HEADER.get(i));
                        XSSFCell headerCell = headerRow.createCell(i+1);
                        sheet.setColumnWidth(i+1, 256*17);
                        headerCell.setCellValue(EKYC_PERSONAL_LOAN_SOLO_CC_WEEKLY_REPORT_HEADER.get(i));
                        headerCell.setCellStyle(headerStyle);
                    }
                }
                else if(duration.equals("monthly")){
                    for (int i = 0; i < EKYC_PERSONAL_LOAN_SOLO_CC_MONTLY_REPORT_HEADER.size(); i++) {
                        //log.info("value i = {} : {}",i,SAVEPLUS_REFERRAL_PROGRAMME_HEADER.get(i));
                        XSSFCell headerCell = headerRow.createCell(i+1);
                        sheet.setColumnWidth(i+1, 256*17);
                        headerCell.setCellValue(EKYC_PERSONAL_LOAN_SOLO_CC_MONTLY_REPORT_HEADER.get(i));
                        headerCell.setCellStyle(headerStyle);
                    }
                }
            }
            else if(report.equals(3)){
                if(duration.equals("weekly")){
                    for (int i = 0; i < EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_WEEKLY_REPORT_HEADER.size(); i++) {
                        //log.info("value i = {} : {}",i,SAVEPLUS_REFERRAL_PROGRAMME_HEADER.get(i));
                        XSSFCell headerCell = headerRow.createCell(i+1);
                        sheet.setColumnWidth(i+1, 256*17);
                        headerCell.setCellValue(EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_WEEKLY_REPORT_HEADER.get(i));
                        headerCell.setCellStyle(headerStyle);
                    }
                }
                else if(duration.equals("monthly")){
                    for (int i = 0; i < EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_MONTHLY_REPORT_HEADER.size(); i++) {
                        //log.info("value i = {} : {}",i,SAVEPLUS_REFERRAL_PROGRAMME_HEADER.get(i));
                        XSSFCell headerCell = headerRow.createCell(i+1);
                        sheet.setColumnWidth(i+1, 256*17);
                        headerCell.setCellValue(EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_MONTHLY_REPORT_HEADER.get(i));
                        headerCell.setCellStyle(headerStyle);
                    }
                }
            }
            if (list.size() > 0)
            list.forEach((item) -> {
                XSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
                int cellCreated = 1;
                Field[] fields = item.getClass().getDeclaredFields();

                for (int i = 0; i < fields.length ; i++) {
                    Object value;
                    try{
                        fields[i].setAccessible(true);
                        value = fields[i].get(item);

                        if (value == null)
                            value = "";
                        XSSFCell dataCell = dataRow.createCell(cellCreated++);
                        dataCell.setCellValue(value.toString());
                    }catch (IllegalArgumentException ex) {
                        log.error("exportExcel - IllegalArgumentException: {}", ex);
                    } catch (IllegalAccessException ex) {
                        log.error("exportExcel - IllegalAccessException: {}", ex);
                    }
                }
            });

            // Create summary row
            XSSFRow summaryRow = sheet.createRow(sheet.getLastRowNum()+2);
            XSSFCell summaryCell = summaryRow.createCell(1);
            summaryCell.setCellValue(messageSource.getMessage("report.summary", null, locale));
            sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum()+1, sheet.getLastRowNum()+1, 1, 2));

            // Create summary row with total record
            XSSFRow summaryTotalRow = sheet.createRow(sheet.getLastRowNum()+1);
            XSSFCell summaryTotalCell = summaryTotalRow.createCell(1);
            Integer[] summaryTotalNumber = {list.size()};
            summaryTotalCell.setCellValue(messageSource.getMessage("report.summary.total", summaryTotalNumber, locale));
            sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum()+1, sheet.getLastRowNum()+1, 1, 2));

            // set style for end of report
            XSSFCellStyle endReportStyle = workbook.createCellStyle();
            XSSFFont endReportFont = workbook.createFont();
            endReportFont.setBold(true);
            endReportFont.setFontHeightInPoints((short) 11);
            endReportStyle.setFont(endReportFont);
            endReportStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFRow endReportRow = sheet.createRow(sheet.getLastRowNum()+2);
            XSSFCell endReportCell = endReportRow.createCell(1);
            endReportCell.setCellValue(messageSource.getMessage("report.end", null, locale));
            endReportCell.setCellStyle(endReportStyle);

            if(report.equals(1) ){
                if(duration.equals("monthly") || duration.equals("daily")){
                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 1, SAVEPLUS_REFERRAL_PROGRAMME_DAILY_REPORT_HEADER.size()));
                }
                else if(duration.equals("weekly")){
                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 1, SAVEPLUS_REFERRAL_PROGRAMME_WEEKLY_REPORT_HEADER.size()));
                }
            }
            else if(report.equals(2)){
                if(duration.equals("weekly")){
                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 1, EKYC_PERSONAL_LOAN_SOLO_CC_WEEKLY_REPORT_HEADER.size()));
                }
                else if(duration.equals("monthly")){
                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 1, EKYC_PERSONAL_LOAN_SOLO_CC_MONTLY_REPORT_HEADER.size()));
                }
            }
            else if(report.equals(3)){
                if(duration.equals("weekly")){
                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 1, EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_WEEKLY_REPORT_HEADER.size()));
                }
                else if(duration.equals("monthly")){
                    sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 1, EKYC_SAVEPLUS_NEW_CUSTOMER_CROSS_SELL_MONTHLY_REPORT_HEADER.size()));
                }
            }

            sheet.setColumnWidth(0, 256*4);

            return workbook;

    }

    public FileForUpload inputStreamToFile(Workbook workbook, String prefix, String fileExtension, String date []) throws IOException {

        String filename = generateFileNameByDate(prefix,fileExtension,date);
        byte[] byteData =null;

        try {
            byteData =encryptFile(workbook);
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        workbook.close();

        return new FileForUpload(byteData,filename);
    }

    public FileForUpload jasperExportExcel(InputStream jrxmlFileStream, String jsonString){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFileStream);
            ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(jsonString.getBytes());
            JRDataSource ds = new JsonQLDataSource(jsonDataStream,"ReportData");


            //fill report with jrxml and data
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,excelReportConfig(), ds);
            JRXlsxExporter excelExporter = new JRXlsxExporter();

            excelExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            excelExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            excelExporter.exportReport();
        } catch (JRException e) {
            e.printStackTrace();
        }

        FileForUpload fileForUpload = new FileForUpload();
        fileForUpload.setFileData(outputStream.toByteArray());
        return fileForUpload;
    }

    //Supporting Function
    public byte[] encryptFile(Workbook workbook) throws CsvValidationException, IOException, InvalidFormatException, GeneralSecurityException {
        ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();
        //Save No Password Version first
        EncryptionInfo encryptionInfo = new EncryptionInfo(EncryptionMode.agile);
        Encryptor encryptor = encryptionInfo.getEncryptor();
        encryptor.confirmPassword(FILE_ENCRYPT_PASSWORD);

        // Wrap the output stream in a POIFSFileSystem to write the encrypted data to it
        POIFSFileSystem poifs = new POIFSFileSystem();
        OutputStream encryptedOutputStream = encryptor.getDataStream(poifs);
        workbook.write(encryptedOutputStream);
        encryptedOutputStream.close();

        // Write the encrypted data to the encrypted output stream
        poifs.writeFilesystem(encryptedStream);
        poifs.close();

        return encryptedStream.toByteArray();
    }

    public FileForUpload encryptFile(FileForUpload fileForUpload){
        XSSFWorkbook workbook= null;
        try {
            workbook = new XSSFWorkbook(new ByteArrayInputStream(fileForUpload.getFileData()));
            fileForUpload.setFileData(encryptFile(workbook));
        } catch (IOException | CsvValidationException | InvalidFormatException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return fileForUpload;
    }

    public String generateFileNameByDate(  String prefix, String fileExtension, String [] date){
        String filename = "";
        //For single date
        if(date.length ==1 )
            filename =  prefix + date[0] + fileExtension;

        //For date range
        else if(date.length ==2 )
            filename =  prefix + date[0] + "-" + date[1] + fileExtension;



        return filename;
    }

    private <E> void setSummary (XSSFWorkbook workbook, XSSFSheet spreadsheet, List<E> list, List<ExcelSummary> excelSummaries, XSSFCellStyle boldStyle, DataFormat datafrmt, List<Date> dateRange, Class<E> mappedClass) {
        // summary
        if (excelSummaries != null && !excelSummaries.isEmpty()) {
            XSSFCellStyle currencyStyle = workbook.createCellStyle();
            XSSFCellStyle currencyStyle2 = workbook.createCellStyle();
            XSSFCellStyle currencyStyle3 = workbook.createCellStyle();
            currencyStyle.setDataFormat(datafrmt.getFormat("+#,##0.00;-#,##0.00"));
            currencyStyle2.setDataFormat(datafrmt.getFormat("+\"RM\"#,##0.00;-\"RM\"#,##0.00"));
            currencyStyle3.setDataFormat(datafrmt.getFormat("\"RM\"#,##0.00"));

            excelSummaries.forEach(summaryItem -> {
                int cellCreated = 0;
                int rowNum = spreadsheet.getLastRowNum();
                Row newRow = spreadsheet.createRow(++rowNum);
                //summary item
                Cell summaryItemCell = newRow.createCell(cellCreated++);
                summaryItemCell.setCellStyle(boldStyle);
                summaryItemCell.setCellValue(summaryItem.getItemName());

                //summary value
                Iterator iterator = list.iterator();
                Cell valueCell = newRow.createCell(cellCreated++);

                switch (summaryItem) {
                    case DATE_RANGE:
                        String dateString = null;
                        if(dateRange.size() == 2){
                            dateString = String.format("%s - %s", DateUtil.formatToDateFormat(dateRange.get(0)), DateUtil.formatToDateFormat(dateRange.get(1)));
                        }else {
                            try {
                                Date now = new Date();
                                dateString = String.format("%s - %s", DateUtil.formatToDateFormat(now), DateUtil.formatToDateFormat(now));
                            }catch (Exception ex) {}
                        }
                        valueCell.setCellValue(dateString);
                        break;
                    default:
                        break;
                }

            });
            spreadsheet.createRow(spreadsheet.getLastRowNum() + 1);
            spreadsheet.createRow(spreadsheet.getLastRowNum() + 1);
        }
    }

    private Map<String,Object> excelReportConfig(){
        Map<String,Object> configParam = new HashMap<>();
        configParam.put(JRParameter.IS_IGNORE_PAGINATION,true);
        configParam.put("com.jaspersoft.jrs.export.xls.paginated",false);
        configParam.put("net.sf.jasperreports.default.font.name", "Calibri");
        configParam.put("net.sf.jasperreports.awt.ignore.missing.font","true");
        return configParam;
    }

    public InputStream getReportTemplate(String reportPath){
        Resource resource = resourceLoader.getResource("classpath:" + reportPath);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Report Template not found");
        }
        return null;
    }

//    private void loadJasperFont(){
//        SimpleJasperReportsContext jasperReportsContext = new SimpleJasperReportsContext();
//        SimpleFontFamily fontFamily = new SimpleFontFamily(jasperReportsContext);
//        fontFamily.setName("Calibri");//to be used in reports as fontName
//
//        SimpleFontFace regular = new SimpleFontFace(jasperReportsContext);
//        regular.setTtf("/fonts/Calibri/calibri-regular.ttf");
//
//        SimpleFontFace bold = new SimpleFontFace(jasperReportsContext);
//        regular.setTtf("/fonts/Calibri/calibri-bold.ttf");
//
//        SimpleFontFace boldItalic = new SimpleFontFace(jasperReportsContext);
//        regular.setTtf("/fonts/Calibri/calibri-bold.ttf");
//
//
//
//        fontFamily.setNormalFace(regular);
//        fontFamily.setBoldFace(bold);
//        fontFamily.setBoldItalicFace(boldItalic);
//        fontFamily.setItalicFace(bold);
//
//        jasperReportsContext.setExtensions(FontFamily.class, Arrays.asList(fontFamily));
//
//    }

    public FileForUpload compressFileToZip(FileForUpload fileForUpload) throws IOException {
        ByteArrayOutputStream zipByte = new ByteArrayOutputStream();
        ZipOutputStream outputStream = new ZipOutputStream(zipByte,FILE_ENCRYPT_PASSWORD.toCharArray());

        ZipParameters parameters = new ZipParameters();
        parameters.setFileNameInZip(fileForUpload.getFileName());
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        outputStream.putNextEntry(parameters);
        outputStream.write(fileForUpload.getFileData());
        outputStream.closeEntry();
        outputStream.close();

        zipByte.close();
       return new FileForUpload(zipByte.toByteArray(),
                fileForUpload.getFileName().replace(".xlsx",".zip"));
    }

    public String formatJsonReportData(ReportMetadata metadata, Object data){
        ObjectMapper mapper =  new ObjectMapper();
        JSONObject dataJSON = new JSONObject();
        JSONObject completeJson = new JSONObject();

        try {
            JSONObject metaData = new JSONObject(mapper.writeValueAsString(metadata));
            JSONArray dataNode = new JSONArray(mapper.writeValueAsString(data));

            dataJSON.put("metadata",metaData);
            dataJSON.put("data_1",dataNode);

            completeJson.put("ReportData",dataJSON);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return completeJson.toString();
    }

}
