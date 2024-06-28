package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;
import java.util.Objects;

public class EmployeesMeEndpoint extends Endpoint implements Endpoint.GetMethod {
    public EmployeesMeEndpoint() {
        super("/employees/me");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not an employee.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(Objects.requireNonNull(sessionToken.isCashier()
                    ? db.getCashier(sessionToken.rut())
                    : db.getManager(sessionToken.rut()))
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not an employee.");
        }
    }
}
