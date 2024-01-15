package com.alliance.dicerecommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.dicerecommendation.model.EngagementModeTemplate;
import com.alliance.dicerecommendation.repository.EngagementModeTemplateRepository;

@Service
public class EngagementModeTemplateService {

    @Autowired
    EngagementModeTemplateRepository engagementModeTemplateRepository;

    public List<EngagementModeTemplate> getAllEngagementModeTemplate() {
        return engagementModeTemplateRepository.findAll();
    }

    public Optional<EngagementModeTemplate> getTemplate(Integer templateId) {
        return engagementModeTemplateRepository.findById(templateId);
    }

    public EngagementModeTemplate createEngagementModeTemplate(
            String engagementModeName) {

        Integer engagmentModeTemplateId = (engagementModeTemplateRepository
                .getMaxEngagementModeTemplateId() != null)
                        ? engagementModeTemplateRepository.getMaxEngagementModeTemplateId() + 1
                        : 1;

        EngagementModeTemplate template = new EngagementModeTemplate();
        template.setEngagementModeTemplateId(engagmentModeTemplateId);
        template.setEngagementModeName(engagementModeName);

        return engagementModeTemplateRepository.save(template);
    }
}
