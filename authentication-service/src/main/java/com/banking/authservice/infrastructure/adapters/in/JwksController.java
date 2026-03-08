package com.banking.authservice.infrastructure.adapters.in;

import com.banking.authservice.infrastructure.config.JwtConfigProperties;
import com.banking.authservice.infrastructure.security.KeyLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Exposes the RSA public key as a JSON Web Key Set (JWKS) so that other
 * services can fetch it and validate tokens without manual key distribution.
 *
 * Endpoint: GET /.well-known/jwks.json
 */
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
@Slf4j
public class JwksController {

    private final ResourceLoader resourceLoader;
    private final JwtConfigProperties jwtConfigProperties;

    private Map<String, Object> jwks;

    @PostConstruct
    public void init() {
        KeyLoader keyLoader = new KeyLoader(resourceLoader);
        RSAPublicKey publicKey = (RSAPublicKey) keyLoader.loadPublicKey(jwtConfigProperties.getPublicKeyPath());

        Base64.Encoder base64url = Base64.getUrlEncoder().withoutPadding();

        String n = base64url.encodeToString(toUnsignedBytes(publicKey.getModulus()));
        String e = base64url.encodeToString(toUnsignedBytes(publicKey.getPublicExponent()));
        String kid = computeKid(publicKey, base64url);

        Map<String, Object> jwk = Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", kid,
                "n", n,
                "e", e
        );

        this.jwks = Map.of("keys", List.of(jwk));
        log.info("JWKS endpoint ready with kid: {}", kid);
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(jwks);
    }

    /**
     * BigInteger.toByteArray() prepends a 0x00 byte when the high bit is set
     * to signal a positive value. JWKS requires unsigned big-endian encoding,
     * so we strip that leading zero when present.
     */
    private byte[] toUnsignedBytes(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0) {
            byte[] unsigned = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, unsigned, 0, unsigned.length);
            return unsigned;
        }
        return bytes;
    }

    /**
     * Derives a stable key ID from a SHA-256 digest of the DER-encoded public key.
     * Falls back to a static value if the digest algorithm is unavailable.
     */
    private String computeKid(RSAPublicKey publicKey, Base64.Encoder base64url) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(publicKey.getEncoded());
            return base64url.encodeToString(digest);
        } catch (Exception ex) {
            log.warn("Could not compute kid via SHA-256, falling back to static value", ex);
            return "rsa-signing-key";
        }
    }
}
