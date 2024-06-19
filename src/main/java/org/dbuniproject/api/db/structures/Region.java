package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.json.JSONEncodable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;

public record Region(
        short number,
        @Nonnull String name,
        @Nonnull ArrayList<Commune> communes
) implements JSONEncodable {
    public void addCommune(short id, @Nonnull String name) {
        communes.add(new Commune(id, name));
    }

    @NotNull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("number", this.number)
                .put("name", this.name)
                .put("communes", this.communes.stream().map(Commune::toJSON).toList());
    }
}
