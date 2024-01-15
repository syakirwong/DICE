package com.alliance.diceruleengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.constant.DataField.RuleType;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.RuleTemplate;
import com.alliance.diceruleengine.repository.RuleTemplateRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RuleTemplateService {

    @Autowired
    RuleTemplateRepository ruleTemplateRepository;

    public RuleTemplate getRuleTemplateById(Integer ruleTemplateId) throws ServiceException {
        log.info("getRuleTemplateId -- {}", ruleTemplateId.toString());
        try {
            RuleTemplate ruleTemplate = ruleTemplateRepository
                    .getRuleTemplateByRuleTemplateId(ruleTemplateId);
            log.info(ruleTemplate.toString());
            return ruleTemplate;
        } catch (Exception ex) {
            log.error("getRuleTemplate - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }

    public RuleTemplate createRuleTemplate(
            RuleType ruleType,
            String description,
            String tableName,
            String key,
            String value) {

        Integer ruleTemplateId = (ruleTemplateRepository
                .getMaxRuleTemplateId() != null)
                        ? ruleTemplateRepository.getMaxRuleTemplateId() + 1
                        : 1;

        RuleTemplate template = new RuleTemplate();
        template.setRuleTemplateId(ruleTemplateId);
        template.setDescription(description);
        template.setRuleType(ruleType);
        template.setDescription(description);
        template.setTableName(tableName);
        template.setValue(value);

        return ruleTemplateRepository.save(template);
    }

}
