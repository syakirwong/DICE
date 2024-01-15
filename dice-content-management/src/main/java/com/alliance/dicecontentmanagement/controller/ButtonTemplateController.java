package com.alliance.dicecontentmanagement.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alliance.dicecontentmanagement.constant.ApiResponse;
import com.alliance.dicecontentmanagement.model.ButtonTemplate;
import com.alliance.dicecontentmanagement.request.CreateButtonTemplateRequest;
import com.alliance.dicecontentmanagement.service.ButtonTemplateService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ButtonTemplateController {

    @Autowired
    private ButtonTemplateService buttonTemplateService;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/templates/button/create")
    public ResponseEntity<ApiResponse> createButtonTemplate(
            @Validated @RequestBody CreateButtonTemplateRequest buttonTemplateRequest)
            throws ServiceException {
        log.info("start - createButtonTemplate: {}", buttonTemplateRequest);
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_CREATED, true,
                messageSource.getMessage("create.template.button.success.add", null, Locale.getDefault()));
        buttonTemplateService.createButtonTemplate(buttonTemplateRequest);

        return ResponseEntity.created(null).body(apiResponse);
    }

    @GetMapping("/templates/button/{id}")
    public ResponseEntity<ButtonTemplate> getButtonTemplate(@PathVariable Integer id) {
        Optional<ButtonTemplate> optionalButtonTemplate = buttonTemplateService.getButtonTemplate(id); 
        if (optionalButtonTemplate.isPresent()) {
            return ResponseEntity.ok(optionalButtonTemplate.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/templates/button")
    public List<ButtonTemplate> getAllButtonTemplates() {
        return buttonTemplateService.getAllButtonTemplates();
    }
}
