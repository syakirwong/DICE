package com.alliance.diceanalytics.service;

import com.alliance.diceanalytics.exception.ServiceException;
import com.alliance.diceanalytics.model.*;
import com.alliance.diceanalytics.repository.CustomerActionTrailRepository;
import com.alliance.diceanalytics.repository.ReferralRepository;
import com.alliance.diceanalytics.repository.UploadedFileHistoryRepository;
import com.alliance.diceanalytics.request.BaseReportInfoRequest;
import com.alliance.diceanalytics.response.*;
import com.alliance.diceanalytics.utility.AESEncyptionUtil;
import com.alliance.diceanalytics.utility.DateUtil;
import com.alliance.diceanalytics.utility.QueryUtil;
import com.alliance.diceanalytics.utility.SystemParam;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CriteriaDefinition;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QueryService {

    @Autowired
    private QueryUtil queryUtil;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UploadedFileHistoryRepository fileHistoryRepository;

    @Autowired
    private CustomerActionTrailRepository actionTrailRepository;

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private ReferralRepository referralRepository;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

/* WEEKLY DATA */
    //Personal Info Update
    public PersonalInfoUpdateReportResponse getPersonalInfoUpdateReportData(BaseReportInfoRequest request) throws Exception, ServiceException {
       PersonalInfoUpdateReportResponse response = new PersonalInfoUpdateReportResponse();


       QueryCondition conditions[];

       QueryCondition orConditions[];
       QueryCondition andConditions [];
       
        // 1 : Total number of targeted customers uploaded
        response.setTotalTargetCustomerUploaded(fileHistoryRepository.getSumOfTotalRowByDate(
                request.getStartDate(),
                request.getEndDate()
        ));


        request.setTableType(Recommendation.class);
        orConditions = getNonTriggeredQuery();

        // A : Total number of customers targeted for push notification

        andConditions = new QueryCondition[]{
                new QueryCondition("engagement_mode","PUSH",CriteriaDefinition.Operators.CONTAINS),
        };
        response.setTotalTargetCustomerPushNoti(queryUtil.countByORFilterCondition(orConditions,andConditions, request));



        // C : Total number of customers targeted for floating button (post-login)
        andConditions = new QueryCondition[]{
                new QueryCondition("engagement_mode","FLOAT",CriteriaDefinition.Operators.CONTAINS),
        };
        response.setTotalTargetCustomerFloatingButtonPostLogin(queryUtil.countByORFilterCondition(orConditions,andConditions, request));



        // D : Total number of customers targeted for logout
        andConditions = new QueryCondition[]{
                new QueryCondition("engagement_mode","LOGOUT",CriteriaDefinition.Operators.CONTAINS),
        };

        response.setTotalTargetCustomerLogout(queryUtil.countByORFilterCondition(orConditions,andConditions,request));




        // 2 : How many successfully submitted the personal information update requests
        //isTriggered = true OR triggerStatus = 'NON_TRIGGER' OR triggerStatus = 'COMPLETED'
        orConditions = getNonTriggeredORCompletedQuery();

        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",true,CriteriaDefinition.Operators.EQ),
        };
        response.setPersonalInfoUpdateRequestSubmitted(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));



        //isTriggered = true OR triggerStatus = 'NON_TRIGGER'
        orConditions = getNonTriggeredQuery();


        // A : How many customers being targeted for push notification
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","PUSH",CriteriaDefinition.Operators.CONTAINS),
        };

        response.setNoOfTargetedCustomerPushNoti2A(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));


        // C : How many customers being targeted for floating button (post-login) 2C
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","FLOAT",CriteriaDefinition.Operators.CONTAINS),
        };

        response.setNoOfTargetedCustomerFloatingBtn2C(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));


        // D : How many customers being targeted for logout
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","LOGOUT",CriteriaDefinition.Operators.CONTAINS),
        };
        response.setNoOfTargetedCustomerLogout(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));



        //isTriggered = true OR triggerStatus = 'NON_TRIGGER' OR triggerStatus = 'COMPLETED'
        orConditions = getNonTriggeredORCompletedQuery();


        // 3 : How many failed to submit the personal information update requests
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",false,CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_ignore",false,CriteriaDefinition.Operators.EQ),
        };

        response.setNoOfCustomerFailedSubmit(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));


        // A : How many customers being targeted for push notification
        orConditions = getNonTriggeredQuery();
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",false,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","PUSH",CriteriaDefinition.Operators.CONTAINS),
        };

        response.setNoOfTargetedCustomerPushNoti3A(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));


        // How many targeted customers did not tap on the push notification
        response.setNoOfTargetedCustomerNotTappedPushNoti(response.getNoOfTargetedCustomerPushNoti3A());


        // C : How many customers being targeted for floating button (post-login)
        orConditions = getNonTriggeredQuery();
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",false,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","FLOAT",CriteriaDefinition.Operators.CONTAINS),
        };
        response.setNoOfTargetedCustomerFloatingBtn3C(queryUtil.countByORFilterCondition(
                        orConditions,
                        andConditions,
                        request
        ));

      // ii) How many targeted customers did not tap on floating button (post-login)
        orConditions = getNonTriggeredQuery();
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",false,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","FLOAT",CriteriaDefinition.Operators.CONTAINS),
        };
        response.setNoOfTargetedCustomerNotTappedFloatingBtn(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));

        // D : How many customers being targeted for logout
        orConditions = getNonTriggeredQuery();
        andConditions = new QueryCondition[]{
                new QueryCondition("is_campaign_updated",false,CriteriaDefinition.Operators.EQ),
                new QueryCondition("engagement_mode","LOGOUT",CriteriaDefinition.Operators.CONTAINS),
        };
        response.setNoOfCustomerLogout3D(queryUtil.countByORFilterCondition(
                orConditions,
                andConditions,
                request
        ));

        // ii : How many targeted customers did not tap on logout
        response.setNoOfTargetedCustomerNotLogout(response.getNoOfCustomerLogout3D());



        request.setTableType(CustomerActionTrail.class);
        //Total number of customers tapped on bell inbox notification (post-login)
        conditions = new QueryCondition[]{
           new QueryCondition("action","BELL_INBOX_NOTIFICATION",CriteriaDefinition.Operators.EQ),
           new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
           new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
        };
        response.setTotalNumCustTapBellPostLogin(queryUtil.countByFilterCondition(conditions, request));

        // i : How many targeted customers tapped on the push notification
        conditions = new QueryCondition[]{
                new QueryCondition("action","PUSH_NOTIFICATION",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
        };
        response.setTotalNumCustTapPush(queryUtil.countByFilterCondition(conditions,request));

        // i : How many targeted customers tapped on floating button (post-login)
        conditions = new QueryCondition[]{
                new QueryCondition("action","FLOATING_BUTTON",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
        };
        response.setTotalNumCustTapFloat(queryUtil.countByFilterCondition(conditions,request));


        // i : How many targeted customers tapped on logout
        conditions = new QueryCondition[]{
                new QueryCondition("action","LOGOUT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
        };
        response.setTotalNumCustTapLogout(queryUtil.countByFilterCondition(conditions,request));


       return response;
    }

    //EKYC Referral
    public EKYCSavePlusReferralWeeklyReportResponse getEKYCSavePlusReportData(BaseReportInfoRequest request){
        EKYCSavePlusReferralWeeklyReportResponse response = new EKYCSavePlusReferralWeeklyReportResponse();
        Integer campaignId = queryUtil.getCampaignIdsByName(SystemParam.getInstance().getEkycSavePlusCampaignName().get(0)).get(0);
        //Insert campaignFilter
        request.setCampaignName(new String[]{SystemParam.getInstance().getEkycSavePlusCampaignName().get(0)});
        request.setTableType(CustomerActionTrail.class);

        QueryCondition conditions[];

        //numberOfClicksInFloatingIcon
        conditions = new QueryCondition[]{
                new QueryCondition("action","PRE_FLOAT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","LOGIN_PASSWORD_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
        };
        response.setNumberOfClicksInFloatingIcon(queryUtil.countByCampaignFilterCondition(conditions,request));

        //numberOfClicksInPermanentMenu;
        conditions = new QueryCondition[]{
                new QueryCondition("action","PERMENANT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","POST_LOGIN_MENU_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
        };
        response.setNumberOfClicksInPermanentMenu(queryUtil.countByCampaignFilterCondition(conditions,request));

        //numberOfClicksOnReferNowIcon;
        conditions = new QueryCondition[]{
                new QueryCondition("action","OPEN_SS",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","IN_APP_MSG_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
        };
        response.setNumberOfClicksOnReferNowIcon(queryUtil.countByCampaignFilterCondition(conditions,request));

        //numberOfUniqueCustomerClicksOnReferNowIcon;
        conditions = new QueryCondition[]{
                new QueryCondition("action","OPEN_SS",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","IN_APP_MSG_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
        };

        response.setNumberOfUniqueCustomerClicksOnReferNowIcon(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));


        //numberOfSuccessfulReferral;
        response.setNumberOfSuccessfulReferral(
                referralRepository.getByRefereeInitiationDate(
                    request.getStartDate(),
                    request.getEndDate(),
                    campaignId
                ).size()
        );
        
        return response;
    }

    //PLoantoSP
    public CrossSellPLoanWeeklyReportResponse getEKYCPLoanReportData(BaseReportInfoRequest request){
        CrossSellPLoanWeeklyReportResponse response = new CrossSellPLoanWeeklyReportResponse();
        request.setCampaignName(SystemParam.getInstance().getEkycPloanCrossSellCampaignName().toArray(new String[0]));

        QueryCondition conditions[];
        request.setTableType(CustomerActionTrail.class);

//     noOfCustomerPromptedWithCrossSellPage;
        request.setCampaignId(null);
        conditions= new QueryCondition[]{
                new QueryCondition("action","PLOAN_FULL_SUBMIT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","PLOAN_TQ_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","SHOWED",CriteriaDefinition.Operators.EQ)
        };

        response.setNoOfCustomerPromptedWithCrossSellPage(queryUtil.countUniqueByFilterCondition(conditions,request));

        request.setCampaignId(null);

//      noOfApplyNowClicked;  buttontype= EKYC_SP
        conditions= new QueryCondition[]{
                new QueryCondition("action","EKYC_SP",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","IN_APP_MSG_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ)
        };
        response.setNoOfApplyNowClicked(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));


//       noOfSuccessfulSavePlusOpening;
        request.setTableType(CampaignJourney.class);
        conditions= new QueryCondition[]{};

        response.setNoOfSuccessfulSavePlusOpening(queryUtil.countByCampaignFilterCondition(conditions,request));

        return response;
    }

    //SOLO CC TO PLOAN
    public CrossSellSoloCCtoPLoanWeeklyResponse getSoloCCReportData(BaseReportInfoRequest request){
        CrossSellSoloCCtoPLoanWeeklyResponse response = new CrossSellSoloCCtoPLoanWeeklyResponse();
        request.setCampaignName(SystemParam.getInstance().getEkycPloanSoloCCCampaignName().toArray(new String[0]));
        request.setTableType(CustomerActionTrail.class);

        QueryCondition [] conditions;
//noOfFloatingButtonDisplayed;
        conditions = new QueryCondition[]{
                new QueryCondition("action","POST_FLOAT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("actionPage","POST_LOGIN_MENU_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","SHOWED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
        };
        response.setNoOfFloatingButtonDisplayed(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));

//noOfFloatingIconClicked;
        conditions = new QueryCondition[]{
                new QueryCondition("action","POST_FLOAT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("actionPage","POST_LOGIN_MENU_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
        };
        response.setNoOfFloatingIconClicked(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));

//noOfApplyNowClicked;
        conditions= new QueryCondition[]{
                new QueryCondition("action","EKYC_PLOAN",CriteriaDefinition.Operators.EQ),
                new QueryCondition("actionPage","IN_APP_MSG_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ)
        };
        response.setNoOfApplyNowClicked(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));


//noOfAIPSubmission;
        request.setCampaignId(null);
        conditions= new QueryCondition[]{
                new QueryCondition("action","PLOAN_AIP_SUBMIT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","REACHED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ)
        };
        response.setNoOfAIPSubmission(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));

//noOfFullSubmission;
        conditions= new QueryCondition[]{
                new QueryCondition("action","PLOAN_FULL_SUBMIT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","SUCCESS",CriteriaDefinition.Operators.EQ)
        };
        response.setNoOfFullSubmission(queryUtil.countUniqueByCampaignFilterCondition(conditions,request));

        return response;
    }

    //HENRY
    public List<HenryPerformanceWeeklyResponse> getHenryReportData(BaseReportInfoRequest request){
        List<HenryPerformanceWeeklyResponse> henryPerformanceWeeklyResponse = new ArrayList<>();
        String [] engagementMode= new String[]
                {"Push Notification","Pre-Login Floating Button",
                "Post-Login Floating Button","Bell Notification"};
        for (String mode: engagementMode){
            HenryPerformanceWeeklyResponse reportResponse = new HenryPerformanceWeeklyResponse();
            reportResponse.setEngagementMode(mode);
            henryPerformanceWeeklyResponse.add(reportResponse);
        }

        return henryPerformanceWeeklyResponse;
    }


//MONTHLY DATA
    public List<CustomerActionTrail> getAIPandFullSubmitTrail(BaseReportInfoRequest request){

        List<CustomerActionTrail> customerActionTrailList = new ArrayList<>();

        final QueryCondition [] fullSubmissionCondition= new QueryCondition[]{
                new QueryCondition("action","PLOAN_FULL_SUBMIT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","SUCCESS",CriteriaDefinition.Operators.EQ)
        };

        final  QueryCondition [] aipSubmissionCondition = new QueryCondition[]{
                new QueryCondition("action","PLOAN_AIP_SUBMIT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","REACHED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("is_login",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("cif_no","", CriteriaDefinition.Operators.GTE)
        };

        List<CustomerActionTrail> aipSubmitList = (List<CustomerActionTrail>)queryUtil.selectAllByConditions(aipSubmissionCondition,request);

        request.setCampaignId(null);
        customerActionTrailList.addAll((List<CustomerActionTrail>)queryUtil.selectAllByConditions(fullSubmissionCondition,request));

        if (customerActionTrailList.size()==0)
            customerActionTrailList.addAll(aipSubmitList);

        aipSubmitList.stream().forEach(
                aipSubmission-> {
                    if (customerActionTrailList.stream().anyMatch(
                            fullSubmssion -> {
                                return !fullSubmssion.getDeviceId().equals(aipSubmission.getDeviceId());
                            })) {
                        customerActionTrailList.add(aipSubmission);
                    }
                }
        );

        return customerActionTrailList;
    }

    public List<CrossSellSoloCCtoPLoanMonthlyResponse> getCrossSellSoloCCtoPLoanReportData(BaseReportInfoRequest request){
        List<CrossSellSoloCCtoPLoanMonthlyResponse> response = new ArrayList<>();
        String [] campaignName = new String[] {SystemParam.getInstance().getEkycPloanSoloCCCampaignName().get(0)};

        request.setCampaignName(campaignName);
        request.setCampaignId(queryUtil.getCampaignIdsByName(campaignName[0]).get(0));
        request.setTableType(CustomerActionTrail.class);

        //Get All AIP Submission Records Query

        //Execute Query
        List<CustomerActionTrail> customerList = getAIPandFullSubmitTrail(request);

        //Loop each AIP Submission to lookup customer profile and
        customerList
                .stream()
                .forEach(customer->{

                    PLoanCustomerProfileResponse customerProfile= null;
                    customerProfile = customerProfileService.getPLoanCustomerProfileByDevice(customer.getDeviceId());

                    if (customerProfile!= null){
                        if (customerProfile.getMobileNo() == null)
                            customerProfile.setMobileNo("");
                        if (customerProfile.getIdNo() == null)
                            customerProfile.setIdNo("");
                    }
             // Get Customer applyNowDate

            String applyNowDate =   getMaxClickApplyNowDate("device_id",customer.getDeviceId(),"EKYC_PLOAN",customer.getCreatedOn());

            String firstLoginDate = getMinFirstLoginDate(customer.getDeviceId());


            CrossSellSoloCCtoPLoanMonthlyResponse  reportResponse =  new CrossSellSoloCCtoPLoanMonthlyResponse();

            if (customer.getAction().equals("PLOAN_FULL_SUBMIT")){
                    reportResponse.setFullSubmissionDateTime(dateFormat.format(customer.getCreatedOn()));
               CustomerActionTrail aipSubmitTrail =  actionTrailRepository.getMaxAIPSubmissionDateByDevice(customer.getDeviceId());


                if (aipSubmitTrail.getCreatedOn()!=null)
                    reportResponse.setAipSubmissionDateTime(dateFormat.format(aipSubmitTrail.getCreatedOn()));
                else{
                    reportResponse.setAipSubmissionDateTime("");
                }



            }
            else
                reportResponse.setAipSubmissionDateTime(dateFormat.format(customer.getCreatedOn()));

            reportResponse.setApplicationNRIC(customerProfile.getIdNo());
            reportResponse.setApplicationMobileNumber(customerProfile.getMobileNo());
            reportResponse.setFirstLoginDateTime(firstLoginDate);
            reportResponse.setApplicationStartDate(applyNowDate);

            response.add(reportResponse);
        });

        return response;
    }

    public List<CrossSellPLoanToSPMonthlyResponse> getCrossCrossSellPLoanToSPReportData(BaseReportInfoRequest request) throws JSONException, IOException {
        List<CrossSellPLoanToSPMonthlyResponse>  response = new ArrayList<>();
        QueryCondition[] conditions;
        String [] campaignName = new String[] {SystemParam.getInstance().getEkycPloanCrossSellCampaignName().get(0)};
        Integer campaignId = queryUtil.getCampaignIdsByName(campaignName[0]).get(0);
        request.setCampaignName(campaignName);


        conditions = new QueryCondition[]{};
        request.setTableType(CampaignJourney.class);
        request.setCampaignId(campaignId);

        List<CampaignJourney> campaignJourneys= ( List<CampaignJourney>) queryUtil.selectAllByConditions(conditions,request);

        for (CampaignJourney campaignJourney: campaignJourneys){

            CustomerProfileEFormResponse customerProfileResponse =customerProfileService.getCustomerProfileEformID(campaignJourney.getReferenceId());

            String dateComplete =  getMinClickApplyNowDate(
                    "device_id",
                    customerProfileResponse.getDeviceUuid(),
                    "EKYC_SP");

            if (customerProfileResponse!= null){
                if (customerProfileResponse.getMobile() == null)
                    customerProfileResponse.setMobile("");
                if (customerProfileResponse.getIdNo()== null)
                    customerProfileResponse.setIdNo("");
                if (customerProfileResponse.getCompletedOn() != null)
                    dateComplete=dateFormat.format(customerProfileResponse.getCompletedOn());

                response.add(new CrossSellPLoanToSPMonthlyResponse(
                        customerProfileResponse.getMobile(),
                        customerProfileResponse.getIdNo(),
                        getFirstPromptPLoanTqPage("device_id",customerProfileResponse.getDeviceUuid()),
                        dateComplete,
                        customerProfileResponse.getStatusCode()
                ));


            }





        }
        return response;
    }


//Referral Data Daily

    public List<MasterPromoCodeResponse> getMasterPromoCodeListing(BaseReportInfoRequest request){
        QueryCondition queryCondition [] = new QueryCondition[]{};


        request.setTableType(ReferralCode.class);
        request.setStartDate(null);


        List<ReferralCode> referralCodeList = (List<ReferralCode>) queryUtil.selectAllByConditions(queryCondition,request);

      return  referralCodeList.stream()
              .map(item-> new MasterPromoCodeResponse(item))
              .collect(Collectors.toList());

    }

    //Merge complete and pending records, if (same Referee/Referrer) from pending and complete
    // pending will not add to complete:
    public List<ReferralFulfillmentReportResponse> getAllReferralReportData(BaseReportInfoRequest request){
        List<ReferralFulfillmentReportResponse> completedList =getCompletedReferralReportData(request);

        List<ReferralFulfillmentReportResponse> pendingList = getPendingReferralReportData(request);

        if (completedList.size()==0)
            completedList.addAll(pendingList);
        else
            pendingList.forEach(item->{
            if (item.getReferrerNric()!=null && !item.getReferrerNric().isEmpty()) {
                if (completedList.stream().anyMatch(completeItem -> {
                     if (item.getRefereeNric() == null || item.getReferrerNric() == null)
                        return true;
                    return
                            !(completeItem.getRefereeNric().equals(item.getRefereeNric()) &&
                                    completeItem.getReferrerNric().equals(item.getReferrerNric()));
                }))
                    completedList.add(item);
            }
        });

        return completedList;
    }

    public List<ReferralFulfillmentReportResponse> getCompletedReferralReportData(BaseReportInfoRequest request){
        List<ReferralFulfillmentReportResponse> response = new ArrayList<>();

        for (String campaignName : request.getCampaignName()){
            Integer currentId = queryUtil.getCampaignIdsByName(campaignName).get(0);

            //GET Referral History
            List<ReferralHistory> histories = referralRepository.getByRefereeInitiationDate(request.getStartDate(),request.getEndDate(),currentId);

            histories.forEach(history->{
                ReferralFulfillmentReportResponse currentResponse = new ReferralFulfillmentReportResponse(history);

                String deviceID = AESEncyptionUtil.decrypt(history.getReferrerDeviceUuid());
                String refereeDeviceId = AESEncyptionUtil.decrypt(history.getReferreeDeviceUuid());

                //GET ISSUED DATE
                //GET MAX DateTime of referral attempted (REFERRER CLICK ON REFER NOW)
                currentResponse.setReferrerDateTimeIssued(getMinReferNow("device_id",deviceID,currentId));

                //GET ATTEMPTED DATE (REFEREE ATTEMPT TO CLICK APPLY NOW)

                currentResponse.setReferrerDateTimeAttempted(getMaxAttemptedDate("device_id",refereeDeviceId,history.getCreatedOn()));

                if (history.getCreatedOn()!=null)
                    currentResponse.setReferrerDateTimeSuccess(dateFormat.format(history.getCreatedOn()));

                currentResponse.setRefereeApplicationStatus("COMPLETED");
                response.add(currentResponse);
            });
        }
            return response;
    }

    public List<ReferralFulfillmentReportResponse> getPendingReferralReportData(BaseReportInfoRequest request){
        List<ReferralFulfillmentReportResponse> response = new ArrayList<>();


        request.setTableType(CustomerActionTrail.class);
        final QueryCondition [] conditions = {
                new QueryCondition("event","EKYC_SP",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action","CREATE_APPLICATION",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","EKYC_OTP",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","PENDING",CriteriaDefinition.Operators.EQ)
        };

        List <CustomerActionTrail> actionTrails = (List <CustomerActionTrail>)queryUtil.selectAllByConditions(conditions,request);

        Integer currentId = queryUtil.getCampaignIdsByName(request.getCampaignName()[0]).get(0);

        actionTrails.forEach(record->{
           ReferralFulfillmentReportResponse reportResponse = new ReferralFulfillmentReportResponse();
           final BaseReportInfoRequest noDateRequest = new BaseReportInfoRequest();
           CustomerProfileResponse referrerProfile =null;
           String eformID = "";
           String promoCode = "";

           if (record.getCifNo() !=null){
               String []dataArr = record.getCifNo().split("\\|");
                if (dataArr.length ==2){
                    eformID = dataArr[0];
                    promoCode = dataArr[1];
                }
                else {
                    eformID =dataArr[0];
                }
           }

            if (promoCode.isEmpty()==false){


           //Referee (The One Use Code)
            try {
              CustomerProfileEFormResponse refereeProfile =  customerProfileService.getCustomerProfileEformID(eformID);

              reportResponse.setRefereeAccountNo("");

              if (refereeProfile!=null){
                  reportResponse.setRefereeName(refereeProfile.getFullName());
                  reportResponse.setRefereeMobileNo(refereeProfile.getMobile());
                  reportResponse.setRefereeNric(refereeProfile.getIdNo());
                  reportResponse.setRefereeApplicationStatus("PENDING");
              }

              reportResponse.setReferrerDateTimeAttempted(dateFormat.format(record.getCreatedOn()));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Referrer (The One give code)
            noDateRequest.setTableType(ReferralCode.class);

            final QueryCondition[] referrerQuery = {
                    new QueryCondition("codevalue",promoCode,CriteriaDefinition.Operators.EQ),
                    new QueryCondition("campaignid",currentId,CriteriaDefinition.Operators.EQ)
            };
            List<ReferralCode> referralCode = ((List<ReferralCode>)queryUtil.selectAllByConditions(referrerQuery,noDateRequest));

            if (referralCode.size() > 0){
                if (referralCode.get(0).getCifNo()!= null) {

                    referrerProfile = customerProfileService.checkCustomerProfile(referralCode.get(0).getCifNo());
                    reportResponse.setReferralCode(promoCode);

                    if (referrerProfile != null) {
                        reportResponse.setReferrerNric(referrerProfile.getIdNo());
                        reportResponse.setReferrerMobileNo(referrerProfile.getMobile());
                        reportResponse.setReferrerName(referrerProfile.getFullName());

                        if (referrerProfile.getDeviceUuid()!=null)
                            reportResponse.setReferrerDateTimeIssued(getMinReferNow("device_id", referrerProfile.getDeviceUuid(),currentId));
                    }
                    reportResponse.setReferrerDateTimeSuccess("");
                }
            }
            if (reportResponse.getReferrerNric()!=null){
                if (reportResponse.getRefereeNric()!= null || reportResponse.getRefereeMobileNo()!=null)
                    response.add(reportResponse);
            }
            }
        });


        return response;
    }

//Referral Data Whatsapp Chatbot
    public List<WhatsappReferralReportResponse> getWAReferralReportData(BaseReportInfoRequest request){
        List<WhatsappReferralReportResponse> response = new ArrayList<>();

        for (String campaignName : request.getCampaignName()){
            Integer currentId = queryUtil.getCampaignIdsByName(campaignName).get(0);

            List<ReferralHistory> histories = referralRepository.getByRefereeInitiationDate(request.getStartDate(),request.getEndDate(),currentId);
            histories.forEach(history->{
                response.add(new WhatsappReferralReportResponse(history));
            });


        }
        return response;
    }

    //Supporting Functions
    private QueryCondition[] getNonTriggeredQuery(){
        return new QueryCondition[]{
                new QueryCondition("is_triggered",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("trigger_status","NON_TRIGGER",CriteriaDefinition.Operators.EQ)
        };

    }

    private QueryCondition[] getNonTriggeredORCompletedQuery(){
       return new QueryCondition[]{
                new QueryCondition("is_triggered",true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("trigger_status","NON_TRIGGER",CriteriaDefinition.Operators.EQ),
                new QueryCondition("trigger_status","COMPLETED",CriteriaDefinition.Operators.EQ)
        };
    }

    private String getMinClickApplyNowDate(String idType, String id, String action){
        if (id==null)
            return "";

        BaseReportInfoRequest noDateRequest = new BaseReportInfoRequest();
        noDateRequest.setTableType(CustomerActionTrail.class);

        final QueryCondition[] applicationStartDateCondition= new QueryCondition[]{
                new QueryCondition("action",action,CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","IN_APP_MSG_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
                new QueryCondition(idType,id,CriteriaDefinition.Operators.EQ)
        };

        List<CustomerActionTrail> result = (List<CustomerActionTrail>) queryUtil.selectColumnsByConditions(
                applicationStartDateCondition,noDateRequest,"created_on");

        if (result.isEmpty())
            return "";

        Optional<Date> maxDate = result.stream()
                .map(CustomerActionTrail::getCreatedOn)
                .min( Date::compareTo);


            if (maxDate!=null)
                return dateFormat.format(maxDate.get());

            return "";
    }

    private String getMaxClickApplyNowDate(String idType, String id, String action,Date dateBefore){
        if (id==null)
            return "";

        BaseReportInfoRequest noDateRequest = new BaseReportInfoRequest();

        noDateRequest.setTableType(CustomerActionTrail.class);

        final QueryCondition[] applicationStartDateCondition= new QueryCondition[]{
                new QueryCondition("action",action,CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","IN_APP_MSG_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","TAPPED",CriteriaDefinition.Operators.EQ),
                new QueryCondition("created_on",dateBefore, CriteriaDefinition.Operators.LTE),
                new QueryCondition(idType,id,CriteriaDefinition.Operators.EQ),
        };


        List<CustomerActionTrail> result = (List<CustomerActionTrail>) queryUtil.selectColumnsByConditions(
                applicationStartDateCondition,noDateRequest,"created_on");

        if (result.isEmpty())
            return "";

        Optional<Date> maxDate = result.stream()
                .map(CustomerActionTrail::getCreatedOn)
                .max( Date::compareTo);



        if (maxDate!=null)
            return dateFormat.format(maxDate.get());

        return "";
    }

    private String getMinReferNow(String idType, String id,Integer campaignID ){
        if (id==null)
            return "";

        BaseReportInfoRequest noDateRequest = new BaseReportInfoRequest();
        noDateRequest.setTableType(CustomerActionTrail.class);

        final QueryCondition[] issuedCondition = new QueryCondition[]{
                new QueryCondition("action", "OPEN_SS", CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page", "IN_APP_MSG_PAGE", CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status", "TAPPED", CriteriaDefinition.Operators.EQ),
                new QueryCondition("campaign_id",campaignID, CriteriaDefinition.Operators.EQ),
                new QueryCondition(idType,id, CriteriaDefinition.Operators.EQ)
        };

        List<CustomerActionTrail> result = (List<CustomerActionTrail>) queryUtil.selectColumnsByConditions(
                issuedCondition,noDateRequest,"created_on");

        if (result.isEmpty())
            return "";

        Optional<Date> maxDate = result.stream()
                .map(CustomerActionTrail::getCreatedOn)
                .min( Date::compareTo);



        if (maxDate!=null)
            return dateFormat.format(maxDate.get());
        return "";
    }

    private String getMaxAttemptedDate(String idType, String id,Date dateBefore){
        if (id==null)
            return "";

        BaseReportInfoRequest noDateRequest = new BaseReportInfoRequest();
        noDateRequest.setTableType(CustomerActionTrail.class);

        final QueryCondition [] conditions = {
                new QueryCondition("event","EKYC_SP",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action","CREATE_APPLICATION",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","EKYC_OTP",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","PENDING",CriteriaDefinition.Operators.EQ),
                new QueryCondition("created_on",dateBefore, CriteriaDefinition.Operators.LT),
                new QueryCondition(idType,id,CriteriaDefinition.Operators.EQ)
        };

        List<CustomerActionTrail> result = (List<CustomerActionTrail>) queryUtil.selectColumnsByConditions(
                conditions,noDateRequest,"created_on");

        if (result.isEmpty())
            return "";

        Optional <Date> maxDate = result.stream()
                .map(CustomerActionTrail::getCreatedOn)
                .max( Date::compareTo);

        if (maxDate.isPresent())
            return dateFormat.format(maxDate.get());
        return "";
    }

    private String getMinFirstLoginDate(String deviceId){
        if(deviceId==null)
            return "";

        BaseReportInfoRequest request = new BaseReportInfoRequest();
        request.setTableType(CustomerActionTrail.class);

        final QueryCondition[] firstLoginDateCondition= new QueryCondition[]{
                new QueryCondition("is_login", true,CriteriaDefinition.Operators.EQ),
                new QueryCondition("device_id",deviceId,CriteriaDefinition.Operators.EQ)
        };

        List<CustomerActionTrail> result = (List<CustomerActionTrail>) queryUtil.selectColumnsByConditions(
                firstLoginDateCondition,request,"created_on");

        if (result.isEmpty())
            return "";

        Optional<Date> minDate = result.stream()
                .map(CustomerActionTrail::getCreatedOn)
                .min( Date::compareTo);

        if (minDate.isPresent()){
                return dateFormat.format(minDate.get());
        }

        return "";
    }

    private String getFirstPromptPLoanTqPage(String idType, String id){

        if (id==null)
            return "";

        BaseReportInfoRequest noDateRequest = new BaseReportInfoRequest();
        noDateRequest.setTableType(CustomerActionTrail.class);

        final QueryCondition [] conditions = {
                new QueryCondition("action","PLOAN_FULL_SUBMIT",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_page","PLOAN_TQ_PAGE",CriteriaDefinition.Operators.EQ),
                new QueryCondition("action_status","SHOWED",CriteriaDefinition.Operators.EQ),
                new QueryCondition(idType,id,CriteriaDefinition.Operators.EQ)
        };

        List<CustomerActionTrail> result = (List<CustomerActionTrail>) queryUtil.selectColumnsByConditions(
                conditions,noDateRequest,"created_on");

        if (result.isEmpty())
            return "";

        Optional<Date> maxDate = result.stream()
                .map(CustomerActionTrail::getCreatedOn)
                .min(Date::compareTo);

        if (maxDate.isPresent())
            return dateFormat.format(maxDate.get());
        return "";

    }




}
