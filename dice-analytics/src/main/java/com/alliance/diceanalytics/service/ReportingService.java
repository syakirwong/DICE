package com.alliance.diceanalytics.service;

import com.alliance.diceanalytics.constant.FilenamePrefix;
import com.alliance.diceanalytics.exception.ServiceException;
import com.alliance.diceanalytics.model.FileForUpload;
import com.alliance.diceanalytics.repository.ReferralRepository;
import com.alliance.diceanalytics.request.BaseReportInfoRequest;
import com.alliance.diceanalytics.request.SendCommonEmailRequest;
import com.alliance.diceanalytics.response.CampaignReportResponse;
import com.alliance.diceanalytics.response.PersonalInfoUpdateReportResponse;
import com.alliance.diceanalytics.response.WhatsappReferralReportResponse;
import com.alliance.diceanalytics.utility.ExportUtil;
import com.alliance.diceanalytics.utility.FreemarkerUtil;
import com.alliance.diceanalytics.utility.SystemParam;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

@Slf4j
@Service
public class ReportingService {
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ReferralRepository referralRepository;


    @Autowired
    private ExportUtil exportUtil;
    @Autowired
    private FreemarkerUtil freemarkerUtil;
    @Autowired
    private MessagingService messagingService;
    @Autowired
    private QueryService queryService;


    @Value("${referral.code.maxLength}")
    private Integer REFERRAL_CODE_MAX_LENGTH;
    @Value("${freemarker.email.template.name.monthly.referral}")
    private String EMAIL_MONTHLY_REFERRAL_TEMPLATE_NAME;
    @Value("${freemarker.email.template.name.weekly.personal.info}")
    private String EMAIL_WEEKLY_PERSONAL_INFO_TEMPLATE_NAME;
    @Value("#{'${spring.mail.monthly.referral.report.to}'.split(',')}")
    private List<String> EMAIL_MONTHLY_REFERRAL_TO;
    @Value("#{'${spring.mail.weekly.personal.info.report.to}'.split(',')}")
    private List<String> EMAIL_WEEKLY_PERSONAL_INFO_TO;



    //Monthly whatsapp chatbot referral
    public FileForUpload manualGenerateReferralMonthlyReport(Integer server, String filename,Date inputStartDate,Date inputEndDate,Boolean sentOut) throws ServiceException, GeneralSecurityException {
        String [] campaignName = new String[] {SystemParam.getInstance().getEkycSavePlusCampaignName().get(1)};

        Date starDate = null;

        Date endDate = null;

        if (inputEndDate!=null && inputStartDate != null){
            starDate = inputStartDate;
            endDate =  inputEndDate;
        }


        BaseReportInfoRequest request = new BaseReportInfoRequest();
        request.setCampaignName(campaignName);
        request.setStartDate(starDate);
        request.setEndDate(endDate);


        List<WhatsappReferralReportResponse> list = queryService.getWAReferralReportData(request);
        log.info("manualGenerateMonthlyReport -  Process Begin. startDate: {}, endDate: {}", starDate, endDate);


        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String currentDate = format.format(new Date());
            if(filename == null || filename.isEmpty()) {
                filename = FilenamePrefix.EKYC_REFERRAL_REPORT + currentDate +  FilenamePrefix.FILE_EXTENSION_EXCEL;
            }

            log.info("manualGenerateMonthlyReport - Generating File. startDate: {}, endDate: {}", starDate, endDate);
            byte[] attachmentBytes = exportUtil.exportExcelV2(list, WhatsappReferralReportResponse.class, "referral", Locale.getDefault());

            if (sentOut){
                log.info("manualGenerateMonthlyReport - Begin Email. startDate: {}, endDate: {}", starDate, endDate);
                sendMonthlyReportEmailV2(filename, currentDate, attachmentBytes);
                log.info("manualGenerateMonthlyReport - Completed. startDate: {}, endDate: {}", starDate, endDate);
            }
            return new FileForUpload(attachmentBytes,filename) ;
        } catch (NoSuchFieldException | SecurityException | IOException ex) {
            log.error("manualGenerateMonthlyReport - Exception: {}", ex);
        }

