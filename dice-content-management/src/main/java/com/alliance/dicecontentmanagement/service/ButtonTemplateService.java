package com.alliance.dicecontentmanagement.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.dicecontentmanagement.model.ButtonTemplate;
import com.alliance.dicecontentmanagement.repository.ButtonTemplateRepository;
import com.alliance.dicecontentmanagement.request.CreateButtonTemplateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ButtonTemplateService {

    @Autowired
    private ButtonTemplateRepository buttonTemplateRepository;

    public ButtonTemplate createButtonTemplate(CreateButtonTemplateRequest buttonTemplateRequest) {
        log.debug("ButtonTemplateRequest: {}", buttonTemplateRequest);

        Integer buttonTemplateId = (buttonTemplateRepository.getMaxButtonTemplateId() != null)
                ? buttonTemplateRepository.getMaxButtonTemplateId() + 1
                : 1;

        log.debug("BUtton template ID {}", buttonTemplateId);

        ButtonTemplate buttonTemplate = new ButtonTemplate();
        buttonTemplate.setButtonTemplateId(buttonTemplateId);
        buttonTemplate.setTemplateStatus(buttonTemplateRequest.getStatus());
        buttonTemplate.setContent(buttonTemplateRequest.getContent());
        buttonTemplate.setButtonTemplateName(buttonTemplateRequest.getButtonTemplateName());
        buttonTemplate.setButtonType(buttonTemplateRequest.getButtonType());
        buttonTemplate.setCreatedOn(new Date());

        return buttonTemplateRepository.save(buttonTemplate);
    }

    public Optional<ButtonTemplate> getButtonTemplate(Integer buttonTemplateId) {
        return buttonTemplateRepository.findById(buttonTemplateId);
    }

    public List<ButtonTemplate> getAllButtonTemplates() {
        return buttonTemplateRepository.findAll();
    }
}
