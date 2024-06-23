package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;

public class EmployeesSalariesEndpoint extends Endpoint implements Endpoint.GetMethod {
    public EmployeesSalariesEndpoint() {
        super("/employees/salaries");
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final String cashierRut = ctx.queryParam("rut");
        if (cashierRut == null || cashierRut.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Expected rut in query.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (!db.doesCashierExist(cashierRut)) {
                throw new EndpointException(
                        HttpStatus.NOT_FOUND,
                        "Cashier with rut " + cashierRut + " does not exist."
                );
            }

            ctx.status(HttpStatus.OK).json(db.getCashierSalaryHistory(cashierRut));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
