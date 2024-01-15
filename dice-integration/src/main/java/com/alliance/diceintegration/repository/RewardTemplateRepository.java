package com.alliance.diceintegration.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceintegration.model.RewardTemplate;

@Repository
public interface RewardTemplateRepository extends CassandraRepository<RewardTemplate, Integer> {
    @Query("SELECT * from EVT_REWARD_TEMPLATE WHERE rewardtemplateid =:rewardId ALLOW FILTERING")
    public RewardTemplate getByRewardId(Integer rewardId);

    @Query("SELECT * FROM EVT_REWARD_TEMPLATE WHERE campaignid =:campaignId ALLOW FILTERING")
    public RewardTemplate getByCampaignId(Integer campaignId);
}
