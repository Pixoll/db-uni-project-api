package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Client extends Person {
    public Client(
            @Nonnull String rut,
            @Nonnull String firstName,
            @Nonnull String secondName,
            @Nonnull String firstLastName,
            @Nonnull String secondLastName,
            @Nonnull String email,
            int phone
    ) {
        super(rut, firstName, secondName, firstLastName, secondLastName, email, phone);
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
                .put("phone", this.phone);
    }

    @Nonnull
    @Override
    public Client clone() {
        return new Client(this.rut, this.firstName, this.secondName, this.firstLastName, this.secondLastName,
                this.email, this.phone);
    }
}
