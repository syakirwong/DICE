package com.alliance.diceetl.rowmapper;

import com.alliance.diceetl.entity.InternetBankingActivation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InternetBankingActivationRowMapper implements RowMapper<InternetBankingActivation> {

    @Override
    public InternetBankingActivation mapRow(ResultSet rs, int rowNum) throws SQLException {
        InternetBankingActivation internetBankingActivation = new InternetBankingActivation();
        internetBankingActivation.setUuid(rs.getString("e.UUID"));
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
    }
}
