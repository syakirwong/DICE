package com.alliance.diceruleengine.controller;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.EngagementModeTemplate;
import com.alliance.diceruleengine.request.CreateEngagementModeTemplate;
import com.alliance.diceruleengine.service.EngagementModeTemplateService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EngagementModeTemplateController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    EngagementModeTemplateService engagementModeTemplateService;

    @GetMapping("/engagementModeTemplate")
    public ResponseEntity<ApiResponse> getAllEngagementModeTemplate() throws ServiceException {
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                engagementModeTemplateService.getAllEngagementModeTemplate());
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/engagementModeTemplate/{id}")
    public ResponseEntity<EngagementModeTemplate> getEngagementModeTemplate(@PathVariable Integer id) {
        Optional<EngagementModeTemplate> engagementMode = engagementModeTemplateService.getTemplate(id);
        if (engagementMode.isPresent()) {
            return ResponseEntity.ok(engagementMode.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/createEngagementModeTemplate")
    public ResponseEntity<EngagementModeTemplate> createEngagementModeTemplate(
            @RequestBody CreateEngagementModeTemplate request) {
        EngagementModeTemplate template = engagementModeTemplateService
                .createEngagementModeTemplate(request.getEngagementModeName());
        return ResponseEntity.ok(template);
    }

}
