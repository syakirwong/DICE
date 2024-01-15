package com.alliance.dicecontentmanagement.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.dicecontentmanagement.model.ButtonStyleTemplate;
import com.alliance.dicecontentmanagement.repository.ButtonStyleTemplateRepository;
import com.alliance.dicecontentmanagement.request.CreateButtonStyleTemplateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ButtonStyleTemplateService {

    @Autowired
    private ButtonStyleTemplateRepository buttonStyleTemplateRepository;

    public ButtonStyleTemplate createButtonStyleTemplate(CreateButtonStyleTemplateRequest buttonStyleTemplateRequest) {
        log.debug("ButtonStyleTemplateRequest: {}", buttonStyleTemplateRequest);

        Integer buttonStyleTemplateId = (buttonStyleTemplateRepository.getMaxButtonStyleTemplateId() != null)
                ? buttonStyleTemplateRepository.getMaxButtonStyleTemplateId() + 1
                : 1;

        log.debug("BUtton Style template ID {}", buttonStyleTemplateId);

        ButtonStyleTemplate buttonStyleTemplate = new ButtonStyleTemplate();
        buttonStyleTemplate.setButtonStyleTemplateId(buttonStyleTemplateId);
        buttonStyleTemplate.setTemplateStatus(buttonStyleTemplateRequest.getStatus());
        buttonStyleTemplate.setStyle(buttonStyleTemplateRequest.getStyle());
        buttonStyleTemplate.setButtonStyleTemplateName(buttonStyleTemplateRequest.getButtonStyleTemplateName());
        buttonStyleTemplate.setCreatedOn(new Date());

        return buttonStyleTemplateRepository.save(buttonStyleTemplate);
    }

    public Optional<ButtonStyleTemplate> getButtonStyleTemplate(Integer buttonStyleTemplateId) {
        return buttonStyleTemplateRepository.findById(buttonStyleTemplateId);
    }

    public List<ButtonStyleTemplate> getAllButtonStyleTemplates() {
        return buttonStyleTemplateRepository.findAll();
    }
}
