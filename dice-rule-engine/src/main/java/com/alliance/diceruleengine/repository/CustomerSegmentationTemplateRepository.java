package com.alliance.diceruleengine.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceruleengine.model.CustomerSegmentationTemplate;

@Repository
public interface CustomerSegmentationTemplateRepository extends CassandraRepository<CustomerSegmentationTemplate, Integer> {
    public CustomerSegmentationTemplate getCustomerSegmentationByCustomerSegmentationTemplateId(Integer customerSegmentationTemplateId);

    @Query(("SELECT MAX(customerSegmentationTemplateId) FROM EVT_CUSTOMER_SEGMENTATION_TEMPLATE"))
    Integer getMaxCustomerSegmentationTemplateId();
}
