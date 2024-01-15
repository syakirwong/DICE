package com.alliance.dicecontentmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.dicecontentmanagement.model.ButtonList;
import com.alliance.dicecontentmanagement.repository.ButtonListRepository;
import com.alliance.dicecontentmanagement.request.CreateButtonListRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ButtonListService {

    @Autowired
    private ButtonListRepository buttonListRepository;

    public ButtonList addButton(CreateButtonListRequest createButtonListRequest) {
        log.debug("buttonListRequest: {}", createButtonListRequest);

        Integer buttonId = (buttonListRepository.getMaxButtonListId() != null)
                ? buttonListRepository.getMaxButtonListId() + 1
                : 1;

        log.debug("Button List ID {}", buttonId);

        ButtonList button = new ButtonList();
        button.setButtonId(buttonId);
        button.setButtonInAppContentTemplateId(createButtonListRequest.getButtonInAppContentTemplateId());
        button.setButtonStyleTemplateId(createButtonListRequest.getButtonStyleTemplateId());
        button.setButtonTemplateId(createButtonListRequest.getButtonTemplateId());

        return buttonListRepository.save(button);
    }

    public Optional<ButtonList> getButton(Integer buttonId) {
        return buttonListRepository.findById(buttonId);
    }

    public List<ButtonList> getAllButton() {
        return buttonListRepository.findAll();
    }
}
