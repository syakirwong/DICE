package com.alliance.dicecontentmanagement.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.dicecontentmanagement.model.MessageTemplate;
import com.alliance.dicecontentmanagement.repository.MessageTemplateRepository;
import com.alliance.dicecontentmanagement.request.CreateMessageTemplateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageTemplateService {

    @Autowired
    private MessageTemplateRepository messageTemplateRepository;

    public MessageTemplate createMessageTemplate(CreateMessageTemplateRequest messageTemplateRequest) {

        log.debug("MessageTemplateRequest: {}", messageTemplateRequest);

        Integer messageTemplateId = (messageTemplateRepository.getMaxMessageTemplateId() != null) ? messageTemplateRepository.getMaxMessageTemplateId() + 1
                : 1;

        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateId(messageTemplateId);
        messageTemplate.setDescription(messageTemplateRequest.getDescription());
        messageTemplate.setTitle(messageTemplateRequest.getTitle());
        messageTemplate.setLanguage(messageTemplateRequest.getLanguage());
        messageTemplate.setContent(messageTemplateRequest.getContent());
        messageTemplate.setButtonIds(messageTemplateRequest.getButtonIds());
        messageTemplate.setMessageTemplateName(messageTemplateRequest.getMessageTemplateName());
        messageTemplate.setCommunicationChannel(messageTemplateRequest.getCommunicationChannel());
        messageTemplate.setCreatedOn(new Date());

        return messageTemplateRepository.save(messageTemplate);

    }

    public Optional<MessageTemplate> getMessageTemplate(Integer messageTemplateId) {
        return messageTemplateRepository.findById(messageTemplateId);
    }

    public List<MessageTemplate> getAllMessageTemplates() {
        return messageTemplateRepository.findAll();
    }
}
