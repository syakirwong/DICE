package com.alliance.diceintegration.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.diceintegration.model.ButtonList;

@Repository
public interface ButtonListRepository extends CassandraRepository<ButtonList, Integer> {
    @Query(("SELECT * FROM EVT_BUTTON_LIST WHERE BUTTON_ID = :buttonId ALLOW FILTERING"))
    public ButtonList getByButtonTemplateId(Integer buttonId);
}

