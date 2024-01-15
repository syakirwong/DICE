package com.alliance.dicerecommendation.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.dicerecommendation.model.PermaLinkTemplate;

public interface PermaLinkTemplateRepository extends CassandraRepository<PermaLinkTemplate, Integer> {
    
    @Query("SELECT * FROM EVT_PERMA_LINK_TEMPLATE WHERE permalinktemplateId = :permaLinkTemplateId ALLOW FILTERING")
    PermaLinkTemplate getPermaLinkTemplateViaId(Integer permaLinkTemplateId);
}
