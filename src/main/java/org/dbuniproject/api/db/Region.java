package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;

public class Region extends Structure {
    public final int number;
    public final String name;
    public final ArrayList<Commune> communes;

    public Region(int number, String name) {
        this.number = number;
        this.name = name;
        this.communes = new ArrayList<>();
    }

    public void addCommune(int id, String name) {
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
