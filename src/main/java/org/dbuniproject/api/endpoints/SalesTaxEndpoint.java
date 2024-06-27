package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.db.DatabaseConnection;
import org.json.JSONObject;

import java.sql.SQLException;

public class SalesTaxEndpoint extends Endpoint implements Endpoint.GetMethod {
    public SalesTaxEndpoint() {
        super("/sales/tax");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not an employee.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(new JSONObject()
                    .put("tax", db.getSalesTax())
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
