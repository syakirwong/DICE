package com.alliance.diceruleengine.service;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PloanService {

    private final JdbcTemplate db2JdbcTemplate;

    public PloanService(
            @Qualifier("db2JdbcTemplate") JdbcTemplate db2JdbcTemplate) {
        this.db2JdbcTemplate = db2JdbcTemplate;
    }

    public Map<String, Object> getCustomerPloan(String eformUUID) throws ServiceException {
        try {
            // String sql = "SELECT * FROM MOHDS.EMPLOYEE WHERE EMPNO = ?";
            String sql = "SELECT * FROM EFORM.PLOAN_APPLICATION_VIEW WHERE UUID = ?";
            List<Map<String, Object>> result = db2JdbcTemplate.queryForList(sql, eformUUID);
            return result.isEmpty() ? null : result.get(0);

        } catch (Exception ex) {
            log.error("getCustomerPloan - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    ex.toString());
        }

    }

    public List<Map<String, Object>> getAllCustomerPloan() throws ServiceException {
        try {
            String sql = "SELECT * FROM EFORM.PLOAN_APPLICATION_VIEW";
            List<Map<String, Object>> result = db2JdbcTemplate.queryForList(sql);
            return result.isEmpty() ? null : result;

        } catch (Exception ex) {
            log.error("getCustomerPloan - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    ex.toString());
        }

    }

    public  Map<String, Object>  getCustomerPLoanByDevice(String deviceID) throws ServiceException {
        try {
            // String sql = "SELECT * FROM MOHDS.EMPLOYEE WHERE EMPNO = ?";
            String sql = "SELECT * FROM EFORM.PLOAN_APPLICATION_VIEW WHERE DEVICE_UUID = ?";
            List<Map<String, Object>> result = db2JdbcTemplate.queryForList(sql, deviceID);
            return result.isEmpty() ? new HashMap<>() : result.get(0);

        } catch (Exception ex) {
            log.error("getCustomerPloan - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR,
                    ex.toString());
        }

    }
}
