package com.alliance.diceanalytics.service;

import com.alliance.diceanalytics.constant.ApiResponse;
import com.alliance.diceanalytics.constant.FilenamePrefix;
import com.alliance.diceanalytics.constant.ReportDetails;
import com.alliance.diceanalytics.constant.ReportPathConstants;
import com.alliance.diceanalytics.exception.ServiceException;
import com.alliance.diceanalytics.model.CustomerActionTrail;
import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.model.ReportMetadata;
import com.alliance.diceanalytics.model.UploadedFileHistory;
import com.alliance.diceanalytics.repository.CustomerActionTrailRepository;
import com.alliance.diceanalytics.repository.UploadedFileHistoryRepository;
import com.alliance.diceanalytics.request.BaseReportInfoRequest;
import com.alliance.diceanalytics.request.CustomerActionTrailRequest;
import com.alliance.diceanalytics.request.UploadedFileHistoryRequest;
import com.alliance.diceanalytics.response.*;
import com.alliance.diceanalytics.utility.DateUtil;
import com.alliance.diceanalytics.utility.ExportUtil;
import com.alliance.diceanalytics.utility.QueryUtil;
import com.alliance.diceanalytics.utility.SystemParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class AnalyticsService {
    @Autowired
    private ExportUtil exportUtil;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CustomerActionTrailRepository customerActionTrailRepository;

    @Autowired
    private UploadedFileHistoryRepository uploadedFileHistoryRepository;

    @Autowired
    private QueryService queryService;

    @Autowired
    private QueryUtil queryUtil;

    @Autowired
    private ReportingService reportingService;


    public FileForUpload generateReport (String duration, Integer report,Date startDate, Date endDate) throws Exception {
           return generateReport(0,duration,report,startDate,endDate);
    }

    public FileForUpload generateReport(Integer server, String duration, Integer report,Date inputStartDate, Date inputEndDate) throws Exception {
        List<String> acceptableDuration = new ArrayList<>(Arrays.asList("daily","weekly","monthly"));

        if (!acceptableDuration.stream().anyMatch(x ->  x.equals(duration)))
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, "Invalid Duration, Accepted Range: 'Daily','Weekly', 'Monthly' ");

        if (ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report).isEmpty())
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_BAD_REQUEST, "Report of duration: " + duration+ " and type: " + report + " NOT FOUND");



        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        ZoneId zoneId = ZoneId.systemDefault();
        FileForUpload filedetails = null;

        LocalDate initialEndDate = LocalDate.now().minusDays(1);
        //12:00:00 am
        Date endDateMorning = Date.from(initialEndDate.atStartOfDay().atZone(zoneId).toInstant());

        //Yesterday / Report End Date 23:59:59 pm
        Date endDate = Date.from(initialEndDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant());

        //Weekly start date  12:00:00 am
        Date startDate =  Date.from(initialEndDate.minusDays(6).atStartOfDay(zoneId).toInstant());

        //Monthly start date 12:00:00 am
        Date monthDateStart =  Date.from(LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant());
        //23:59:59 pm
        Date monthDateEnd =  Date.from(LocalDate.now().minusDays(1).atTime(LocalTime.MAX).atZone(zoneId).toInstant());



        //Customized Date
        if (inputEndDate!=null && inputStartDate != null){
            startDate = Date.from(inputStartDate.toInstant().atZone(zoneId).toLocalDate().atStartOfDay(zoneId).toInstant());
            endDate =   Date.from(inputEndDate.toInstant().atZone(zoneId).toLocalDate().atTime(LocalTime.MAX).atZone(zoneId).toInstant());

            endDateMorning =Date.from(inputStartDate.toInstant().atZone(zoneId).toLocalDate().atStartOfDay(zoneId).toInstant());
            monthDateStart = Date.from(inputStartDate.toInstant().atZone(zoneId).toLocalDate().atStartOfDay(zoneId).toInstant());
            monthDateEnd = Date.from(inputEndDate.toInstant().atZone(zoneId).toLocalDate().atTime(LocalTime.MAX).atZone(zoneId).toInstant());
        }



        String formattedDate [] = new String[] {format.format(endDate)};
        String formattedDateRange []=  new String[] {format.format(startDate), format.format(endDate)};
        String formattedMonth []=  new String[] {new SimpleDateFormat("MMMyyyy").format(monthDateStart), new SimpleDateFormat("MMMyyyy").format(monthDateEnd)};

        BaseReportInfoRequest request = new BaseReportInfoRequest();


        
        log.info("Generating {} Report startDate: {}, endDate: {}", ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report), startDate, endDate);

        try {
            // String currentDate = format.format(new Date());
            List<Date> dateRange = DateUtil.getCurrentAndLastWeekDateRange();



            switch (duration){
                case "daily":
                    dateRange = new ArrayList<>(Arrays.asList(endDate,endDateMorning,endDate));
                    request =new BaseReportInfoRequest(endDateMorning, endDate, "", "");
                    request =DateUtil.adjustDateTime(request);
                    break;
                case "monthly":
                    dateRange = new ArrayList<>(Arrays.asList(endDate,monthDateStart,monthDateEnd));
                    request =new BaseReportInfoRequest(monthDateStart, monthDateEnd, "", "");
                    request =DateUtil.adjustDateTime(request);
                    break;
                case "weekly":
                    dateRange = new ArrayList<>(Arrays.asList(endDate,startDate,endDate));
                    request =new BaseReportInfoRequest(startDate, endDate, "", "");
                    request =DateUtil.adjustDateTime(request);
                    break;
                default:
                    break;
            }

            //Whatsapp Chatbot Referral
            if (report.equals(0)){
                if(duration.equals("monthly")){
                    filedetails  = reportingService.manualGenerateReferralMonthlyReport(0, null,
                            request.getStartDate(),request.getEndDate(),false);
                }
            }
            //EKYC REFERRAL
            else if(report.equals(1)){
                if(duration.equals("daily")){
                    // Create and populate EKYCSavePlusReferralResponse objects
                    String [] campaignName = new String[] {SystemParam.getInstance().getEkycSavePlusCampaignName().get(0)};

                    request.setCampaignName(campaignName);

                    List<ReferralFulfillmentReportResponse> referralReportResponse =queryService.getAllReferralReportData(request);

                    if (referralReportResponse.size() ==0 ){
                        log.info("{} Report {} don't have data for date {} to {}",
                                duration,
                                ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report),
                                dateRange.get(1),
                                dateRange.get(2)
                        );
                        return new FileForUpload();
                    }

                    Workbook workbook = exportUtil.generateExcelReport(referralReportResponse, ReferralFulfillmentReportResponse.class, "daily", Locale.getDefault(),dateRange,duration,report);
                    String[] fillEmpty = {""};
                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.daily.eKYC.referral.programme.aom", fillEmpty, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL, new String[]{formattedDate[0]});
                }

                else if(duration.equals("monthly")){
                    // Create and populate EKYCSavePlusReferralResponse objects
                    String [] campaignName = new String[] {SystemParam.getInstance().getEkycSavePlusCampaignName().get(0)};

                    request.setCampaignName(campaignName);

                    List<ReferralFulfillmentReportResponse> referralReportResponse =queryService.getAllReferralReportData(request);

                    if (referralReportResponse.size() ==0 ) {
                        log.info("{} Report {} don't have data for date {} to {}",
                                duration,
                                ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration), report),
                                dateRange.get(1),
                                dateRange.get(2)
                        );
                        return new FileForUpload();
                    }

                    Workbook workbook = exportUtil.generateExcelReport(referralReportResponse, ReferralFulfillmentReportResponse.class, "monthly", Locale.getDefault(),dateRange,duration,report);

                    String[] fillEmpty = {""};
                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.daily.eKYC.referral.programme.aom", fillEmpty, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL,formattedMonth);
                }

                else if (duration.equals("weekly")){
                    List <EKYCSavePlusReferralWeeklyReportResponse> eKYCSavePlusReferralWeeklyReportResponse = new ArrayList();
                    request.setEngagementMode("");
                    request.setEventType("");

                    eKYCSavePlusReferralWeeklyReportResponse.add(queryService.getEKYCSavePlusReportData(request));

                    Workbook workbook = exportUtil.generateExcelReport(eKYCSavePlusReferralWeeklyReportResponse, EKYCSavePlusReferralWeeklyReportResponse.class, "weekly", Locale.getDefault(),dateRange,duration,report);

                    String[] fillEmpty = {"",""};
                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.weekly.eKYC.referral.programme.aom", fillEmpty, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL, formattedDateRange);

                }

            }
            //SOLO CC TO PLOAN
            else if(report.equals(2)) {
                //CrossSellSoloCCtoPLoan
                if (duration.equals("weekly")){
                    List<CrossSellSoloCCtoPLoanWeeklyResponse> response = new ArrayList<>();
                    request.setEngagementMode("");
                    request.setEventType("");

                    response.add( queryService.getSoloCCReportData(request));

                    Workbook workbook  = exportUtil.generateExcelReport(response, CrossSellSoloCCtoPLoanWeeklyResponse.class, "weekly", Locale.getDefault(),dateRange,duration,report);

                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.weekly.eKYC.personal.loan.solo.cc", null, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL, formattedDateRange);
                
                }
                //CrossSellSoloCCtoPLoanConversion
                else if(duration.equals("monthly")){
                    List<CrossSellSoloCCtoPLoanMonthlyResponse> data = queryService.getCrossSellSoloCCtoPLoanReportData(request);

                    Workbook workbook  = exportUtil.generateExcelReport(data, EKYCSavePlusReferralWeeklyReportResponse.class, "monthly", Locale.getDefault(),dateRange,duration,report);

                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.monthly.eKYC.personal.loan.solo.cc", null, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL, formattedMonth);
                
                }
            }
            //PLOAN TO SP
            else if(report.equals(3)) {
                if (duration.equals("weekly")){
                    //CrossSellPLoantoSP
                    request.setEngagementMode("");
                    request.setEventType("");

                    List<CrossSellPLoanWeeklyReportResponse> response = new ArrayList<>();
                    response.add(
                            queryService.getEKYCPLoanReportData(request)
                    );

                    Workbook workbook = exportUtil.generateExcelReport(response, CrossSellPLoanWeeklyReportResponse.class, "weekly", Locale.getDefault(),dateRange,duration,report);

                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.weekly.eKYC.savePlus.new.customer.cross.sell", formattedDate, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL, formattedDateRange);

                }
                //CrossSellPLoantoSPConversion
                else if(duration.equals("monthly")){
                    List<CrossSellPLoanToSPMonthlyResponse> data = queryService.getCrossCrossSellPLoanToSPReportData(request);

                    Workbook workbook = exportUtil.generateExcelReport(data, EKYCSavePlusReferralWeeklyReportResponse.class, "monthly", Locale.getDefault(),dateRange,duration,report);

                    filedetails = exportUtil.inputStreamToFile(workbook, messageSource.getMessage("spring.mail.subject.monthly.eKYC.savePlus.new.customer.cross.sell", formattedMonth, Locale.getDefault()), FilenamePrefix.FILE_EXTENSION_EXCEL, formattedMonth);
                
                }
            }
            //HENRY
            else if (report.equals(4)){
                if (duration.equals("weekly")){
                    ReportMetadata reportMetadata = new ReportMetadata(
                            ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report),
                            request.getStartDate(),
                            request.getEndDate(),
                            request.getEndDate()
                    );


                    filedetails=  exportUtil.jasperExportExcel(
                            exportUtil.getReportTemplate(ReportPathConstants.HENRY_REPORT_PATH),
                            exportUtil.formatJsonReportData(reportMetadata,queryService.getHenryReportData(request))
                    );
                    String filename = exportUtil.generateFileNameByDate(
                            ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report)+"_",
                            FilenamePrefix.FILE_EXTENSION_EXCEL,
                            formattedDateRange
                     );

                    filedetails.setFileName(filename);
                    filedetails = exportUtil.compressFileToZip(filedetails);

                }
            }
            //Personal Info Update
            else if (report.equals(6)){
                filedetails = reportingService
                        .generatePersonalInfoUpdateWeeklyReport
                                (request.getStartDate(),request.getEndDate(),null,false);
            }

            else if (report.equals(7)){
                if (duration.equals("daily")){
                    ReportMetadata reportMetadata = new ReportMetadata(
                            ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report),
                            queryUtil.getCampaignDateByName(SystemParam.getInstance().getEkycSavePlusCampaignName().get(1)),
                            request.getEndDate(),
                            request.getEndDate()
                    );


                    filedetails=  exportUtil.jasperExportExcel(
                            exportUtil.getReportTemplate(ReportPathConstants.MASTER_PROMO_REPORT_PATH),
                            exportUtil.formatJsonReportData(reportMetadata,queryService.getMasterPromoCodeListing(request))
                    );

                    String filename = exportUtil.generateFileNameByDate(
                            ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report)+"_",
                            FilenamePrefix.FILE_EXTENSION_EXCEL,
                            new String[] {formattedDate[0]}
                    );

                    filedetails.setFileName(filename);
                    filedetails = exportUtil.encryptFile(filedetails);
                }

            }

            log.info("{} Report Generate Completed startDate: {}, endDate: {}", ReportDetails.getReportName(ReportDetails.ReportDuration.valueOf(duration),report), startDate, endDate);

        } catch (NoSuchFieldException | SecurityException | IOException | JSONException | GeneralSecurityException ex) {
            ex.printStackTrace();
            log.error("generateReferralMonthlyReport - Exception: {}", ex);
        }
        return filedetails;
    }

    public void createCustomerActionTrail(CustomerActionTrailRequest customerActionTrailRequest) throws ServiceException {
        if(!(customerActionTrailRequest==null)){
            CustomerActionTrail currentCustomerActionTrail;
            Boolean isLastAction=false;
            if(customerActionTrailRequest.getAction().equalsIgnoreCase("LAST_ACTION")){
                isLastAction = true;
                currentCustomerActionTrail = customerActionTrailRepository.findAllBySessionIdAndAction(customerActionTrailRequest.getSessionId(), customerActionTrailRequest.getAction());
            }
            else{
                isLastAction = false;
                currentCustomerActionTrail = null;
            }

            if (currentCustomerActionTrail!=null && isLastAction == true){
                currentCustomerActionTrail.setActionPage(customerActionTrailRequest.getActionPage());
                customerActionTrailRepository.save(currentCustomerActionTrail);
            }
            else {
                CustomerActionTrail customerActionTrail = new CustomerActionTrail();
                customerActionTrail.setEvent(customerActionTrailRequest.getEvent());
                customerActionTrail.setAction(customerActionTrailRequest.getAction());
                customerActionTrail.setCifNo(customerActionTrailRequest.getCifNo());
                customerActionTrail.setDeviceId(customerActionTrailRequest.getDeviceId());
                customerActionTrail.setDevicePlatform(customerActionTrailRequest.getDevicePlatform());
                customerActionTrail.setIsLogin(customerActionTrailRequest.getIsLogin());
                customerActionTrail.setActionStatus(customerActionTrailRequest.getActionStatus());
                customerActionTrail.setCampaignId(customerActionTrailRequest.getCampaignId());
                customerActionTrail.setSessionId(customerActionTrailRequest.getSessionId());
                customerActionTrail.setActionPage(customerActionTrailRequest.getActionPage());
                customerActionTrail.setChannel(customerActionTrailRequest.getChannel());
                
                if (customerActionTrailRequest != null && customerActionTrailRequest.getEngagementMode() != null) {
                    customerActionTrail.setEngagementMode(customerActionTrailRequest.getEngagementMode());
                } 
                 else {
                     log.debug("createCustomerActionTrail - Engagement Mode is null");
                 }
                customerActionTrailRepository.save(customerActionTrail);
            }
        }else {
            log.error("createCustomerActionTrail - customerActionTrailRequest is null");
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_CONFLICT,"Request is null");
        }
    }

    public void addUploadedFileHistoryLog(UploadedFileHistoryRequest uploadedFileHistoryRequest) throws ServiceException {
        try {
            if(!(uploadedFileHistoryRequest==null)){ 
                Integer uploadedFileId = (uploadedFileHistoryRepository.getMaxUploadedFileId() != null) ? uploadedFileHistoryRepository.getMaxUploadedFileId() + 1 : 1;               
                UploadedFileHistory uploadedFileHistory = new UploadedFileHistory();

                uploadedFileHistory.setUploadedFileId(uploadedFileId);
                uploadedFileHistory.setFileName(uploadedFileHistoryRequest.getFileName());
                uploadedFileHistory.setFileFormat(uploadedFileHistoryRequest.getFileFormat());
                uploadedFileHistory.setDescription(uploadedFileHistoryRequest.getDescription());
                uploadedFileHistory.setTotalRow(uploadedFileHistoryRequest.getTotalRow());
                uploadedFileHistory.setTotalColumn(uploadedFileHistoryRequest.getTotalColumn());
                uploadedFileHistory.setTotalSheet(uploadedFileHistoryRequest.getTotalSheet());
                uploadedFileHistory.setIsReadHeader(uploadedFileHistoryRequest.getIsReadHeader());
                uploadedFileHistory.setTargetedCampaignList(uploadedFileHistoryRequest.getTargetedCampaignList());
                uploadedFileHistory.setTotalTargetedCampaign(uploadedFileHistoryRequest.getTotalTargetedCampaign());
                uploadedFileHistory.setTotalTriggerRequest(uploadedFileHistoryRequest.getTotalTriggerRequest());
                uploadedFileHistory.setCreatedBy(uploadedFileHistoryRequest.getCreateBy());
                Date currentDate = new Date();
                long currentTimeInMillis = currentDate.getTime();
                long eightHoursInMillis = 8 * 60 * 60 * 1000; // 8 hours in milliseconds
                long malaysiaTimeInMillis = currentTimeInMillis + eightHoursInMillis;
                Date malaysiaTime = new Date(malaysiaTimeInMillis); // to store as malaysia time in cassandra since it will make the date be UTC

                uploadedFileHistory.setCreatedOn(malaysiaTime);
                uploadedFileHistory.setUpdatedOn(malaysiaTime);

                uploadedFileHistoryRepository.save(uploadedFileHistory);
            }else {
                log.info("addUploadedFileHistoryLog - uploadedFileHistoryRequest is null");
                throw new ServiceException(ApiResponse.HTTP_RESPONSE_CONFLICT,"Request is null");
            }
        } catch (Exception ex) {
                log.error("addUploadedFileHistoryLog - Exception: {}", ex);
        }
    }
}
