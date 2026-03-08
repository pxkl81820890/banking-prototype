package com.banking.authservice.infrastructure.adapters.in;

import com.banking.authservice.domain.model.GeneratedToken;
import com.banking.authservice.domain.model.TokenPayload;
import com.banking.authservice.domain.ports.TokenGenerationUseCase;
import com.banking.authservice.infrastructure.adapters.in.dto.TokenRequest;
import com.banking.authservice.infrastructure.adapters.in.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Inbound adapter in hexagonal architecture.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Token Management", description = "JWT token generation operations")
public class AuthController {

    private final TokenGenerationUseCase tokenGenerationUseCase;

    @PostMapping("/generate-token")
    @Operation(
        summary = "Generate JWT token",
        description = "Generates a stateless JWT token signed with RS256 algorithm. Token includes custom claims for multi-entity banking context (bank, branch, currency)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token generated successfully",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - Missing or invalid fields"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - Token generation failed"
        )
    })
    public ResponseEntity<TokenResponse> generateToken(@Valid @RequestBody TokenRequest request) {
        log.info("Received token generation request for user: {}", request.userId());
        
        TokenPayload payload = new TokenPayload(
            request.userId(),
            request.bankCode(),
            request.branchCode(),
            request.currency()
        );
        
        GeneratedToken generatedToken = tokenGenerationUseCase.generateToken(payload);
        
        TokenResponse response = new TokenResponse(
            true,
            generatedToken.token(),
            generatedToken.message(),
            generatedToken.expiresIn()
        );
        
        return ResponseEntity.ok(response);
    }
}