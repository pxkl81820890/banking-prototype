package com.banking.channelconfig.infrastructure.adapters.in;

import com.banking.channelconfig.domain.service.FeatureFlagService;
import com.banking.channelconfig.infrastructure.adapters.in.dto.FeatureFlagsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for feature flag operations.
 */
@RestController
@RequestMapping("/api/v1/feature-flags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feature Flags", description = "Feature flag and ACL configuration operations")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    /**
     * Get feature flags for a user based on their ACL configuration.
     * 
     * @param userId User ID from request header
     * @return Map of feature flags and their enabled status
     */
    @GetMapping
    @Operation(
        summary = "Get feature flags for user",
        description = "Retrieves all feature flags available to the user based on ACL configuration. " +
                     "Returns both public flags (IS_ACL_ENABLED=false) and user-specific flags based on ACL_ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Feature flags retrieved successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagsResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - Missing USER_ID header"
        )
    })
    public ResponseEntity<FeatureFlagsResponse> getFeatureFlags(
            @Parameter(description = "User ID", required = true, example = "1119test1")
            @RequestHeader("USER_ID") String userId) {
        
        log.info("Received request for feature flags with USER_ID: {}", userId);
        
        Map<String, Boolean> featureFlags = featureFlagService.getFeatureFlagsForUser(userId);
        
        FeatureFlagsResponse response = new FeatureFlagsResponse(userId, featureFlags);
        
        return ResponseEntity.ok(response);
    }
}
