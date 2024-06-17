package org.dbuniproject.api;

import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import org.dbuniproject.api.db.Structure;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.dbuniproject.api.endpoints.EndpointException;

import jakarta.annotation.Nonnull;

import java.lang.reflect.Type;
import java.util.Collection;

public class JSONMapper implements JsonMapper {
    @Nonnull
    @Override
    public String toJsonString(@Nonnull Object obj, @Nonnull Type type) {
        if (obj instanceof JSONObject json) {
            return json.toString();
        }

        if (obj instanceof JSONArray array) {
            return array.toString();
        }

        if (obj instanceof Collection<?> collection) {
            return new JSONArray(collection.stream().map(element -> {
                if (element instanceof Structure structure) {
                    return structure.toJSON();
                }

                return element;
            }).toList()).toString();
        }

        if (obj instanceof Structure structure) {
            return structure.toJSON().toString();
        }

        return obj.toString();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T> T fromJsonString(@Nonnull String json, @Nonnull Type targetType) {
        try {
            return (T) new JSONObject(json);
        } catch (JSONException e) {
            throw new RuntimeException(
                    new EndpointException(HttpStatus.BAD_REQUEST, "Invalid request body: " + e.getMessage())
            );
        }
    }
}
