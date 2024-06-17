package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;

public interface Validatable {
    void validate(@Nonnull String parentName) throws ValidationException;

    default void validate() throws ValidationException {
        this.validate("");
    }
}
