package com.alliance.dicesqlitecache.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alliance.dicesqlitecache.model.InternetBankingActivationEntity;
import com.alliance.dicesqlitecache.model.PloanApplicationEntity;
import com.alliance.dicesqlitecache.model.SoleCcEntity;
import com.alliance.dicesqlitecache.model.VccOnboardingFormsEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DB2DataService {

    @Autowired
    private JdbcTemplate db2JdbcTemplate;

    @Value("${internet.banking.sql.query}")
    private String DB2_INTERNET_BANKING_VIEW_QUERY;

    @Value("${ploan.application.sql.query}")
    private String DB2_PLOAN_APPLICATION_VIEW_QUERY;

    @Value("${ddm.solecc.sql.query}")
    private String DB2_DDM_SOLECC_VIEW_QUERY;

    @Value("${vcc.onboarding.forms.sql.query}")
    private String DB2_VCC_ONBOARDING_FORMS_VIEW_QUERY;

    public List<InternetBankingActivationEntity> fetchDataFromInternetBankingActivationDB2() {

        String sqlQuery = DB2_INTERNET_BANKING_VIEW_QUERY;

        try {
            return db2JdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
                InternetBankingActivationEntity internetBankingActivation = new InternetBankingActivationEntity();

                internetBankingActivation.setUuid(rs.getString("UUID"));
                internetBankingActivation.setIdNo(rs.getString("ID_NO"));
                internetBankingActivation.setFullName(rs.getString("FULL_NAME"));
                internetBankingActivation.setPromoCode(rs.getString("PROMO_CODE"));
                internetBankingActivation.setMobile(rs.getString("MOBILE"));
                internetBankingActivation.setDeviceUuid(rs.getString("DEVICE_UUID"));
                internetBankingActivation.setDevicePlatform(rs.getString("DEVICE_PLATFORM"));
                internetBankingActivation.setCompletedOn(rs.getTimestamp("COMPLETED_ON"));
                internetBankingActivation.setStatusCode(rs.getString("STATUS_CODE"));
                internetBankingActivation.setPdpaFlag(rs.getString("PDPA_FLAG"));
                return internetBankingActivation;
            });
        } catch (Exception e) {
            log.error("Error fetching data from DB2 (InternetBankingActivation): {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<PloanApplicationEntity> fetchDataFromPloanApplicationDB2() {

        String sqlQuery = DB2_PLOAN_APPLICATION_VIEW_QUERY;

        try {
            return db2JdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
                PloanApplicationEntity ploanApplication = new PloanApplicationEntity();

                ploanApplication.setUuid(rs.getString("UUID"));
                ploanApplication.setNricNo(rs.getString("NRIC_NO"));
                ploanApplication.setFullName(rs.getString("FULL_NAME"));
                ploanApplication.setPromoCode(rs.getString("PROMO_CODE"));
                ploanApplication.setMobileNo(rs.getString("MOBILE_NO"));
                ploanApplication.setDeviceUuid(rs.getString("DEVICE_UUID"));
                ploanApplication.setDevicePlatform(rs.getString("DEVICE_PLATFORM"));
                ploanApplication.setIsPdpaConsent(rs.getShort("IS_PDPA_CONSENT"));
                ploanApplication.setIsNta(rs.getShort("IS_NTA"));

                return ploanApplication;
            });
        } catch (Exception e) {
            log.error("Error fetching data from DB2 (PloanApplication): {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SoleCcEntity> fetchDataFromSoleCcDB2() {

        String sqlQuery = DB2_DDM_SOLECC_VIEW_QUERY;

        try {
            return db2JdbcTemplate.query(sqlQuery, (resultSet, rowNum) -> {
                SoleCcEntity entity = new SoleCcEntity();

                entity.setUserId(resultSet.getString("USERID"));
                entity.setCifNo(resultSet.getString("CIF"));
                entity.setNewIcNo(resultSet.getString("NEWICNO"));
                entity.setCustomerName(resultSet.getString("CUSTOMERNAME"));
                entity.setMobile(resultSet.getString("MOBILE"));
                entity.setPackageId(resultSet.getString("PACKAGEID"));
                entity.setDob(resultSet.getString("DOB"));
                entity.setGender(resultSet.getString("GENDER"));
                entity.setEmail(resultSet.getString("EMAIL"));
                entity.setNationality(resultSet.getString("NATIONALITY"));
                entity.setMobileFirstPlatformId(resultSet.getString("MOBILEFIRSTPLATFORMID"));
                entity.setDevicePlatform(resultSet.getString("DEVICEPLATFORM"));

                return entity;
            });
        } catch (Exception e) {
            log.error("Error fetching data from DB2 (SoleCc): {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<VccOnboardingFormsEntity> fetchDataFromVccOnboardingFormsDB2() {

        String sqlQuery = DB2_VCC_ONBOARDING_FORMS_VIEW_QUERY;

        try {
            return db2JdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
                VccOnboardingFormsEntity onBoardingForms = new VccOnboardingFormsEntity();

                onBoardingForms.setUuid(rs.getString("UUID"));
                onBoardingForms.setIdNo(rs.getString("ID_NO"));
                onBoardingForms.setMobileNo(rs.getString("MOBILE_NO"));
                onBoardingForms.setCustomerName(rs.getString("CUSTOMER_NAME"));
                onBoardingForms.setSubmittedOn(rs.getTimestamp("SUBMITTED_ON"));
                onBoardingForms.setPromoCode(rs.getString("PROMO_CODE"));
                onBoardingForms.setDeviceUuid("DEVICE_UUID");
                onBoardingForms.setDevicePlatform("DEVICE_PLATFORM");

                return onBoardingForms;
            });
        } catch (Exception e) {
            log.error("Error fetching data from DB2 (VccOnboardingForms): {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
