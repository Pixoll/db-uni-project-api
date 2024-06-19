package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.json.JSONEncodable;
import org.json.JSONObject;

public record Cashier(
        @Nonnull String rut,
        @Nonnull String firstName,
        @Nonnull String secondName,
        @Nonnull String firstLastName,
        @Nonnull String secondLastName,
        @Nonnull String email,
        int phone,
        boolean fullTime,
        @Nonnull String password,
        @Nonnull String salt
) implements JSONEncodable {
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
                .put("password", this.password);
    }
}
