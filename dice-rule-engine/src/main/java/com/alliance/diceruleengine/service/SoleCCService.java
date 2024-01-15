package com.alliance.diceruleengine.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.SoleCC;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SoleCCService {

    private final JdbcTemplate db1JdbcTemplate;

    public SoleCCService(
            @Qualifier("db1JdbcTemplate") JdbcTemplate db1JdbcTemplate) {
        this.db1JdbcTemplate = db1JdbcTemplate;
    }

    public SoleCC getCustomerSoleCC(String cifNo) throws ServiceException {
        try {
            String sql = "SELECT * FROM IBSADMIN.DDM_SOLE_CC_VIEW WHERE CIFNO = ?";
            return db1JdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SoleCC.class), cifNo).stream().findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            log.error("getCustomerSoleCC - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    ex.toString());
        }

    }

    public List<Map<String, Object>> getAllCustomerSoleCC() throws ServiceException {
        try {
            String sql = "SELECT * FROM IBSADMIN.DDM_SOLE_CC_VIEW LIMIT 10";
            List<Map<String, Object>> result = db1JdbcTemplate.queryForList(sql);
            return result.isEmpty() ? null : result;

        } catch (Exception ex) {
            log.error("getCustomerSoleCC - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    ex.toString());
        }

    }

    // public SoleCC createSoleCC(String cifNo, String userId, String idNo, String
    // fullName,
    // String mobile, String deviceUUID, String devicePlatform) {
    // SoleCC soleCC = new SoleCC();
    // soleCC.setCifNo(cifNo);
    // soleCC.setUserId(userId);
    // soleCC.setIdNo(idNo);
    // soleCC.setFullName(fullName);
    // soleCC.setMobile(mobile);
    // soleCC.setDeviceUUID(deviceUUID);
    // soleCC.setDevicePlatform(devicePlatform);

    // return soleCCRepository.save(soleCC);
    // }

}
