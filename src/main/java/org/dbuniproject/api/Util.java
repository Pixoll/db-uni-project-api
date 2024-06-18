package org.dbuniproject.api;

import io.javalin.http.Context;
import jakarta.annotation.Nullable;

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
}
