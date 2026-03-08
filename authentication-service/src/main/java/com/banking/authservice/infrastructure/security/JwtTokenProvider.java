package com.banking.authservice.infrastructure.security;

import com.banking.authservice.domain.model.TokenPayload;
import com.banking.authservice.domain.ports.TokenProviderOutputPort;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.Date;

/**
 * Infrastructure adapter implementing JWT token signing using JJWT library.
 * Uses RS256 algorithm with RSA private key.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider implements TokenProviderOutputPort {

    private final ResourceLoader resourceLoader;
    
    @Value("${app.jwt.private-key-path}")
    private String privateKeyPath;
    
    @Value("${app.jwt.issuer}")
    private String issuer;
    
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        log.info("Initializing JWT Token Provider");
        KeyLoader keyLoader = new KeyLoader(resourceLoader);
        this.privateKey = keyLoader.loadPrivateKey(privateKeyPath);
        log.info("JWT Token Provider initialized successfully");
    }

    @Override
    public String signToken(TokenPayload payload, long expirationSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .subject(payload.userId())
                .claim("bnk", payload.bankCode())
                .claim("brn", payload.branchCode())
                .claim("cur", payload.currency())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
}
