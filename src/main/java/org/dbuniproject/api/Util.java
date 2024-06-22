package org.dbuniproject.api;

import com.google.common.hash.Hashing;
import io.javalin.http.Context;
import jakarta.annotation.Nullable;
import org.intellij.lang.annotations.Language;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Util {
    @Language("RegExp")
    public static final String EMAIL_REGEX = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\""
                                             + ".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|("
                                             + "([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    private static final int[] RUT_VALIDATION_SEQUENCE = {2, 3, 4, 5, 6, 7};

    public static ArrayList<Integer> getQueryParamAsIntegerList(Context ctx, String key) {
        final ArrayList<Integer> integers = new ArrayList<>();

        for (final String string : ctx.queryParams(key)) {
            for (final String number : string.split(",")) {
                try {
                    integers.add(Integer.parseInt(number));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return integers;
    }

    @Nullable
    public static <T> T getQueryParam(Context ctx, String key, Class<T> clazz) {
        try {
            return ctx.queryParamAsClass(key, clazz).getOrDefault(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static JSONObject readJSONObjectFile(File file) {
        final String content;
        try {
            content = Files.readString(Paths.get(file.toURI()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JSONObject(content);
    }

    @Nullable
    public static <E extends Enum<E>> E stringToEnum(@Nullable String value, Class<E> enumClass) {
        if (value == null) return null;
        for (final E e : enumClass.getEnumConstants()) {
            if (e.toString().equals(value)) {
                return e;
            }
        }
        return null;
    }

    public static String generatePassword() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder password = new StringBuilder();
        for (byte b : bytes) {
            char c = (char) (((char) (b) % 93) + 33);
            password.append(c == '\\' ? '?' : c);
        }
        return password.toString();
    }

    public static String generateSalt() {
        byte[] randomBytes = new byte[16];
        new SecureRandom().nextBytes(randomBytes);
        return new BigInteger(1, randomBytes).toString(16);
    }

    public static String hashPassword(String password, String salt) {
        return Hashing.sha256()
                .hashString(password + salt, StandardCharsets.UTF_8)
                .toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidRut(String rut) {
        if (!rut.matches("^\\d{7,}-[\\dkK]$")) return false;

        final String[] rutParts = rut.split("-");
        final String digits = rutParts[0];
        final String expectedVerificationDigit = rutParts[1];
        final String verificationDigit = calculateVerificationCode(digits);

        return expectedVerificationDigit.equals(verificationDigit);
    }

    public static String intColorToHexString(int color) {
        final String hex = Integer.toHexString(color);
        return "#" + "0".repeat(6 - hex.length()) + hex;
    }

    public static <T> ArrayList<T> jsonArrayToList(JSONArray jsonArray, Class<T> of) throws JSONException {
        final ArrayList<T> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            final Object obj = jsonArray.get(i);
            if (obj == null) {
                throw new JSONException("Invalid " + of.getSimpleName() + " at index " + i + " .");
            }

            try {
                list.add(of.cast(obj));
            } catch (ClassCastException e) {
                try {
                    list.add(of.getDeclaredConstructor(obj.getClass()).newInstance(obj));
                } catch (Exception ex) {
                    throw new JSONException("Invalid " + of.getSimpleName() + " at index " + i + " .");
                }
            }
        }

        return list;
    }

    private static @Nullable String calculateVerificationCode(String digits) {
        if (Integer.parseInt(digits) < 1e6) return null;

        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            sum += Integer.parseInt(digits.charAt(digits.length() - i - 1) + "")
                   * RUT_VALIDATION_SEQUENCE[i % RUT_VALIDATION_SEQUENCE.length];
        }

        final int verificationNumber = 11 - sum + (sum / 11 * 11);
        return verificationNumber == 10 ? "K" : String.valueOf(verificationNumber % 11);
    }
}
