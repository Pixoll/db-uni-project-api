package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.Sale;
import org.dbuniproject.api.db.structures.ValidationException;
import org.json.JSONObject;

import java.sql.SQLException;

public class SalesEndpoint extends Endpoint implements Endpoint.GetMethod, Endpoint.PostMethod {
    public SalesEndpoint() {
        super("/sales");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not an employee.");
        }

        final Integer id = Util.getQueryParam(ctx, "id", Integer.class);
        if (id != null) {
            try (final DatabaseConnection db = new DatabaseConnection()) {
                final Sale sale = db.getSale(id);
                if (sale == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Sale " + id + " does not exist.");
                }

                ctx.status(HttpStatus.OK).json(sale);
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(sessionToken.isCashier()
                    ? db.getSalesOfCashier(sessionToken.rut())
                    : db.getSalesInStore(sessionToken.rut())
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void post(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isCashier()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a cashier.");
        }

        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final Sale sale;

        try {
            sale = new Sale(body, sessionToken.rut());
        } catch (ValidationException e) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final long newSaleId = db.insertSale(sale);
            ctx.status(HttpStatus.CREATED).json(new JSONObject()
                    .put("id", newSaleId)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
