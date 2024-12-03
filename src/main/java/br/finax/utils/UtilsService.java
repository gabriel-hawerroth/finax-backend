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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class UtilsService {

    private static final Pattern errorMessageHandlePattern = Pattern.compile("ERROR:\\s([^\\n\\[$]++)");

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

    public static String extractRelevantErrorMessage(String exceptionMessage) {
        if (exceptionMessage == null || exceptionMessage.isBlank()) {
            return null;
        }

        Matcher matcher = errorMessageHandlePattern.matcher(exceptionMessage);

        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(2).trim();
        }

        return "Error message not found";
    }
}
