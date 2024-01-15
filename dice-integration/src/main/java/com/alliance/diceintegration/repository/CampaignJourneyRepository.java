package com.alliance.diceintegration.repository;

import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.alliance.diceintegration.model.CampaignJourney;

@Repository
public interface CampaignJourneyRepository extends CassandraRepository<CampaignJourney, Integer> {
    CampaignJourney getById(UUID uuid);

}
