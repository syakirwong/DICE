package com.alliance.dicesqlitecache.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alliance.dicesqlitecache.model.SoleCcEntity;

public interface SoleCcRepository extends JpaRepository<SoleCcEntity, String> {

    Optional<SoleCcEntity> findByCifNo(String cifNo);

    Optional<SoleCcEntity> findByUserId(String userId);
}
