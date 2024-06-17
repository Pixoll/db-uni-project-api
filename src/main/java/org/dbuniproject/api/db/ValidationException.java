package org.dbuniproject.api.db;

public class ValidationException extends Exception {
    public ValidationException(String key, String message) {
        super("Validation error at '" + key + "': " + message);
    }
}
