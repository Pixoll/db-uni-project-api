package org.dbuniproject.api.json;

import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import jakarta.annotation.Nonnull;
import org.dbuniproject.api.endpoints.EndpointException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Collection;

public class JSONMapper implements JsonMapper {
    @Nonnull
    @Override
    public String toJsonString(@Nonnull Object object, @Nonnull Type type) {
        if (object instanceof JSONObject json) {
            return json.toString();
        }

        if (object instanceof JSONArray array) {
            return array.toString();
        }

        if (object instanceof Collection<?> collection) {
            return new JSONArray(collection.stream().map(element -> {
                if (element instanceof JSONEncodable encodable) {
                    return encodable.toJSON();
                }

                return element;
            }).toList()).toString();
        }

        if (object instanceof JSONEncodable encodable) {
            return encodable.toJSON().toString();
        }

        return object.toString();
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
