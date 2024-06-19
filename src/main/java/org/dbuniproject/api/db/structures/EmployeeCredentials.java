package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;

public record EmployeeCredentials(@Nonnull String password, @Nonnull String salt) {
}
