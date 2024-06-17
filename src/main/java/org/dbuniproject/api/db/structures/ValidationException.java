package org.dbuniproject.api.db.structures;

public class ValidationException extends Exception {
    public ValidationException(String key, String message) {
        super("Validation error at '" + key + "': " + message);
    }
}
