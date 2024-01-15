package com.alliance.dicerecommendation.repository;

import com.alliance.dicerecommendation.model.HeaderMapping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HeaderMappingRepository extends CassandraRepository<HeaderMapping, Integer> {
    @Query(("SELECT MAX(HEADER_MAPPING_ID) FROM EVT_HEADER_MAPPING"))
    Integer getMaxHeaderMappingId();

    @Query(("SELECT * FROM EVT_HEADER_MAPPING WHERE HEADER_MAPPING_ID = :headerMappingId"))
    Optional<HeaderMapping> getHeaderMappingById(Integer headerMappingId);

    @Query(("SELECT header_name,header_name_mapping,header_type FROM EVT_HEADER_MAPPING WHERE HEADER_MAPPING_ID IN :headerMappingIdList"))
    List<HeaderMapping> getNameHeaderMappingInId(List<Integer> headerMappingIdList);

}

