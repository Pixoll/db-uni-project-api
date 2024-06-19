package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.EmployeeCredentials;
import org.json.JSONObject;

import java.sql.SQLException;

public class EmployeesSessionsEndpoint extends Endpoint implements Endpoint.PostMethod, Endpoint.DeleteMethod {
    public EmployeesSessionsEndpoint() {
        super("/employees/sessions");
    }

    @Override
    public void post(Context ctx) throws EndpointException {
        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final String email = body.optString("email");
        final String password = body.optString("password");
        final String typeString = body.optString("type");
        final SessionTokenManager.Token.Type type = Util.stringToEnum(typeString, SessionTokenManager.Token.Type.class);

        if (email.isEmpty() || password.isEmpty() || typeString.isEmpty()) {
            throw new EndpointException(
                    HttpStatus.BAD_REQUEST,
                    "Expected email, password and type in the request body."
            );
        }

        if (type == null) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Invalid employee type.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final EmployeeCredentials credentials = db.getEmployeeCredentials(email, type);
            if (credentials == null) {
                throw new EndpointException(
                        HttpStatus.NOT_FOUND,
                        "No credentials found for " + type + " with email " + email + "."
                );
            }

            if (!Util.hashPassword(password, credentials.salt()).equals(credentials.password())) {
                throw new EndpointException(HttpStatus.UNAUTHORIZED, "Wrong password.");
            }

            ctx.status(HttpStatus.OK).json(new JSONObject().put("session_token",
                    SessionTokenManager.generateSessionToken(type, email)
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        SessionTokenManager.revokeSessionToken(sessionToken);

        ctx.status(HttpStatus.NO_CONTENT);
    }
}
