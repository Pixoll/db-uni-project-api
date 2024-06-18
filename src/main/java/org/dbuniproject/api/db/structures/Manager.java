package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Manager extends Employee {
    public Manager(
            @Nonnull String rut,
            @Nonnull String firstName,
            @Nonnull String secondName,
            @Nonnull String firstLastName,
            @Nonnull String secondLastName,
            @Nonnull String email,
            int phone,
            @Nonnull String password,
            @Nonnull String salt
    ) {
        super(rut, firstName, secondName, firstLastName, secondLastName, email, phone, password, salt);
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("rut", this.rut)
                .put("firstName", this.firstName)
                .put("secondName", this.secondName)
                .put("firstLastName", this.firstLastName)
                .put("secondLastName", this.secondLastName)
                .put("email", this.email)
                .put("phone", this.phone)
                .put("password", this.password);
    }

    @Nonnull
    @Override
    public Manager clone() {
        return new Manager(
                this.rut,
                this.firstName,
                this.secondName,
                this.firstLastName,
                this.secondLastName,
                this.email,
                this.phone,
                this.password,
                this.salt
        );
    }
}
