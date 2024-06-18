package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Cashier extends Employee {
    public boolean fullTime;

    public Cashier(
            @Nonnull String rut,
            @Nonnull String firstName,
            @Nonnull String secondName,
            @Nonnull String firstLastName,
            @Nonnull String secondLastName,
            @Nonnull String email,
            int phone,
            boolean fullTime,
            @Nonnull String password,
            @Nonnull String salt,
            int storeId
    ) {
        super(rut, firstName, secondName, firstLastName, secondLastName, email, phone, password, salt, storeId);

        this.fullTime = fullTime;
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
                .put("fullTime", this.fullTime)
                .put("password", this.password)
                .put("storeId", this.storeId);
    }

    @Nonnull
    @Override
    public Cashier clone() {
        return new Cashier(
                this.rut,
                this.firstName,
                this.secondName,
                this.firstLastName,
                this.secondLastName,
                this.email,
                this.phone,
                this.fullTime,
                this.password,
                this.salt,
                this.storeId
        );
    }
}
