package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class ProductType extends Structure {
    public final int id;
    @Nonnull
    public final String name;
    @Nonnull
    public final String description;

    public ProductType(int id, @Nonnull String name, @Nonnull String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("name", this.name)
                .put("description", this.description);
    }

    @Nonnull
    @Override
    public ProductType clone() {
        return new ProductType(this.id, this.name, this.description);
    }
}
