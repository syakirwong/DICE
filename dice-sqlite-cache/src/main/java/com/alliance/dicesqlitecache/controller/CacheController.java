package com.alliance.dicesqlitecache.controller;

import com.alliance.dicesqlitecache.repository.InternetBankingActivationRepository;
import com.alliance.dicesqlitecache.request.CacheRequest;
import com.alliance.dicesqlitecache.request.ProfileRequest;
import com.alliance.dicesqlitecache.service.CacheDataLoader;
import com.alliance.dicesqlitecache.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheDataLoader cacheDataLoader;

    @GetMapping("/{key}")
    public String getCacheEntry(@PathVariable String key) {
        return cacheService.getCacheEntry(key);
    }

    @PostMapping("/{key}")
    public void saveCacheEntry(@PathVariable String key, @RequestBody CacheRequest request) {
        cacheService.saveCacheEntry(key, request.getValue(), request.getSourceDatabase(), request.getSourceTable());
    }

    @GetMapping("/get/internet-banking")
    public ResponseEntity<String> getInternetBankingCache(@RequestParam(name = "key") String key) {
        try {
            String result = cacheService.getCacheEntryFromInternetBankingTable(key);
            return ResponseEntity.ok("Cache entry for key " + key + ": " + result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to retrieve cache entry from Internet Banking table: " + e.getMessage());
        }
    }

    @GetMapping("/get/ploan-application")
    public ResponseEntity<String> getPloanApplicationCache(@RequestParam(name = "key") String key) {
        try {
            String result = cacheService.getCacheEntryFromPloanApplicationTable(key);
            return ResponseEntity.ok("Cache entry for key " + key + ": " + result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to retrieve cache entry from Ploan Application table: " + e.getMessage());
        }
    }

    @GetMapping("/get/sole-cc")
    public ResponseEntity<String> getSoleCcCache(@RequestParam(name = "key") String key,
            @RequestParam(name = "isCifNo", defaultValue = "false") boolean isCifNo,
            @RequestParam(name = "isUserId", defaultValue = "false") boolean isUserId) {
        try {
            String result = cacheService.getCacheEntryFromSoleCcTable(key, isCifNo, isUserId);
            return ResponseEntity.ok("Cache entry for key " + key + ": " + result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to retrieve cache entry from Sole CC table: " + e.getMessage());
        }
    }

    @GetMapping("/get/vcc-onboarding-forms")
    public ResponseEntity<String> getVccOnboardingFormsCache(@RequestParam(name = "key") String key) {
        try {
            String result = cacheService.getCacheEntryFromVccOnboardingFormsTable(key);
            return ResponseEntity.ok("Cache entry for key " + key + ": " + result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to retrieve cache entry from VCC Onboarding Forms table: " + e.getMessage());
        }
    }

    @PostMapping("/start")
    public ResponseEntity<String> startEvent(
            @RequestParam(name = "source", defaultValue = "all") String eventType) {
        try {
            cacheDataLoader.loadDataFromDB2(eventType);
            return ResponseEntity.ok("Cache start event initiated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to initiate cache refresh: " + e.getMessage());
        }
    }


    @PostMapping(value = "/getProfileById", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProfileById(@Valid @RequestBody ProfileRequest profileRequest) {
        try {
            log.info("start - getProfileById with Request: {}", profileRequest);
            Map<String, Object> result = cacheService.getProfileById(profileRequest);
            if (result != null && !result.isEmpty()) {
                return ResponseEntity.ok(result);
            } else {
                log.info("getProfileById - No profile data found for request : {}", profileRequest);
                return ResponseEntity.ok(null);
            }
        } catch (Exception ex) {
            log.error("getProfileById with Request {} - Exception: {}", profileRequest, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

}
