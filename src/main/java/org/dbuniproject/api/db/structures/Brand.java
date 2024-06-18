package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Brand extends Structure {
    public final int id;
    @Nonnull
    public String name;

    public Brand(int id, @Nonnull String name) {
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
    public Brand clone() {
        return new Brand(this.id, this.name);
    }
}
