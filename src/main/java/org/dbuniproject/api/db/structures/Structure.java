package org.dbuniproject.api.db.structures;

import org.json.JSONObject;

import jakarta.annotation.Nonnull;

public abstract class Structure {
    @Nonnull
    public abstract JSONObject toJSON();

    public boolean jsonEquals(@Nonnull Structure other) {
        return this.toJSON().toString().equals(other.toJSON().toString());
    }

    @Nonnull
    public String toString() {
        return this.getClass().getSimpleName() + " " + this.toJSON().toString(2);
    }

    @Nonnull
    abstract public Structure clone();
}
