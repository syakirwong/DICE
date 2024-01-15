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
import com.alliance.dicecontentmanagement.model.ButtonStyleTemplate;
import com.alliance.dicecontentmanagement.request.CreateButtonStyleTemplateRequest;
import com.alliance.dicecontentmanagement.service.ButtonStyleTemplateService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ButtonStyleTemplateController {

    @Autowired
    private ButtonStyleTemplateService buttonStyleTemplateService;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/templates/button-style/create")
    public ResponseEntity<ApiResponse> createButtonStyleTemplate(
            @Validated @RequestBody CreateButtonStyleTemplateRequest buttonStyleTemplateRequest)
            throws ServiceException {
        log.info("start - createButtonStyleTemplate: {}", buttonStyleTemplateRequest);
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_CREATED, true,
                messageSource.getMessage("create.template.buttonStyle.success.add", null, Locale.getDefault()));
        buttonStyleTemplateService.createButtonStyleTemplate(buttonStyleTemplateRequest);

        return ResponseEntity.created(null).body(apiResponse);
    }

    @GetMapping("/templates/button-style/{id}")
    public ResponseEntity<ButtonStyleTemplate> getButtonStyleTemplate(@PathVariable Integer id) {
        Optional<ButtonStyleTemplate> optionalButtonStyleTemplate = buttonStyleTemplateService.getButtonStyleTemplate(id); 
        if (optionalButtonStyleTemplate.isPresent()) {
            return ResponseEntity.ok(optionalButtonStyleTemplate.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/templates/button-style") 
    public List<ButtonStyleTemplate> getAllButtonStyleTemplates() {
        return buttonStyleTemplateService.getAllButtonStyleTemplates();
    }
}
