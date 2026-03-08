package com.banking.channelconfig.domain.repository;

import com.banking.channelconfig.domain.model.AclConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AclConfigRepository extends JpaRepository<AclConfig, Long> {
    
    List<AclConfig> findByUserId(String userId);
}
