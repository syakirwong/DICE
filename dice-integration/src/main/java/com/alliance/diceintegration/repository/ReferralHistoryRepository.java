package com.alliance.diceintegration.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.diceintegration.model.ReferralHistory;

public interface ReferralHistoryRepository extends CassandraRepository<ReferralHistory, Integer> {

    @Query(("SELECT MAX(referralHistoryLogId) FROM EVT_REFERRAL_HISTORY_LOG"))
    Integer getMaxReferralHistoryLogId();
}
