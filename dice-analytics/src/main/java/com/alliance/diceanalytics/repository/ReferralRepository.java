package com.alliance.diceanalytics.repository;

import com.alliance.diceanalytics.model.ReferralHistory;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReferralRepository extends CassandraRepository<ReferralHistory, Integer> {


    @AllowFiltering
    @Query("SELECT * FROM EVT_REFERRAL_HISTORY_LOG WHERE created_on >=:startDate and created_on <=:endDate and campaignid=:campaignID ALLOW FILTERING")
    List<ReferralHistory> getByRefereeInitiationDate(Date startDate, Date endDate, Integer campaignID);



}

