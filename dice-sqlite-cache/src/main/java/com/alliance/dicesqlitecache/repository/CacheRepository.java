package com.alliance.dicesqlitecache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alliance.dicesqlitecache.model.CacheEntity;


public interface CacheRepository extends JpaRepository<CacheEntity, String> {

}
