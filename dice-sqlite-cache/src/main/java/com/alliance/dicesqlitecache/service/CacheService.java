package com.alliance.dicesqlitecache.service;

import com.alliance.dicesqlitecache.model.*;
import com.alliance.dicesqlitecache.repository.*;
import com.alliance.dicesqlitecache.request.ProfileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class CacheService {

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private InternetBankingActivationRepository internetBankingActivationRepository;

    @Autowired
    private PloanApplicationRepository ploanApplicationRepository;

    @Autowired
    private SoleCcRepository soleCcRepository;

    private VccOnboardingFormsRepository vccOnboardingFormsRepository;


    @Autowired
    private JdbcTemplate sqliteJdbcTemplate;


    private ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "cacheEntries", key = "#key")
    public String getCacheEntry(String key) {

        Optional<CacheEntity> cacheEntityOptional = cacheRepository.findById(key);
        if (cacheEntityOptional.isPresent()) {
            CacheEntity cacheEntity = cacheEntityOptional.get();
            log.info("DB hit for key: {}", key);
            try {
                return objectMapper.writeValueAsString(cacheEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Cacheable(value = "internetBankingActivationEntries", key = "#key")
    public String getCacheEntryFromInternetBankingTable(String key) {

        Optional<InternetBankingActivationEntity> internetBankingActivationEntityOptional = internetBankingActivationRepository
                .findById(key);
        if (internetBankingActivationEntityOptional.isPresent()) {
            InternetBankingActivationEntity cacheEntity = internetBankingActivationEntityOptional.get();
            log.info("DB hit for key: {}", key);
            try {
                return objectMapper.writeValueAsString(cacheEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Cacheable(value = "ploanApplicationEntries", key = "#key")
    public String getCacheEntryFromPloanApplicationTable(String key) {
        Optional<PloanApplicationEntity> ploanApplicationEntityOptional = ploanApplicationRepository.findById(key);
        if (ploanApplicationEntityOptional.isPresent()) {
            PloanApplicationEntity cacheEntity = ploanApplicationEntityOptional.get();
            log.info("DB hit for key: {}", key);
            try {
                return objectMapper.writeValueAsString(cacheEntity);
            } catch (Exception e) {
                log.error("Error converting Ploan Application cache entity to JSON: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Cacheable(value = "soleCcEntries", key = "#key")
    public String getCacheEntryFromSoleCcTable(String key, boolean isCifNo, boolean isUserId) {
        Optional<SoleCcEntity> soleCcEntityOptional;
        if (isCifNo) {
            soleCcEntityOptional = soleCcRepository.findByCifNo(key);

        } else if (isUserId) {
            soleCcEntityOptional = soleCcRepository.findByUserId(key);

        } else {
            soleCcEntityOptional = soleCcRepository.findById(key);
        }

        if (soleCcEntityOptional.isPresent()) {
            SoleCcEntity cacheEntity = soleCcEntityOptional.get();
            log.info("DB hit for key: {}", key);
            try {
                return objectMapper.writeValueAsString(cacheEntity);
            } catch (Exception e) {
                log.error("Error converting Sole CC cache entity to JSON: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Cacheable(value = "vccOnboardingFormsEntries", key = "#key")
    public String getCacheEntryFromVccOnboardingFormsTable(String key) {
        Optional<VccOnboardingFormsEntity> vccOnboardingFormsEntityOptional = vccOnboardingFormsRepository
                .findById(key);
        if (vccOnboardingFormsEntityOptional.isPresent()) {
            VccOnboardingFormsEntity cacheEntity = vccOnboardingFormsEntityOptional.get();
            log.info("DB hit for key: {}", key);
            try {
                return objectMapper.writeValueAsString(cacheEntity);
            } catch (Exception e) {
                log.error("Error converting VCC Onboarding Forms cache entity to JSON: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Cacheable(value = "profileEntries", key = "#profileRequest.idType +'-'+ #profileRequest.idValue + '-' + #profileRequest.tableName")
    public Map<String, Object> getProfileById(ProfileRequest profileRequest) {
        log.info("Start - getProfileById service");
        try {
            String sqlQuery = "SELECT * FROM {tableName} WHERE {idType}='{idValue}'; "
                    .replace("{tableName}", profileRequest.getTableName())
                    .replace("{idType}", profileRequest.getIdType())
                    .replace("{idValue}", profileRequest.getIdValue());
    
            List<Map<String, Object>> result = sqliteJdbcTemplate.queryForList(sqlQuery);

            if (result != null && result.size()!=0) {
                log.info("getProfileById service - Profile data retrieved successfully");
                return Collections.singletonMap("data", result.get(result.size()-1));
            } else {
                log.error("getProfileById service - No profile data found");
                return Collections.emptyMap();
            }
        } catch (Exception ex) {
            log.error("getProfileById service - Exception : {}", ex);
            // Return an empty map to indicate an error occurred during data retrieval
            return Collections.emptyMap();
        }
    }
    



    public void saveCacheEntry(String key, String value, String sourceDatabase, String sourceTable) {
        try {
            CacheEntity cacheEntity = new CacheEntity();
            cacheEntity.setKey(key);
            cacheEntity.setValue(value);
            cacheEntity.setSourceDatabase(sourceDatabase);
            cacheEntity.setSourceTable(sourceTable);

            log.info(cacheRepository.save(cacheEntity).toString());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }
}
