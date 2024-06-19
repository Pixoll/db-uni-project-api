package org.dbuniproject.api.json;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public interface JSONEncodable {
    @Nonnull
    JSONObject toJSON();
}
