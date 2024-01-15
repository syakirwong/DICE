package com.alliance.diceetl.rowmapper;

import com.alliance.diceetl.entity.PloanApplication;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PloanApplicationRowMapper implements RowMapper<PloanApplication> {
    @Override
    public PloanApplication mapRow(ResultSet rs, int rowNum) throws SQLException {
        PloanApplication ploanApplication = new PloanApplication();
        ploanApplication.setUuid(rs.getString("e.UUID"));
        ploanApplication.setNricNo(rs.getString("NRIC_NO"));
        ploanApplication.setFullName(rs.getString("FULL_NAME"));
        ploanApplication.setPromoCode(rs.getString("PROMO_CODE"));
        ploanApplication.setMobileNo(rs.getString("MOBILE_NO"));
        ploanApplication.setDeviceUuid(rs.getString("DEVICE_UUID"));
        ploanApplication.setDevicePlatform(rs.getString("DEVICE_PLATFORM"));
        ploanApplication.setIsPdpaConsent(rs.getShort("IS_PDPA_CONSENT"));
        ploanApplication.setIsNta(rs.getShort("IS_NTA"));
        return ploanApplication;
    }
}
