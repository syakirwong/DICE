package com.alliance.diceruleengine.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.diceruleengine.model.DiceActionTemplate;
import com.alliance.diceruleengine.repository.DiceActionTemplateRepository;

@Service
public class DiceActionTemplateService {

    @Autowired
    private DiceActionTemplateRepository diceActionTemplateRepository;

    public List<DiceActionTemplate> getAllDiceActionTemplate() {
        return diceActionTemplateRepository.findAll();
    }

    public Optional<DiceActionTemplate> getTemplate(Integer templateId) {
        return diceActionTemplateRepository.findById(templateId);
    }

    public DiceActionTemplate createDiceActionTemplate(
            String diceActionName) {

        Integer diceActionTemplateId = (diceActionTemplateRepository
                .getMaxDiceActionTemplateId() != null)
                        ? diceActionTemplateRepository.getMaxDiceActionTemplateId() + 1
                        : 1;

        DiceActionTemplate template = new DiceActionTemplate();
        template.setDiceActionTemplateId(diceActionTemplateId);
        template.setDiceActionName(diceActionName);

        return diceActionTemplateRepository.save(template);
    }
}
