package com.banking.authservice.domain.service;

import com.banking.authservice.domain.model.GeneratedToken;
import com.banking.authservice.domain.model.TokenPayload;
import com.banking.authservice.domain.ports.TokenProviderOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TokenGenerationService.
 * Tests the domain service logic in isolation by mocking the TokenProviderOutputPort.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenGenerationService Unit Tests")
class TokenGenerationServiceTest {

    @Mock
    private TokenProviderOutputPort tokenProvider;

    @InjectMocks
    private TokenGenerationService tokenGenerationService;

    private static final long EXPIRATION_SECONDS = 3600L;
    private static final String MOCK_JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.signature";

    @BeforeEach
    void setUp() {
        // Set the expiration value using reflection since it's injected via @Value
        ReflectionTestUtils.setField(tokenGenerationService, "expirationSeconds", EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("Should generate token successfully with valid payload")
    void shouldGenerateTokenSuccessfully() {
        // Given
        TokenPayload payload = new TokenPayload(
            "user-uuid-123",
            "101",
            "1119",
            "SGD"
        );
        
        when(tokenProvider.signToken(payload, EXPIRATION_SECONDS))
            .thenReturn(MOCK_JWT_TOKEN);

        // When
        GeneratedToken result = tokenGenerationService.generateToken(payload);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(MOCK_JWT_TOKEN);
        assertThat(result.expiresIn()).isEqualTo(EXPIRATION_SECONDS);
        assertThat(result.message()).isEqualTo("Token generated successfully");
        
        verify(tokenProvider).signToken(payload, EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("Should delegate to token provider with correct parameters")
    void shouldDelegateToTokenProviderWithCorrectParameters() {
        // Given
        TokenPayload payload = new TokenPayload(
            "user-456",
            "202",
            "2228",
            "USD"
        );
        
        when(tokenProvider.signToken(any(TokenPayload.class), eq(EXPIRATION_SECONDS)))
            .thenReturn(MOCK_JWT_TOKEN);

        // When
        tokenGenerationService.generateToken(payload);

        // Then
        verify(tokenProvider).signToken(payload, EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("Should return GeneratedToken with correct expiration time")
    void shouldReturnGeneratedTokenWithCorrectExpirationTime() {
        // Given
        TokenPayload payload = new TokenPayload(
            "user-789",
            "303",
            "3337",
            "EUR"
        );
        
        when(tokenProvider.signToken(any(TokenPayload.class), eq(EXPIRATION_SECONDS)))
            .thenReturn(MOCK_JWT_TOKEN);

        // When
        GeneratedToken result = tokenGenerationService.generateToken(payload);

        // Then
        assertThat(result.expiresIn()).isEqualTo(EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("Should handle different expiration configurations")
    void shouldHandleDifferentExpirationConfigurations() {
        // Given
        long customExpiration = 7200L; // 2 hours
        ReflectionTestUtils.setField(tokenGenerationService, "expirationSeconds", customExpiration);
        
        TokenPayload payload = new TokenPayload(
            "user-999",
            "404",
            "4446",
            "JPY"
        );
        
        when(tokenProvider.signToken(payload, customExpiration))
            .thenReturn(MOCK_JWT_TOKEN);

        // When
        GeneratedToken result = tokenGenerationService.generateToken(payload);

        // Then
        assertThat(result.expiresIn()).isEqualTo(customExpiration);
        verify(tokenProvider).signToken(payload, customExpiration);
    }

    @Test
    @DisplayName("Should reject null userId in TokenPayload")
    void shouldRejectNullUserId() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload(null, "101", "1119", "SGD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("userId cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank userId in TokenPayload")
    void shouldRejectBlankUserId() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("", "101", "1119", "SGD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("userId cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null bankCode in TokenPayload")
    void shouldRejectNullBankCode() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("user-123", null, "1119", "SGD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("bankCode cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank bankCode in TokenPayload")
    void shouldRejectBlankBankCode() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("user-123", "  ", "1119", "SGD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("bankCode cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null branchCode in TokenPayload")
    void shouldRejectNullBranchCode() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("user-123", "101", null, "SGD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("branchCode cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank branchCode in TokenPayload")
    void shouldRejectBlankBranchCode() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("user-123", "101", "", "SGD"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("branchCode cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null currency in TokenPayload")
    void shouldRejectNullCurrency() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("user-123", "101", "1119", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("currency cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank currency in TokenPayload")
    void shouldRejectBlankCurrency() {
        // When/Then
        assertThatThrownBy(() -> new TokenPayload("user-123", "101", "1119", "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("currency cannot be null or blank");
    }

    @Test
    @DisplayName("Should accept valid TokenPayload with all required fields")
    void shouldAcceptValidTokenPayload() {
        // Given/When
        TokenPayload payload = new TokenPayload(
            "user-uuid-123",
            "101",
            "1119",
            "SGD"
        );

        // Then
        assertThat(payload.userId()).isEqualTo("user-uuid-123");
        assertThat(payload.bankCode()).isEqualTo("101");
        assertThat(payload.branchCode()).isEqualTo("1119");
        assertThat(payload.currency()).isEqualTo("SGD");
    }

    @Test
    @DisplayName("Should generate token with different currency codes")
    void shouldGenerateTokenWithDifferentCurrencyCodes() {
        // Given
        String[] currencies = {"SGD", "USD", "EUR", "JPY", "GBP"};
        
        for (String currency : currencies) {
            TokenPayload payload = new TokenPayload(
                "user-123",
                "101",
                "1119",
                currency
            );
            
            when(tokenProvider.signToken(payload, EXPIRATION_SECONDS))
                .thenReturn(MOCK_JWT_TOKEN);

            // When
            GeneratedToken result = tokenGenerationService.generateToken(payload);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(MOCK_JWT_TOKEN);
        }
    }
}
