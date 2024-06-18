package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;

public class StoresEndpoint extends Endpoint implements Endpoint.GetMethod {
    public StoresEndpoint() {
        super("/stores");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        try (final DatabaseConnection db = new DatabaseConnection()) {
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
