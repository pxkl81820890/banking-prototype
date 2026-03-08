package com.banking.channelconfig.domain.service;

import com.banking.channelconfig.domain.model.AclConfig;
import com.banking.channelconfig.domain.model.FeatureFlagMaster;
import com.banking.channelconfig.domain.repository.AclConfigRepository;
import com.banking.channelconfig.domain.repository.FeatureFlagMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to retrieve feature flags for a user based on ACL configuration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final FeatureFlagMasterRepository featureFlagMasterRepository;
    private final AclConfigRepository aclConfigRepository;

    /**
     * Get all feature flags for a given user ID.
     * 
     * Logic:
     * 1. Get ALL feature flags from MASTER table
     * 2. For each feature flag, determine if user has access:
     *    - If IS_ACL_ENABLED = false: return true (public feature)
     *    - If IS_ACL_ENABLED = true: check if user has matching ACL_ID
     * 3. Return map with all feature flags and their true/false status
     * 
     * @param userId the user ID from request header
     * @return Map of feature flag names to their enabled status (true/false)
     */
    public Map<String, Boolean> getFeatureFlagsForUser(String userId) {
        log.info("Fetching feature flags for user: {}", userId);
        
        Map<String, Boolean> featureFlags = new HashMap<>();
        
        // 1. Get user's ACL IDs
        List<AclConfig> userAcls = aclConfigRepository.findByUserId(userId);
        Set<Long> userAclIds = userAcls.stream()
                .map(AclConfig::getAclId)
                .collect(Collectors.toSet());
        log.debug("User {} has ACL IDs: {}", userId, userAclIds);
        
        // 2. Get ALL feature flags
        List<FeatureFlagMaster> allFlags = featureFlagMasterRepository.findAll();
        log.debug("Found {} total feature flags", allFlags.size());
        
        // 3. Determine access for each feature flag
        for (FeatureFlagMaster flag : allFlags) {
            boolean hasAccess;
            
            if (!flag.getIsAclEnabled()) {
                // Public feature - available to all users
                hasAccess = true;
                log.debug("Feature '{}' is public - access granted", flag.getFeatureFlag());
            } else {
                // ACL-protected feature - check if user has required ACL
                hasAccess = userAclIds.contains(flag.getAclId());
                log.debug("Feature '{}' requires ACL {} - access {}", 
                    flag.getFeatureFlag(), 
                    flag.getAclId(), 
                    hasAccess ? "granted" : "denied");
            }
            
            featureFlags.put(flag.getFeatureFlag(), hasAccess);
        }
        
        log.info("Returning {} feature flags for user {} ({} enabled, {} disabled)", 
            featureFlags.size(), 
            userId,
            featureFlags.values().stream().filter(v -> v).count(),
            featureFlags.values().stream().filter(v -> !v).count());
        
        return featureFlags;
    }
}
