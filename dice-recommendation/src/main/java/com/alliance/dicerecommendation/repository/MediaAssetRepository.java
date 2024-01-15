package com.alliance.dicerecommendation.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.dicerecommendation.model.MediaAsset;

@Repository
public interface MediaAssetRepository extends CassandraRepository<MediaAsset, Integer> {
    @Query(("SELECT MEDIA_ASSET_PATH_URL FROM EVT_MEDIA_ASSET WHERE MEDIA_ASSET_ID = :mediaAssetId ALLOW FILTERING"))
    String getPathUrlByMediaAssetId(Integer mediaAssetId);

};

