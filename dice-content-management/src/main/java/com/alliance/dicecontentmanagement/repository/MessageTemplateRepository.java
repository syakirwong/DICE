package com.alliance.dicecontentmanagement.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.dicecontentmanagement.model.MessageTemplate;

public interface MessageTemplateRepository extends CassandraRepository<MessageTemplate, Integer> {

    @Query(("SELECT MAX(messageTemplateId) FROM EVT_MESSAGE_TEMPLATE"))
    Integer getMaxMessageTemplateId();

};
