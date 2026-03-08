package com.banking.loginservice.infrastructure.adapters.in;

import com.banking.loginservice.domain.model.LoginResult;
import com.banking.loginservice.domain.ports.LoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller (Inbound Adapter) for login operations.
 * Maps HTTP requests to the LoginUseCase port following hexagonal architecture.
 * Uses constructor injection via Lombok @RequiredArgsConstructor as per style.md.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication operations")
public class LoginController {

    private final LoginUseCase loginUseCase;

    /**
     * Handles POST /api/v1/auth/login endpoint.
     * Accepts login credentials and delegates to the domain service via LoginUseCase port.
     * Exceptions are handled by GlobalExceptionHandler.
     *
     * @param request the login request containing bankCode, branchCode, username, password, and currency
     * @return ResponseEntity with LoginResponse containing success status, userId, message, and token
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user with multi-entity context (bank, branch, currency). Returns a JWT token upon successful authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - Currency mismatch or invalid entity",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {} at bank: {}, branch: {}", 
                 request.username(), request.bankCode(), request.branchCode());

        LoginResult result = loginUseCase.login(
            request.bankCode(),
            request.branchCode(),
            request.username(),
            request.password(),
            request.currency()
        );

        LoginResponse response = new LoginResponse(
            result.success(),
            result.userId(),
            result.message(),
            result.token()
        );
        
        log.info("Login successful for user: {}", request.username());
        
        return ResponseEntity.ok(response);
    }
}
