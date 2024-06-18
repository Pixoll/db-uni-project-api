package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;

public class Region extends Structure {
    public final short number;
    @Nonnull
    public final ArrayList<Commune> communes;
    @Nonnull
    public String name;

    public Region(short number, @Nonnull String name) {
        this.number = number;
        this.name = name;
        this.communes = new ArrayList<>();
    }

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

    @Nonnull
    @Override
    public Region clone() {
        final Region newRegion = new Region(this.number, this.name);
        for (final Commune commune : this.communes) {
            newRegion.communes.add(commune.clone());
        }
        return newRegion;
    }
}
