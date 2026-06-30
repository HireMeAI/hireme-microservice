package com.hireme.authservice.configs.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Chiffrement authentifié AES-256-GCM des données personnelles identifiantes (PII) au repos
 * (dossier §7.3, RGPD Art. 32).
 *
 * <p>Mode GCM (AEAD) : chaque valeur chiffrée embarque un tag d'authentification de 128 bits.
 * Toute altération d'un seul bit en base fait échouer le déchiffrement — la donnée corrompue
 * n'est jamais présentée.</p>
 *
 * <p>Format stocké : Base64( IV[12 octets] || ciphertext || tag ).</p>
 *
 * <p>La clé (256 bits, encodée en Base64) est fournie via la variable d'environnement
 * {@code HIREME_PII_KEY}. Hibernate instancie ce convertisseur via son constructeur sans
 * argument ; un constructeur dédié permet l'injection d'une clé en test.</p>
 */
@Converter
public class PiiAttributeConverter implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;        // 96 bits, recommandé pour GCM
    private static final int TAG_LENGTH_BITS = 128; // tag d'authentification
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    public PiiAttributeConverter() {
        this(System.getenv("HIREME_PII_KEY"));
    }

    /** Constructeur testable : clé AES-256 encodée en Base64 (32 octets décodés). */
    public PiiAttributeConverter(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(
                    "HIREME_PII_KEY manquante : impossible de chiffrer les PII (RGPD Art. 32).");
        }
        byte[] raw = Base64.getDecoder().decode(base64Key);
        if (raw.length != 32) {
            throw new IllegalStateException("La clé PII doit faire 256 bits (32 octets).");
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Échec du chiffrement AES-GCM d'une PII.", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbValue);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Tag GCM invalide => donnée altérée : on refuse de présenter une valeur corrompue.
            throw new IllegalStateException("Échec du déchiffrement AES-GCM (PII altérée ?).", e);
        }
    }
}
