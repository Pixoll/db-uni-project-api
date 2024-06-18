package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class ProductSize extends Structure {
    public final int id;
    @Nonnull
    public final String name;

    public ProductSize(int id, @Nonnull String name) {
        this.id = id;
        this.name = name;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("name", this.name);
    }

    @Nonnull
    @Override
    public ProductSize clone() {
        return new ProductSize(this.id, this.name);
    }
}
