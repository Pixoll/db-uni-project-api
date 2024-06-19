package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.json.JSONEncodable;
import org.json.JSONObject;

public record Commune(short id, @Nonnull String name) implements JSONEncodable {
    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("name", this.name);
    }
}
