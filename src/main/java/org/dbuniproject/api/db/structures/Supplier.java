package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.json.JSONEncodable;
import org.json.JSONObject;

public record Supplier(
        @Nonnull String rut,
        @Nonnull String firstName,
        @Nonnull String secondName,
        @Nonnull String firstLastName,
        @Nonnull String secondLastName,
        @Nonnull String email,
        int phone,
        @Nonnull String addressStreet,
        short addressNumber,
        short communeId
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
                .put("addressStreet", this.addressStreet)
                .put("addressNumber", this.addressNumber)
                .put("communeId", this.communeId);
    }
}

