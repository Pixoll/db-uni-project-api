package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;

public abstract class Person extends Structure {
    @Nonnull
    public final String rut;
    @Nonnull
    public String firstName;
    @Nonnull
    public String secondName;
    @Nonnull
    public String firstLastName;
    @Nonnull
    public String secondLastName;
    @Nonnull
    public String email;
    public int phone;

    public Person(
            @Nonnull String rut,
            @Nonnull String firstName,
            @Nonnull String secondName,
            @Nonnull String firstLastName,
            @Nonnull String secondLastName,
            @Nonnull String email,
            int phone
    ) {
        this.rut = rut;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.phone = phone;
    }
}
