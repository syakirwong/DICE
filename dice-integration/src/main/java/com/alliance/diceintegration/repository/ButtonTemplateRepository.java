package com.alliance.diceintegration.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceintegration.model.ButtonTemplate;

@Repository
public interface ButtonTemplateRepository extends CassandraRepository<ButtonTemplate, Integer> {
    @Query(("SELECT * FROM EVT_BUTTON_TEMPLATE WHERE buttonTemplateId = :buttonTemplateId ALLOW FILTERING"))
    public ButtonTemplate getByButtonTemplateId(Integer buttonTemplateId);
}

