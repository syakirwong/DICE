package com.alliance.diceetl.rowmapper;

import com.alliance.diceetl.entity.DdmSoleCc;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DdmSoleCcRowMapper implements RowMapper<DdmSoleCc> {

    @Override
    public DdmSoleCc mapRow(ResultSet rs, int rowNum) throws SQLException {
        DdmSoleCc ddmSoleCc = new DdmSoleCc();
        ddmSoleCc.setUserId(rs.getString("i.USERID"));
        ddmSoleCc.setCif(rs.getString("CIF"));
        ddmSoleCc.setNewIcNo(rs.getString("NEWICNO"));
        ddmSoleCc.setCustomerName(rs.getString("CUSTOMERNAME"));
        ddmSoleCc.setMobile(rs.getString("MOBILE"));
        ddmSoleCc.setPackageId(rs.getString("PACKAGEID"));
        ddmSoleCc.setDob(rs.getString("DOB"));
        ddmSoleCc.setGender(rs.getString("GENDER"));
        ddmSoleCc.setEmail(rs.getString("EMAIL"));
        ddmSoleCc.setNationality(rs.getString("NATIONALITY"));
        ddmSoleCc.setMobileFirstPlatformId(rs.getString("MOBILEFIRSTPLATFORMID"));
        ddmSoleCc.setDevicePlatform(rs.getString("DEVICEPLATFORM"));
        return ddmSoleCc;
    }
}
