package com.alliance.diceintegration.repository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

// @Repository
// @Slf4j
// public class ProfileInfoRepository {
//     @PersistenceContext
//     private EntityManager entityManager;

//     public List<Object[]> findByUuid(String schema, String table, String eform_uuid) {
//         String sql = "SELECT * FROM " + schema + "." + table + " WHERE UUID IN ('" + eform_uuid + "')";
//         log.info("ProfileInfoRepository - findByUuid : {}", sql);
//         Query query = entityManager.createNativeQuery(sql);
//         return query.getResultList();
//     }

//     public List<Object[]> findByDeviceID(String schema, String table, String deviceID) {
//         String sql = "SELECT * FROM " + schema + "." + table + " WHERE DEVICE_UUID IN ('" + deviceID + "')";
//         log.info("ProfileInfoRepository - findByUuid : {}", sql);
//         Query query = entityManager.createNativeQuery(sql);
//         return query.getResultList();
//     }
// }

@Repository
@Slf4j
public class ProfileInfoRepository {
    @PersistenceContext(unitName = "dbobdbEntityManagerFactory")
    private EntityManager dbobdbEntityManager;
    
    @PersistenceContext(unitName = "vccdbEntityManagerFactory")
    private EntityManager vccdbEntityManager;

    // DBOBDB data source
    public List<Object[]> findByUuid(String schema, String table, String eform_uuid) {
        String sql = "SELECT * FROM " + schema + "." + table + " WHERE UUID IN ('" + eform_uuid + "')";
        log.info("ProfileInfoRepository - findByUuid : {}", sql);
        Query query = dbobdbEntityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    public List<Object[]> findByDeviceID(String schema, String table, String deviceID) {
        String sql = "SELECT * FROM " + schema + "." + table + " WHERE DEVICE_UUID IN ('" + deviceID + "')";
        log.info("ProfileInfoRepository - findByUuid : {}", sql);
        Query query = dbobdbEntityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    // VCCDB data source
    public List<Object[]> findByUuidViaVccDb(String schema, String table, String eform_uuid) {
        String sql = "SELECT * FROM " + schema + "." + table + " WHERE UUID IN ('" + eform_uuid + "')";
        log.info("ProfileInfoRepository - findByUuid : {}", sql);
        Query query = vccdbEntityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    public List<Object[]> findByDeviceIDViaVccDb(String schema, String table, String deviceID) {
        String sql = "SELECT * FROM " + schema + "." + table + " WHERE DEVICE_UUID IN ('" + deviceID + "')";
        log.info("ProfileInfoRepository - findByUuid : {}", sql);
        Query query = vccdbEntityManager.createNativeQuery(sql);
        return query.getResultList();
    }
}
