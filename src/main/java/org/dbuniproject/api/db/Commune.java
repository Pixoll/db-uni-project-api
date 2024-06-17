package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Commune extends Structure {
    public final int id;
    @Nonnull
    public final String name;

    public Commune(int id, @Nonnull String name) {
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
    public Commune clone() {
        return new Commune(this.id, this.name);
    }
}
