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
import com.alliance.dicecontentmanagement.model.MessageTemplate;
import com.alliance.dicecontentmanagement.request.CreateMessageTemplateRequest;
import com.alliance.dicecontentmanagement.service.MessageTemplateService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class MessageTemplateController {

    @Autowired
    private MessageTemplateService messageTemplateService;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/templates/message/create")
    public ResponseEntity<ApiResponse> createMessageTemplate(@Validated @RequestBody CreateMessageTemplateRequest messageTemplateRequest)
    throws ServiceException {
        log.info("start - createMessageTemplate: {}", messageTemplateRequest);
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_CREATED, true,
                messageSource.getMessage("create.template.message.success.add", null, Locale.getDefault()));
        messageTemplateService.createMessageTemplate(messageTemplateRequest);

        return ResponseEntity.created(null).body(apiResponse);

    }

    @GetMapping("/templates/message/{id}")
    public ResponseEntity<MessageTemplate> getMessageTemplate(@PathVariable Integer id) {
        Optional<MessageTemplate> optionalMessageTemplate = messageTemplateService.getMessageTemplate(id); 
        if (optionalMessageTemplate.isPresent()) {
            return ResponseEntity.ok(optionalMessageTemplate.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/templates/message") 
    public List<MessageTemplate> getAllMessageTemplates() {
        return messageTemplateService.getAllMessageTemplates();
    }
}
