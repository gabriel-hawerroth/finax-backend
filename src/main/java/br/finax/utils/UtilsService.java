package br.finax.utils;

import br.finax.exceptions.InvalidHashAlgorithmException;
import br.finax.models.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@UtilityClass
public class UtilsService {

    public static String generateHash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new InvalidHashAlgorithmException();
        }

        byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));

        // Converter bytes em representação hexadecimal
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            hexStringBuilder.append(String.format("%02x", b));
        }

        return hexStringBuilder.toString();
    }

    public static User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public static boolean isEmpty(Object object) {
        return !isNotEmpty(object);
    }

    public static boolean isNotEmpty(Object object) {
        if (object == null)
            return false;

        return switch (object) {
            case String s -> !s.isBlank();
            case List<?> list -> !list.isEmpty();
            default -> false;
        };
    }
}
