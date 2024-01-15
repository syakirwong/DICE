package com.alliance.dicerecommendation.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.alliance.dicerecommendation.model.PersonalInfoTemplate;

@Repository
public interface PersonalInfoTemplateRepository  extends CassandraRepository<PersonalInfoTemplate, Integer> {
    @Query("SELECT PERSONAL_INFO_NAME FROM EVT_PERSONAL_INFO_TEMPLATE WHERE PERSONAL_INFO_TEMPLATE_ID = :personalInfoTemplateId ALLOW FILTERING")
    String getPersonalInfoTemplateNameById(Integer personalInfoTemplateId);
}
