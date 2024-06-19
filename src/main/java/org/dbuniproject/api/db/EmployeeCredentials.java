package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;

public record EmployeeCredentials(@Nonnull String password, @Nonnull String salt) {
}
