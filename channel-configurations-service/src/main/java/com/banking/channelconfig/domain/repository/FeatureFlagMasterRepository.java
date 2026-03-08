package com.banking.channelconfig.domain.repository;

import com.banking.channelconfig.domain.model.FeatureFlagMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureFlagMasterRepository extends JpaRepository<FeatureFlagMaster, Long> {
    
    List<FeatureFlagMaster> findByIsAclEnabledFalse();
    
    List<FeatureFlagMaster> findByIsAclEnabledTrueAndAclIdIn(List<Long> aclIds);
}
