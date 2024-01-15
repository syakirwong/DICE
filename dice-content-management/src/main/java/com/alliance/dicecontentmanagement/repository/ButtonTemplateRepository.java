package com.alliance.dicecontentmanagement.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.dicecontentmanagement.model.ButtonTemplate;

public interface ButtonTemplateRepository extends CassandraRepository<ButtonTemplate, Integer> {

    @Query(("SELECT MAX(buttonTemplateId) FROM EVT_BUTTON_TEMPLATE"))
    Integer getMaxButtonTemplateId();
}
