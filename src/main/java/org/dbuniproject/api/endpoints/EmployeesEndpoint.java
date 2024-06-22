package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.Cashier;
import org.dbuniproject.api.db.structures.ValidationException;
import org.json.JSONObject;

import java.sql.SQLException;

public class EmployeesEndpoint
        extends Endpoint
        implements Endpoint.GetMethod, Endpoint.PostMethod, Endpoint.DeleteMethod {
    public EmployeesEndpoint() {
        super("/employees");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final String cashierRut = ctx.queryParam("rut");
            if (cashierRut != null && !cashierRut.isEmpty()) {
                final Cashier cashier = db.getCashier(cashierRut);
                if (cashier == null) {
                    throw new EndpointException(
                            HttpStatus.NOT_FOUND,
                            "Cashier with rut " + cashierRut + " does not exist or was fired."
                    );
                }

                ctx.status(HttpStatus.OK).json(cashier);
                return;
            }

            ctx.status(HttpStatus.OK).json(db.getCashiers(sessionToken.rut()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void post(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final String password = Util.generatePassword();
        final String salt = Util.generateSalt();
        body.put("password", Util.hashPassword(password, salt));
        body.put("salt", salt);

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final Integer storeId = db.getManagerStoreId(sessionToken.rut());
            if (storeId == null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                throw new RuntimeException("Could not determine store if from token rut " + sessionToken.rut() + ".");
            }

            body.put("storeId", storeId);
            final Cashier cashier;

            try {
                cashier = new Cashier(body);
            } catch (ValidationException e) {
                throw new EndpointException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            if (db.doesEmployeeExist(cashier.rut(), cashier.email(), cashier.phone())) {
                throw new EndpointException(
                        HttpStatus.CONFLICT,
                        "Employee with that rut, email or phone already exists."
                );
            }

            db.insertCashier(cashier);
            ctx.status(HttpStatus.CREATED).json(new JSONObject()
                    .put("generatedPassword", password)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final String cashierRut = ctx.queryParam("rut");
        if (cashierRut == null || cashierRut.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Expected rut in query.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (db.isCashierFired(cashierRut)) {
                throw new EndpointException(HttpStatus.CONFLICT, "Cashier was already fired.");
            }

            db.markCashierAsFired(cashierRut);
            SessionTokenManager.revokeSessionToken(SessionTokenManager.Token.Type.CASHIER, cashierRut);

            ctx.status(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
