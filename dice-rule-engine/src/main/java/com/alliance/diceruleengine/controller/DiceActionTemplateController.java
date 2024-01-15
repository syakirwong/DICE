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
import com.alliance.diceruleengine.model.DiceActionTemplate;
import com.alliance.diceruleengine.request.CreateDiceActionTemplateRequest;
import com.alliance.diceruleengine.service.DiceActionTemplateService;

@Controller
public class DiceActionTemplateController {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DiceActionTemplateService diceActionTemplateService;

    @GetMapping("/diceActionTemplate")
    public ResponseEntity<ApiResponse> getAllDiceActionTemplate() throws ServiceException {
        ApiResponse apiResponse = new ApiResponse(ApiResponse.HTTP_RESPONSE_OK, true,
                messageSource.getMessage("common.request.success", null, Locale.getDefault()),
                diceActionTemplateService.getAllDiceActionTemplate());
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/diceActionTemplate/{id}")
    public ResponseEntity<DiceActionTemplate> getDiceActionTemplate(@PathVariable Integer id) {
        Optional<DiceActionTemplate> diceAction = diceActionTemplateService.getTemplate(id);
        if (diceAction.isPresent()) {
            return ResponseEntity.ok(diceAction.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/createDiceActionTemplate")
    public ResponseEntity<DiceActionTemplate> createEngagementModeTemplate(
            @RequestBody CreateDiceActionTemplateRequest request) {
        DiceActionTemplate template = diceActionTemplateService
                .createDiceActionTemplate(request.getDiceActionName());
        return ResponseEntity.ok(template);
    }
}
