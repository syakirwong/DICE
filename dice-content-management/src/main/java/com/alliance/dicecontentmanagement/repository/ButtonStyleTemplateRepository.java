package com.alliance.dicecontentmanagement.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.alliance.dicecontentmanagement.model.ButtonStyleTemplate;

public interface ButtonStyleTemplateRepository extends CassandraRepository<ButtonStyleTemplate, Integer> {

    @Query(("SELECT MAX(buttonStyleTemplateId) FROM EVT_BUTTON_STYLE_TEMPLATE"))
    Integer getMaxButtonStyleTemplateId();
}
