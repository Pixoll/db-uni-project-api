package org.dbuniproject.api;

import com.google.common.hash.Hashing;
import io.javalin.http.Context;
import jakarta.annotation.Nullable;
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
}
