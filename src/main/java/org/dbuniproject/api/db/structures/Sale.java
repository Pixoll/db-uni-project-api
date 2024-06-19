package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.json.JSONEncodable;
import org.json.JSONObject;

import java.util.Date;

public record Sale(
        long id,
        @Nonnull Date date,
        @Nonnull String cashierRut,
        @Nonnull String clientRut
) implements JSONEncodable {
    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("date", this.date)
                .put("cashierRut", this.cashierRut)
                .put("clientRut", this.clientRut);
    }
}
