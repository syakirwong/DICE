package com.alliance.diceruleengine.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.constant.DataField.Status;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.ReferralCode;
import com.alliance.diceruleengine.service.ReferralCodeService;
import com.ibm.db2.jcc.a.a.e;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ReferralCodeController {
    @Autowired
    MessageSource messageSource;

    @Autowired
    ReferralCodeService referralCodeService;

    @GetMapping("/referral")
    public ResponseEntity<ReferralCode> getReferralCode(
            @RequestParam(name = "referralCode", required = false) String referralCode,
            @RequestParam(name = "status", required = false) Status status) throws ServiceException {
        return ResponseEntity.ok(referralCodeService.getReferralCode(referralCode, status));
    }

    @GetMapping("/referralByUuid")
    public ResponseEntity<ReferralCode> getReferralCodeByUuid(
            @RequestParam(name = "uuidType", required = false) String uuidType,
            @RequestParam(name = "uuid", required = false) String uuid) throws ServiceException {
        ReferralCode referralCode = referralCodeService.getExistingCodeBasedOnUuid(uuidType, uuid);

        return ResponseEntity.ok(referralCode);

    }

}
