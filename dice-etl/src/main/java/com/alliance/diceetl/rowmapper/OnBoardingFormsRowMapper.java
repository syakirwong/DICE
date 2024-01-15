package com.alliance.diceetl.rowmapper;

import com.alliance.diceetl.entity.OnBoardingForms;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OnBoardingFormsRowMapper implements RowMapper<OnBoardingForms> {

    @Override
    public OnBoardingForms mapRow(ResultSet rs, int rowNum) throws SQLException {
        OnBoardingForms onBoardingForms = new OnBoardingForms();
        onBoardingForms.setUuid(rs.getString("A.UUID"));
        onBoardingForms.setIdNo(rs.getString("ID_NO"));
        onBoardingForms.setMobileNo(rs.getString("MOBILE_NO"));
        onBoardingForms.setCustomerName(rs.getString("CUSTOMER_NAME"));
        onBoardingForms.setSubmittedOn(rs.getTimestamp("SUBMITTED_ON"));
        onBoardingForms.setPromoCode(rs.getString("PROMO_CODE"));
        onBoardingForms.setDeviceUuid("DEVICE_UUID");
        onBoardingForms.setDevicePlatform("DEVICE_PLATFORM");
        return onBoardingForms;
    }
}
