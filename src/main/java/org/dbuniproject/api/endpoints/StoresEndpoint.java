package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.json.JSONObject;

import java.sql.SQLException;

public class StoresEndpoint extends Endpoint implements Endpoint.GetMethod {
    public StoresEndpoint() {
        super("/stores");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final Integer id = Util.getQueryParam(ctx, "id", Integer.class);
        if (id != null) {
            try (final DatabaseConnection db = new DatabaseConnection()) {
                final JSONObject store = db.getStore(id);
                if (store == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Store does not exist.");
                }

                ctx.status(HttpStatus.OK).json(store);
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(db.getStores());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
