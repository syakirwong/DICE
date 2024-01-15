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
import com.alliance.dicecontentmanagement.model.ButtonList;
import com.alliance.dicecontentmanagement.request.CreateButtonListRequest;
import com.alliance.dicecontentmanagement.service.ButtonListService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ButtonListController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    ButtonListService buttonListService;

    @PostMapping("/templates/button-list/create")
    public ResponseEntity<ApiResponse> createButtonList(
            @Validated @RequestBody CreateButtonListRequest buttonListRequest)
            throws ServiceException {
        log.info("start - createButtonList: {}", buttonListRequest);
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_CREATED, true,
                messageSource.getMessage("add.button.list.success", null, Locale.getDefault()));
        buttonListService.addButton(buttonListRequest);

        return ResponseEntity.created(null).body(apiResponse);
    }

    @GetMapping("/templates/button-list/{id}")
    public ResponseEntity<ButtonList> getButton(@PathVariable Integer id) {
        Optional<ButtonList> optionalButton = buttonListService.getButton(id);
        if (optionalButton.isPresent()) {
            return ResponseEntity.ok(optionalButton.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/templates/button-list")
    public List<ButtonList> getAllButton() {
        return buttonListService.getAllButton();
    }

}
