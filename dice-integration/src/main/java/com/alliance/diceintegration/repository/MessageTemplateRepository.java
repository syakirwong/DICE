package com.alliance.diceintegration.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceintegration.model.MessageTemplate;

@Repository
public interface MessageTemplateRepository extends CassandraRepository<MessageTemplate, Integer> {
    @Query(("SELECT * FROM EVT_MESSAGE_TEMPLATE WHERE messageTemplateId = :messageTemplateId ALLOW FILTERING"))
    public MessageTemplate getByMessageTemplateId(Integer messageTemplateId);
}

