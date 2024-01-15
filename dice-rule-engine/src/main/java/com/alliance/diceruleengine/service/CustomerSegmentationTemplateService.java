package com.alliance.diceruleengine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliance.diceruleengine.constant.ApiResponse;
import com.alliance.diceruleengine.constant.DataField.Status;
import com.alliance.diceruleengine.exception.ServiceException;
import com.alliance.diceruleengine.model.CustomerSegmentationTemplate;
import com.alliance.diceruleengine.repository.CustomerSegmentationTemplateRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerSegmentationTemplateService {
    @Autowired
    private CustomerSegmentationTemplateRepository customerSegmentationRepository;

    public CustomerSegmentationTemplate getCustomerSegmentationTemplateById(Integer customerSegmentationTemplateId)
            throws ServiceException {
        // log.info("getCustomerSegmentationTemplateById - id : {}",customerSegmentationTemplateId.toString());
        try {
            CustomerSegmentationTemplate customerSegmentationTemplate = customerSegmentationRepository
                    .getCustomerSegmentationByCustomerSegmentationTemplateId(customerSegmentationTemplateId);
            log.info("getCustomerSegmentationTemplateById - id : {}, customerSegmentationTemplate : {}",customerSegmentationTemplateId.toString(), customerSegmentationTemplate.toString());
            return customerSegmentationTemplate;
        } catch (Exception ex) {
            log.error("getCustomerSegmentationTemplate - Exception : {}", ex);
            throw new ServiceException(ApiResponse.HTTP_RESPONSE_INTERNAL_SERVER_ERROR, ex.toString());
        }

    }

    public List<CustomerSegmentationTemplate> getAllCustomerSegmentationTemplate() {
        return customerSegmentationRepository.findAll();
    }

    public CustomerSegmentationTemplate createCustomerSegmentationTemplate(
            String customerSegmentType,
            String customerSegmentValue,
            String customerSegmentBehaviour,
            String description) {

        Integer customerSegmentationTemplateId = (customerSegmentationRepository
                .getMaxCustomerSegmentationTemplateId() != null)
                        ? customerSegmentationRepository.getMaxCustomerSegmentationTemplateId() + 1
                        : 1;

        CustomerSegmentationTemplate template = new CustomerSegmentationTemplate();
        template.setCustomerSegmentationTemplateId(customerSegmentationTemplateId);
        template.setCustomerSegmentType(customerSegmentType);
        template.setCustomerSegmentValue(customerSegmentValue);
        template.setCustomerSegmentBehaviour(customerSegmentBehaviour);
        template.setDescription(description);
        template.setStatus(Status.ACTIVE);

        return customerSegmentationRepository.save(template);
    }

}
