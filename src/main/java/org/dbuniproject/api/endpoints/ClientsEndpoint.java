package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.Client;
import org.dbuniproject.api.db.structures.ValidationException;
import org.json.JSONObject;

import java.sql.SQLException;

public class ClientsEndpoint extends Endpoint implements Endpoint.GetMethod, Endpoint.PostMethod {
    public ClientsEndpoint() {
        super("/clients");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        if (getSessionToken(ctx) == null) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not an employee.");
        }

        final String rut = ctx.queryParam("rut");
        if (rut == null || rut.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Expected rut in query.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final Client client = db.getClient(rut);
            if (client == null) {
                throw new EndpointException(HttpStatus.NOT_FOUND, "Client does not exist.");
            }

            ctx.status(HttpStatus.OK).json(client);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void post(Context ctx) throws EndpointException {
        if (getSessionToken(ctx) == null) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not an employee.");
        }

        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final Client client;

        try {
            client = new Client(body);
        } catch (ValidationException e) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (db.doesClientExist(client)) {
                throw new EndpointException(HttpStatus.CONFLICT, "Client with that rut, email or phone already exists.");
            }

            db.insertClient(client);
            ctx.status(HttpStatus.CREATED);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
