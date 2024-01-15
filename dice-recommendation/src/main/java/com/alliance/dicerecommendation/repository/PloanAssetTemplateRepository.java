package com.alliance.dicerecommendation.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.dicerecommendation.model.PloanAssetTemplate;

public interface PloanAssetTemplateRepository extends CassandraRepository<PloanAssetTemplate, Integer> {
    @Query("SELECT * FROM EVT_PLOAN_ASSET_TEMPLATE WHERE ploanassettemplateid = :ploanAssetTemplateId ALLOW FILTERING")
    PloanAssetTemplate getPloanAssetTemplateViaId(Integer ploanAssetTemplateId);
}
