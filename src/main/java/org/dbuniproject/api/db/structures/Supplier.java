package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Supplier extends Person {
    @Nonnull
    public String addressStreet;
    public short addressNumber;
    public short communeId;

    public Supplier(
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
    ) {
        super(rut, firstName, secondName, firstLastName, secondLastName, email, phone);

        this.addressStreet = addressStreet;
        this.addressNumber = addressNumber;
        this.communeId = communeId;
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
                .put("addressStreet", this.addressStreet)
                .put("addressNumber", this.addressNumber)
                .put("communeId", this.communeId);
    }

    @Nonnull
    @Override
    public Supplier clone() {
        return new Supplier(
                this.rut,
                this.firstName,
                this.secondName,
                this.firstLastName,
                this.secondLastName,
                this.email,
                this.phone,
                this.addressStreet,
                this.addressNumber,
                this.communeId
        );
    }
}
