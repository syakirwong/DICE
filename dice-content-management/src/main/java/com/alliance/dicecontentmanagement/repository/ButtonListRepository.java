package com.alliance.dicecontentmanagement.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.dicecontentmanagement.model.ButtonList;

@Repository
public interface ButtonListRepository extends CassandraRepository<ButtonList, Integer> {

    @Query(("SELECT MAX(button_id) FROM EVT_BUTTON_LIST"))
    Integer getMaxButtonListId();
}