        return null;
    }

    public void sendMonthlyReportEmailV2(String fileName, String currentDate, byte[] attachmentBytes) {
        Map<String, Object> mailInfo = new HashMap<String, Object>();
        mailInfo.put("emailTemplate", EMAIL_MONTHLY_REFERRAL_TEMPLATE_NAME);
        mailInfo.put("title", messageSource.getMessage("spring.mail.subject.monthly.referral", new String[] {currentDate}, Locale.ENGLISH));

        try {
            String mailBody = freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{reportName}",fileName);
            SendCommonEmailRequest request = new SendCommonEmailRequest(messageSource.getMessage("spring.mail.subject.monthly.referral", new String[] {currentDate}, Locale.ENGLISH), null,
                    SystemParam.getInstance().getMailMonthlyReferralReportTo().toArray(new String[0]), mailBody, attachmentBytes, fileName);

            messagingService.sendEmail(Arrays.asList(request));

        } catch (NoSuchMessageException | IOException | TemplateException ex) {
            log.info("sendMonthlyReportEmail - Exception: {}", ex);
        }

    }

    public FileForUpload generatePersonalInfoUpdateWeeklyReport(Date inputStartDate,Date inputEndDate, String filename,Boolean sendOut) throws Exception {

        List<Date> dateRange = new ArrayList<>();

        BaseReportInfoRequest actionInfoRequest = new BaseReportInfoRequest(
                inputStartDate,
                inputEndDate,
                "",
                ""
        );

        if (inputEndDate!=null && inputStartDate != null){
            Date displayEndDate =  Date.from(
                     LocalDateTime.ofInstant(inputEndDate.toInstant(),ZoneId.systemDefault())
                             .minusHours(8)
                             .atZone(ZoneId.systemDefault())
                             .toInstant()
            );

            Date displayStartDate = Date.from(
                    LocalDateTime.ofInstant(inputStartDate.toInstant(),ZoneId.systemDefault())
                            .minusHours(8)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );


            dateRange.add(displayEndDate);
            dateRange.add(displayStartDate);
            dateRange.add(displayEndDate);

        }




        log.info("generatePersonalInfoUpdateWeeklyReport -  Process Begin. startDate: {}, endDate: {}", dateRange.get(1).toString(), dateRange.get(2).toString());
        PersonalInfoUpdateReportResponse personalInfoUpdateReportResponse = queryService.getPersonalInfoUpdateReportData(actionInfoRequest);

        CampaignReportResponse campaignReportResponse = new CampaignReportResponse();
        // campaignReportResponse.setTotalTargetedCustomer(targetedCustomerRepository.getTotalTargetedCustomerForWeek());
        // campaignReportResponse.setTotalNotificationSent(personalInfoUpdateReportResponse.getTriggeredCountByDateRangeAndEngagementMode(dateRange.get(1), dateRange.get(2),"BELL"));
        campaignReportResponse.setTotalPushNotificationTap(1);
        campaignReportResponse.setTotalBellBoxNotificationTapPostLogin(1);
        campaignReportResponse.setTotalFloatingButtonTapPostLogin(1);
        campaignReportResponse.setTotalGeneralLogoutTap(1);
        campaignReportResponse.setTotalTargetLogoutTap(1);
        campaignReportResponse.setTotalBellBoxNotificationTapPreLogin(1);
        campaignReportResponse.setTotalFloatingButtonPreLogin(1);
        campaignReportResponse.setTotalInAppMessageReached(1);
        campaignReportResponse.setTotalUpdateNowTap(1);
        campaignReportResponse.setTotalDontShowMeAgainTap(1);
        campaignReportResponse.setTotalNextTap(1);
        campaignReportResponse.setTotalNoChangeTap(1);
        campaignReportResponse.setTotalSuccessLogin(1);
        campaignReportResponse.setTotalFailLogin(1);
        campaignReportResponse.setTotalWithFacialProfilInteger(1);
        campaignReportResponse.setTotalWithOutFacialProfilInteger(1);
        campaignReportResponse.setTotalEnrollFacialProfile(1);
        campaignReportResponse.setTotalSuccessFacialBiometric(1);
        campaignReportResponse.setTotalFailFacialBiometric(1);
        campaignReportResponse.setTotalSuccessUpdateSubmit(1);
        campaignReportResponse.setTotalFailUpdateSubmit(1);

        String[] reportOneColumnThree = {
                // 1 : Total number of targeted customers uploaded
                personalInfoUpdateReportResponse.getTotalTargetCustomerUploaded().toString(),

                // A : Total number of customers targeted for push notification
                personalInfoUpdateReportResponse.getTotalTargetCustomerPushNoti().toString(),

                // B : Total number of customers tapped on bell inbox notification (post-login)
                personalInfoUpdateReportResponse.getTotalNumCustTapBellPostLogin().toString(),

                // C : Total number of customers targeted for floating button (post-login)
                personalInfoUpdateReportResponse.getTotalTargetCustomerFloatingButtonPostLogin().toString(),

                // D : Total number of customers targeted for logout
                personalInfoUpdateReportResponse.getTotalTargetCustomerFloatingButtonPostLogin().toString(),

                // E : Total number of customers tapped on bell inbox notification (pre-login)
                "0",

                // F : Total number of customers targeted for floating button (pre-login)
                "0",

                // 2 : How many successfully submitted the personal information update requests
                personalInfoUpdateReportResponse.getPersonalInfoUpdateRequestSubmitted().toString(),

                // A : How many customers being targeted for push notification
                personalInfoUpdateReportResponse.getNoOfTargetedCustomerPushNoti2A().toString(),

                // B : How many customers tapped on bell inbox notification (post-login)
                personalInfoUpdateReportResponse.getTotalNumCustTapBellPostLogin().toString(),

                // C : How many customers being targeted for floating button (post-login)
                personalInfoUpdateReportResponse.getNoOfTargetedCustomerFloatingBtn2C().toString(),


                // D : How many customers being targeted for logout
                personalInfoUpdateReportResponse.getNoOfTargetedCustomerLogout().toString(),


                // E : How many customers tapped on bell inbox notification (pre-login)
                "0",

                // F : How many customers being targeted for floating button (pre-login)
                "0",

                // 3 : How many failed to submit the personal information update requests
                personalInfoUpdateReportResponse.getNoOfCustomerFailedSubmit().toString(),

                // A : How many customers being targeted for push notification
                personalInfoUpdateReportResponse.getNoOfTargetedCustomerPushNoti3A().toString(),

                // i : How many targeted customers tapped on the push notification
                personalInfoUpdateReportResponse.getTotalNumCustTapPush().toString(),

                //     a) How many targeted customers reached in-app message
                "0",

                //     i) How many targeted customers tapped on “Update Now”
                "0",

                //     ii) How many targeted customers tapped on “Don’t show me again”
                "0",

                //     iii) How many targeted customers tapped on “X”
                "0",

                //     b) How many targeted customers reached Personal Info eForm
                "0",

                //     i) How many targeted customers tapped on “Next”
                "0",

                //     ii) How many targeted customers tapped on “No change”
                "0",

                //     iii) How many targeted customers tapped on “X”
                "0",

                //     c) How many targeted customers reached Summary page
                "0",

                //     i) How many targeted customers tapped on “Submit”
                "0",

                //     iii) How many targeted customers tapped on “X”
                "0",

                // ii : How many targeted customers did not tap on the push notification
                String.valueOf((personalInfoUpdateReportResponse.getNoOfTargetedCustomerNotTappedPushNoti())-(personalInfoUpdateReportResponse.getTotalNumCustTapPush())),

                // B : How many customers tapped on bell inbox notification (post-login)
                personalInfoUpdateReportResponse.getTotalNumCustTapBellPostLogin().toString(),

                // i : How many targeted customers tapped on personal info engagement message in the bell inbox notification (post-login)
                personalInfoUpdateReportResponse.getTotalNumCustTapBellPostLogin().toString(),

                //     a) How many targeted customers reached in-app message
                "0",
                //     i) How many targeted customers tapped on “Update Now”
                "0",
                //     ii) How many targeted customers tapped on “Don’t show me again”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     b) How many targeted customers reached Personal Info eForm
                "0",
                //     i) How many targeted customers tapped on “Next”
                "0",
                //     ii) How many targeted customers tapped on “No change”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     c) How many targeted customers reached Summary page
                "0",
                //     i) How many targeted customers tapped on “Submit”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                // ii : How many targeted customers did not tap on personal info engagement message in the bell inbox notification (post-login)
                "0",

                // C : How many customers being targeted for floating button (post-login)
                personalInfoUpdateReportResponse.getNoOfTargetedCustomerFloatingBtn3C().toString(),

                // i : How many targeted customers tapped on floating button (post-login)
                personalInfoUpdateReportResponse.getTotalNumCustTapFloat().toString(),
                //     a) How many targeted customers reached in-app message
                "0",
                //     i) How many targeted customers tapped on “Update Now”
                "0",
                //     ii) How many targeted customers tapped on “Don’t show me again”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     b) How many targeted customers reached Personal Info eForm
                "0",
                //     i) How many targeted customers tapped on “Next”
                "0",
                //     ii) How many targeted customers tapped on “No change”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     c) How many targeted customers reached Summary page
                "0",
                //     i) How many targeted customers tapped on “Submit”
                "0",
                //     ii) How many targeted customers tapped on “X”
                "0",
                // ii) How many targeted customers did not tap on floating button (post-login)
                String.valueOf((personalInfoUpdateReportResponse.getNoOfTargetedCustomerNotTappedPushNoti())-(personalInfoUpdateReportResponse.getTotalNumCustTapFloat())),

                // D : How many customers being targeted for logout
                personalInfoUpdateReportResponse.getNoOfTargetedCustomerNotLogout().toString(),
                // i : How many targeted customers tapped on logout
                personalInfoUpdateReportResponse.getTotalNumCustTapLogout().toString(),
                //     a) How many targeted customers reached in-app message
                "0",
                //     i) How many targeted customers tapped on “Update Now”
                "0",
                //     ii) How many targeted customers tapped on “Don’t show me again”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     b) How many targeted customers reached Personal Info eForm
                "0",
                //     i) How many targeted customers tapped on “Next”
                "0",
                //     ii) How many targeted customers tapped on “No change”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     c) How many targeted customers reached Summary page
                "0",
                //     i) How many targeted customers tapped on “Submit”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                // ii : How many targeted customers did not tap on logout
                String.valueOf(Math.max(personalInfoUpdateReportResponse.getNoOfTargetedCustomerNotLogout() - personalInfoUpdateReportResponse.getTotalNumCustTapLogout(), 0)),
                //String.valueOf((personalInfoUpdateReportResponse.getTriggeredCountByDateRangeAndEngagementModeAndIsCampaignUpdatedFalse()- (personalInfoUpdateReportResponse.getTotalNumCustTapLogout()))),
                //     How many customers tapped on logout in general
                personalInfoUpdateReportResponse.getTotalNumCustTapLogout().toString(),
                // E : How many customers tapped on bell inbox notification (pre-login)
                "0",
                // i : How many targeted customers tapped on personal info engagement message in the bell inbox notification (pre-login)
                "0",
                //     a) How many targeted customers reached in-app message
                "0",
                //     i) How many targeted customers tapped on “Update Now”
                "0",
                //     ii) How many targeted customers tapped on “Don’t show me again”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     b) How many targeted customers reached Personal Info eForm
                "0",
                //     i) How many targeted customers tapped on “Next”
                "0",
                //     ii) How many targeted customers tapped on “No change”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     c) How many targeted customers reached Summary page
                "0",
                //     i) How many targeted customers tapped on “Submit”
                "0",
                //     ii) How many targeted customers tapped on “X”
                "0",
                //     d) How many targeted customers reached Login page
                "0",
                //     i) How many targeted customers successfully login
                "0",
                //     ii) How many targeted customers failed to login
                "0",
                // ii : How many targeted customers did not tap on personal info engagement message in the bell inbox notification (pre-login)
                "0",
                // F : How many customers being targeted for floating button (pre-login)
                "0",
                // i : How many targeted customers tapped on floating button (pre-login)
                "0",
                //     a) How many targeted customers reached in-app message
                "0",
                //     i) How many targeted customers tapped on “Update Now”
                "0",
                //     ii) How many targeted customers tapped on “Don’t show me again”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     b) How many targeted customers reached Personal Info eForm
                "0",
                //     i) How many targeted customers tapped on “Next”
                "0",
                //     ii) How many targeted customers tapped on “No change”
                "0",
                //     iii) How many targeted customers tapped on “X”
                "0",
                //     c) How many targeted customers reached Summary page
                "0",
                //     i) How many targeted customers tapped on “Submit”
                "0",
                //     ii) How many targeted customers tapped on “X”
                "0",
                //     d) How many targeted customers reached Login page
                "0",
                //     i) How many targeted customers successfully login
                "0",
                //     ii) How many targeted customers failed to login
                "0",
                // ii : How many targeted customers did not tap on floating button (pre-login)
                "0"
        };

        // Create table for report 2
        String[] reportTwoColumnThree = {
                // A : How many customers targeted to update high risk personal information
                "0",
                // i : How many with facial profile
                "0",
                //     a) How many successfully performed facial biometric
                "0",
                //     b) How many failed to perform facial biometric
                "0",
                // ii : How many without facial profile
                "0",
                //     a) How many managed to enroll facial profile
                "0",
                //     b) How many did not manage to enroll facial profile
                "0"
        };

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String currentDate = format.format(new Date());
            if(filename == null || filename.isEmpty()) {
                filename = FilenamePrefix.EKYC_PERSONAL_INFO_UPDATE_REPORT + currentDate +  FilenamePrefix.FILE_EXTENSION_EXCEL;
            }

            log.info("generatePersonalInfoUpdateWeeklyReport - Generating File. startDate: {}, endDate: {}", dateRange.get(1).toString(), dateRange.get(2).toString());
            //InputStream inputStream = exportUtil.exportPersonalInfoUpdateExcel(campaignReportResponse, CampaignReportResponse.class, "personalInfoUpdate", Locale.getDefault(), dateRange, reportOneColumnThree, reportTwoColumnThree);
            byte[] attachmentBytes = exportUtil.exportPersonalInfoUpdateExcel(campaignReportResponse, CampaignReportResponse.class, "personalInfoUpdate", Locale.getDefault(), dateRange, reportOneColumnThree, reportTwoColumnThree);


            // log.info("generatePersonalInfoUpdateWeeklyReport - Begin SFTP. startDate: {}, endDate: {}", dateRange.get(1).toString(), dateRange.get(2).toString());
            if (sendOut ==true){

                log.info("generatePersonalInfoUpdateWeeklyReport - Begin Email. startDate: {}, endDate: {}", dateRange.get(1).toString(), dateRange.get(2).toString());
                // sendPersonalInfoWeeklyReportEmail(filename, currentDate, encryptedBytes);
                sendPersonalInfoWeeklyReportEmail(filename, currentDate, attachmentBytes);
                log.info("generatePersonalInfoUpdateWeeklyReport - Completed. startDate: {}, endDate: {}", dateRange.get(1).toString(), dateRange.get(2).toString());
            }

            return new FileForUpload(attachmentBytes,filename);

        } catch (SecurityException | IOException ex) {
            log.error("generatePersonalInfoUpdateWeeklyReport - Exception: {}", ex);
        }
        return  new FileForUpload();
    }

    public void sendPersonalInfoWeeklyReportEmail(String fileName, String currentDate, byte[] attachmentBytes) {
        log.info("Start - sendPersonalInfoWeeklyReportEmail");
        Map<String, Object> mailInfo = new HashMap<String, Object>();
        mailInfo.put("emailTemplate", EMAIL_WEEKLY_PERSONAL_INFO_TEMPLATE_NAME);
        mailInfo.put("title", messageSource.getMessage("spring.mail.subject.weekly.personal.info", new String[] {currentDate}, Locale.ENGLISH));


        try {
            String mailBody = freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{reportName}",fileName);
            SendCommonEmailRequest request = new SendCommonEmailRequest(messageSource.getMessage("spring.mail.subject.weekly.personal.info", new String[] {currentDate}, Locale.ENGLISH), null,
                    SystemParam.getInstance().getMailWeeklyPersonalInfoReportTo().toArray(new String[0]), mailBody, attachmentBytes, fileName);

            messagingService.sendEmail(Arrays.asList(request));

        } catch (NoSuchMessageException | IOException | TemplateException ex) {
            log.error("sendPersonalInfoWeeklyReportEmail - Exception: {}", ex);
        }

    }




}
