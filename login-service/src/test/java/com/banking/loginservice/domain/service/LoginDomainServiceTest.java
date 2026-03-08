package com.banking.loginservice.domain.service;

import com.banking.loginservice.domain.exception.CurrencyMismatchException;
import com.banking.loginservice.domain.exception.InvalidCredentialsException;
import com.banking.loginservice.domain.exception.InvalidEntityException;
import com.banking.loginservice.domain.model.LoginResult;
import com.banking.loginservice.domain.model.User;
import com.banking.loginservice.domain.ports.AuthenticationOutputPort;
import com.banking.loginservice.domain.ports.UserOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoginDomainService.
 * Tests all scenarios: successful login, invalid entity, invalid credentials, and currency mismatch.
 * Validates requirements RE-01 (success path) and RE-02 (error handling).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginDomainService Unit Tests")
class LoginDomainServiceTest {

    @Mock
    private UserOutputPort userOutputPort;

    @Mock
    private AuthenticationOutputPort authenticationOutputPort;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginDomainService loginDomainService;

    private static final String VALID_BANK_CODE = "101";
    private static final String VALID_BRANCH_CODE = "1119";
    private static final String VALID_USERNAME = "1119TestUser1";
    private static final String VALID_PASSWORD = "password123";
    private static final String VALID_CURRENCY = "SGD";
    private static final String VALID_USER_ID = "user-123";
    private static final String VALID_PASSWORD_HASH = "$2a$10$hashedPassword";
    private static final String EXPECTED_TOKEN = "authentication-success!";

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User(
            VALID_USER_ID,
            VALID_PASSWORD_HASH,
            VALID_BANK_CODE,
            VALID_BRANCH_CODE,
            VALID_CURRENCY
        );
    }

    @Test
    @DisplayName("Should successfully login with valid credentials - RE-01 Success Path")
    void shouldSuccessfullyLoginWithValidCredentials() {
        // Given
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(VALID_PASSWORD, VALID_PASSWORD_HASH))
            .thenReturn(true);
        when(authenticationOutputPort.generateToken(VALID_USER_ID, VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_CURRENCY))
            .thenReturn(EXPECTED_TOKEN);

        // When
        LoginResult result = loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.userId()).isEqualTo(VALID_USER_ID);
        assertThat(result.token()).isEqualTo(EXPECTED_TOKEN);
        assertThat(result.message()).isEqualTo("Login successful");

        verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME);
        verify(passwordEncoder).matches(VALID_PASSWORD, VALID_PASSWORD_HASH);
        verify(authenticationOutputPort).generateToken(VALID_USER_ID, VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_CURRENCY);
    }

    @Test
    @DisplayName("Should throw InvalidEntityException when user not found - RE-02 Invalid Entity")
    void shouldThrowInvalidEntityExceptionWhenUserNotFound() {
        // Given
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY))
            .isInstanceOf(InvalidEntityException.class);

        verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidEntityException with invalid bank code")
    void shouldThrowInvalidEntityExceptionWithInvalidBankCode() {
        // Given
        String invalidBankCode = "999";
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            invalidBankCode, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            invalidBankCode, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY))
            .isInstanceOf(InvalidEntityException.class);

        verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            invalidBankCode, VALID_BRANCH_CODE, VALID_USERNAME);
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidEntityException with invalid branch code")
    void shouldThrowInvalidEntityExceptionWithInvalidBranchCode() {
        // Given
        String invalidBranchCode = "9999";
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, invalidBranchCode, VALID_USERNAME))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, invalidBranchCode, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY))
            .isInstanceOf(InvalidEntityException.class);

        verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, invalidBranchCode, VALID_USERNAME);
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void shouldThrowInvalidCredentialsExceptionWhenPasswordIncorrect() {
        // Given
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(VALID_PASSWORD, VALID_PASSWORD_HASH))
            .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME);
        verify(passwordEncoder).matches(VALID_PASSWORD, VALID_PASSWORD_HASH);
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException with wrong password")
    void shouldThrowInvalidCredentialsExceptionWithWrongPassword() {
        // Given
        String wrongPassword = "wrongPassword";
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(wrongPassword, VALID_PASSWORD_HASH))
            .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, wrongPassword, VALID_CURRENCY))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder).matches(wrongPassword, VALID_PASSWORD_HASH);
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw CurrencyMismatchException when currency does not match")
    void shouldThrowCurrencyMismatchExceptionWhenCurrencyDoesNotMatch() {
        // Given
        String wrongCurrency = "USD";
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(VALID_PASSWORD, VALID_PASSWORD_HASH))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, wrongCurrency))
            .isInstanceOf(CurrencyMismatchException.class);

        verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME);
        verify(passwordEncoder).matches(VALID_PASSWORD, VALID_PASSWORD_HASH);
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should successfully login with USD currency")
    void shouldSuccessfullyLoginWithUsdCurrency() {
        // Given
        User usdUser = new User(
            VALID_USER_ID,
            VALID_PASSWORD_HASH,
            VALID_BANK_CODE,
            VALID_BRANCH_CODE,
            "USD"
        );
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(usdUser));
        when(passwordEncoder.matches(VALID_PASSWORD, VALID_PASSWORD_HASH))
            .thenReturn(true);
        when(authenticationOutputPort.generateToken(VALID_USER_ID, VALID_BANK_CODE, VALID_BRANCH_CODE, "USD"))
            .thenReturn(EXPECTED_TOKEN);

        // When
        LoginResult result = loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, "USD");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.userId()).isEqualTo(VALID_USER_ID);
        verify(authenticationOutputPort).generateToken(VALID_USER_ID, VALID_BANK_CODE, VALID_BRANCH_CODE, "USD");
    }

    @Test
    @DisplayName("Should validate all steps in correct order - RE-01 Step Sequence")
    void shouldValidateAllStepsInCorrectOrder() {
        // Given
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(VALID_PASSWORD, VALID_PASSWORD_HASH))
            .thenReturn(true);
        when(authenticationOutputPort.generateToken(VALID_USER_ID, VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_CURRENCY))
            .thenReturn(EXPECTED_TOKEN);

        // When
        loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY);

        // Then - Verify order of operations
        var inOrder = inOrder(userOutputPort, passwordEncoder, authenticationOutputPort);
        inOrder.verify(userOutputPort).findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME);
        inOrder.verify(passwordEncoder).matches(VALID_PASSWORD, VALID_PASSWORD_HASH);
        inOrder.verify(authenticationOutputPort).generateToken(VALID_USER_ID, VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_CURRENCY);
    }

    @Test
    @DisplayName("Should not check password when user not found")
    void shouldNotCheckPasswordWhenUserNotFound() {
        // Given
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, VALID_CURRENCY))
            .isInstanceOf(InvalidEntityException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should not check currency when password is invalid")
    void shouldNotCheckCurrencyWhenPasswordInvalid() {
        // Given
        when(userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME))
            .thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(VALID_PASSWORD, VALID_PASSWORD_HASH))
            .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> loginDomainService.login(
            VALID_BANK_CODE, VALID_BRANCH_CODE, VALID_USERNAME, VALID_PASSWORD, "USD"))
            .isInstanceOf(InvalidCredentialsException.class);

        // Currency check should not be reached
        verify(passwordEncoder).matches(VALID_PASSWORD, VALID_PASSWORD_HASH);
        verify(authenticationOutputPort, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }
}
