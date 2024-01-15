package com.alliance.dicesqlitecache.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alliance.dicesqlitecache.model.PloanApplicationEntity;

public interface PloanApplicationRepository extends JpaRepository<PloanApplicationEntity, String> {
    
}
