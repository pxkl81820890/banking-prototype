package com.banking.authservice.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Utility class for loading RSA keys from PEM files.
 */
@Slf4j
public class KeyLoader {

    private final ResourceLoader resourceLoader;

    public KeyLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public PrivateKey loadPrivateKey(String path) {
        try {
            log.debug("Loading private key from: {}", path);
            Resource resource = resourceLoader.getResource(path);
            
            try (PEMParser pemParser = new PEMParser(new InputStreamReader(resource.getInputStream()))) {
                Object object = pemParser.readObject();
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                
                if (object instanceof PrivateKeyInfo) {
                    PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) object);
                    log.info("Private key loaded successfully");
                    return privateKey;
                }
                
                throw new IllegalArgumentException("Invalid private key format");
            }
        } catch (IOException e) {
            log.error("Failed to load private key from: {}", path, e);
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    public PublicKey loadPublicKey(String path) {
        try {
            log.debug("Loading public key from: {}", path);
            Resource resource = resourceLoader.getResource(path);
            
            try (PEMParser pemParser = new PEMParser(new InputStreamReader(resource.getInputStream()))) {
                Object object = pemParser.readObject();
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                
                if (object instanceof SubjectPublicKeyInfo) {
                    PublicKey publicKey = converter.getPublicKey((SubjectPublicKeyInfo) object);
                    log.info("Public key loaded successfully");
                    return publicKey;
                }
                
                throw new IllegalArgumentException("Invalid public key format");
            }
        } catch (IOException e) {
            log.error("Failed to load public key from: {}", path, e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }
}
