package org.dbuniproject.api;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class SessionTokenManager {
    private static final File TOKENS_FILE = new File("data/tokens.json");
    private static final JSONObject TOKENS = new JSONObject()
            .put(Token.Type.MANAGER.toString(), new JSONObject())
            .put(Token.Type.CASHIER.toString(), new JSONObject());

    public static void loadSessionTokens() {
        if (!TOKENS_FILE.exists()) {
            //noinspection ResultOfMethodCallIgnored
            TOKENS_FILE.getParentFile().mkdirs();
            try {
                //noinspection ResultOfMethodCallIgnored
                TOKENS_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            saveSessionTokens();
            return;
        }

        final JSONObject json = Util.readJSONObjectFile(TOKENS_FILE);

        TOKENS.put(Token.Type.MANAGER.toString(), json.getJSONObject(Token.Type.MANAGER.toString()));
        TOKENS.put(Token.Type.CASHIER.toString(), json.getJSONObject(Token.Type.CASHIER.toString()));
    }

    @Nonnull
    public static Token generateSessionToken(@Nonnull Token.Type type, @Nonnull String rut) {
        final JSONObject destination = TOKENS.getJSONObject(type.toString());
        if (destination.has(rut)) {
            revokeSessionToken(type, rut);
        }

        String token;
        do {
            byte[] randomBytes = new byte[64];
            new SecureRandom().nextBytes(randomBytes);
            token = Base64.getEncoder().encodeToString(randomBytes);
        } while (destination.has(token));

        destination.put(rut, token);
        destination.put(token, rut);
        saveSessionTokens();

        return new Token(token, rut, type);
    }

    @Nullable
    public static Token getSessionToken(@Nonnull String token) {
        for (final Token.Type type : Token.Type.values()) {
            final String rut = TOKENS.getJSONObject(type.name).optString(token, null);
            if (rut != null) {
                return new Token(token, rut, type);
            }
        }

        return null;
    }

    public static void revokeSessionToken(@Nonnull Token.Type type, @Nonnull String rut) {
        final JSONObject source = TOKENS.getJSONObject(type.name);
        if (!source.has(rut)) return;

        final String token = source.getString(rut);

        source.remove(rut);
        source.remove(token);
        saveSessionTokens();
    }

    public static void revokeSessionToken(@Nonnull Token token) {
        revokeSessionToken(token.type, token.rut);
    }

    private static void saveSessionTokens() {
        try (FileWriter fileWriter = new FileWriter(TOKENS_FILE)) {
            fileWriter.write(TOKENS.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record Token(@Nonnull String token, @Nonnull String rut, @Nonnull Type type) {
        public boolean isCashier() {
            return this.type == Type.CASHIER;
        }

        public boolean isManager() {
            return this.type == Type.MANAGER;
        }

        public enum Type {
            CASHIER("cashier"),
            MANAGER("manager");

            private final String name;

            Type(final String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return this.name;
            }
        }
    }
}
