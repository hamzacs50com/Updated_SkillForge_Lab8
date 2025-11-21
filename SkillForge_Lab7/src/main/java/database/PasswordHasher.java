package database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA-256 password hashing.
 */
public class PasswordHasher {

    /**
     * Hashes a plain-text password using SHA-256.
     * @param password The plain-text password.
     * @return The hex string representation of the hash.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not found.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a plain-text password matches a stored hash.
     * @param plainPassword The plain-text password to check.
     * @param hashedPassword The stored SHA-256 hash.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        String hashOfPlainPassword = hashPassword(plainPassword);
        return hashOfPlainPassword != null && hashOfPlainPassword.equals(hashedPassword);
    }

    /**
     * Helper method to convert byte array to hex string.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}