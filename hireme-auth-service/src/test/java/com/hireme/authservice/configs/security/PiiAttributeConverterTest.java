package com.hireme.authservice.configs.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires autonomes (sans contexte Spring) du chiffrement AES-256-GCM des PII (§7.3).
 */
class PiiAttributeConverterTest {

    // Clé AES-256 de test (32 octets), encodée Base64 — JAMAIS une clé de production.
    private static final String TEST_KEY =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    private final PiiAttributeConverter converter = new PiiAttributeConverter(TEST_KEY);

    @Test
    @DisplayName("Un e-mail chiffré puis déchiffré redonne la valeur d'origine")
    void roundTripReturnsOriginal() {
        String plain = "candidat@example.com";
        String encrypted = converter.convertToDatabaseColumn(plain);
        assertNotEquals(plain, encrypted, "La valeur stockée ne doit pas être en clair");
        assertEquals(plain, converter.convertToEntityAttribute(encrypted));
    }

    @Test
    @DisplayName("Deux chiffrements de la même valeur diffèrent (IV aléatoire)")
    void encryptionIsNonDeterministic() {
        String plain = "jean.dupont@example.com";
        assertNotEquals(
                converter.convertToDatabaseColumn(plain),
                converter.convertToDatabaseColumn(plain));
    }

    @Test
    @DisplayName("Une donnée altérée en base est rejetée (tag GCM invalide)")
    void tamperedCiphertextIsRejected() {
        String encrypted = converter.convertToDatabaseColumn("secret@example.com");
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 0x01; // on inverse un bit du tag
        String tampered = Base64.getEncoder().encodeToString(bytes);
        assertThrows(IllegalStateException.class,
                () -> converter.convertToEntityAttribute(tampered));
    }

    @Test
    @DisplayName("Les valeurs null sont gérées sans erreur")
    void nullIsSafe() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("Une clé de taille incorrecte est refusée")
    void rejectsWrongKeySize() {
        String shortKey = Base64.getEncoder().encodeToString("tropcourt".getBytes());
        assertThrows(IllegalStateException.class, () -> new PiiAttributeConverter(shortKey));
    }
}
