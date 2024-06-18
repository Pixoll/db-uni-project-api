package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;

public abstract class Employee extends Person {
    @Nonnull
    public final String email;
    @Nonnull
    public String password;
    @Nonnull
    public String salt;
    public int storeId;

    public Employee(
            @Nonnull String rut,
            @Nonnull String firstName,
            @Nonnull String secondName,
            @Nonnull String firstLastName,
            @Nonnull String secondLastName,
            @Nonnull String email,
            int phone,
            @Nonnull String password,
            @Nonnull String salt,
            int storeId
    ) {
        super(rut, firstName, secondName, firstLastName, secondLastName, email, phone);

        this.email = email;
        this.password = password;
        this.salt = salt;
        this.storeId = storeId;
    }
}
