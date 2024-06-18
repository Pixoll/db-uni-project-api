package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Store extends Structure {
    public final int id;
    @Nonnull
    public String name;
    @Nonnull
    public final String addressStreet;
    public final short addressNumber;
    public final short communeId;

    public Store(int id, @Nonnull String name, @Nonnull String addressStreet, short addressNumber, short communeId) {
        this.id = id;
        this.name = name;
        this.addressStreet = addressStreet;
        this.addressNumber = addressNumber;
        this.communeId = communeId;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("name", this.name)
                .put("addressStreet", this.addressStreet)
                .put("addressNumber", this.addressNumber)
                .put("communeId", this.communeId);
    }

    @Nonnull
    @Override
    public Store clone() {
        return new Store(this.id, this.name, this.addressStreet, this.addressNumber, this.communeId);
    }
}
