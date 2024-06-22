package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.Cashier;
import org.json.JSONObject;

import java.sql.SQLException;

public class EmployeesContractsEndpoint extends Endpoint implements Endpoint.PatchMethod {
    public EmployeesContractsEndpoint() {
        super("/employees/contracts");
    }

    @Override
    public void patch(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final String cashierRut = ctx.queryParam("rut");
        if (cashierRut == null || cashierRut.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Expected rut in query.");
        }

        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        if (!body.has("fullTime")) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Expected 'fullTime' in request body.");
        }

        final boolean fullTime = body.getBoolean("fullTime");

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final Cashier cashier = db.getCashier(cashierRut);
            if (cashier == null) {
                throw new EndpointException(
                        HttpStatus.BAD_REQUEST,
                        "Cashier with rut " + cashierRut + " does not exist."
                );
            }

            if (cashier.fullTime() == fullTime) {
                throw new EndpointException(
                        HttpStatus.CONFLICT,
                        "Cashier with rut " + cashierRut + " already has a "
                        + (fullTime ? "full" : "part") + "-time contract."
                );
            }

            db.updateCashierContract(cashierRut, fullTime);
            ctx.status(HttpStatus.OK);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
