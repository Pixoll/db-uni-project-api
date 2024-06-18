package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

import java.util.Date;

public class Sale extends Structure {
    public final long id;
    public Date date;
    public String cashierRut;
    public String clientRut;

    public Sale(long id, Date date, String cashierRut, String clientRut) {
        this.id = id;
        this.date = date;
        this.cashierRut = cashierRut;
        this.clientRut = clientRut;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("date", this.date)
                .put("cashierRut", this.cashierRut)
                .put("clientRut", this.clientRut);
    }

    @Nonnull
    @Override
    public Sale clone() {
        return new Sale(this.id, this.date, this.cashierRut, this.clientRut);
    }
}
