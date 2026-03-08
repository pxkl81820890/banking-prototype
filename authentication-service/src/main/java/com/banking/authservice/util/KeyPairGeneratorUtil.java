package com.banking.authservice.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Utility class for generating RSA 2048-bit key pairs for JWT signing.
 * Generates keys in PEM format:
 * - Private key: PKCS#8 format
 * - Public key: X.509 format
 */
public class KeyPairGeneratorUtil {

    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";
    
    public static void main(String[] args) {
        try {
            // Generate key pair
            KeyPair keyPair = generateKeyPair();
            
            // Define output directory
            String outputDir = "src/main/resources/keys";
            Path keysPath = Paths.get(outputDir);
            
            // Create directory if it doesn't exist
            if (!Files.exists(keysPath)) {
                Files.createDirectories(keysPath);
                System.out.println("Created directory: " + outputDir);
            }
            
            // Save keys to files
            savePrivateKey(keyPair.getPrivate(), outputDir + "/private_key.pem");
            savePublicKey(keyPair.getPublic(), outputDir + "/public_key.pem");
            
            System.out.println("RSA 2048-bit key pair generated successfully!");
            System.out.println("Private key saved to: " + outputDir + "/private_key.pem");
            System.out.println("Public key saved to: " + outputDir + "/public_key.pem");
            
        } catch (Exception e) {
            System.err.println("Error generating key pair: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates an RSA 2048-bit key pair.
     */
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }
    
    /**
     * Saves the private key in PKCS#8 PEM format.
     */
    private static void savePrivateKey(PrivateKey privateKey, String filename) throws IOException {
        String encodedKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            writer.write(formatPemContent(encodedKey));
            writer.write("-----END PRIVATE KEY-----\n");
        }
    }
    
    /**
     * Saves the public key in X.509 PEM format.
     */
    private static void savePublicKey(PublicKey publicKey, String filename) throws IOException {
        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            writer.write(formatPemContent(encodedKey));
            writer.write("-----END PUBLIC KEY-----\n");
        }
    }
    
    /**
     * Formats the base64 encoded key content to 64 characters per line.
     */
    private static String formatPemContent(String content) {
        StringBuilder formatted = new StringBuilder();
        int index = 0;
        while (index < content.length()) {
            int endIndex = Math.min(index + 64, content.length());
            formatted.append(content, index, endIndex).append("\n");
            index = endIndex;
        }
        return formatted.toString();
    }
}
