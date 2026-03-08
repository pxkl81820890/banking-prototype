package com.banking.channelconfig.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Response containing feature flags for a user.
 */
@Schema(description = "Feature flags response")
public record FeatureFlagsResponse(
    @Schema(description = "User ID", example = "1119test1")
    String userId,
    
    @Schema(description = "Map of feature flag names to their enabled status", 
            example = "{\"isArchiveEnquiryEnabled\": true, \"isTransferEnabled\": false}")
    Map<String, Boolean> featureFlags
) {
}
