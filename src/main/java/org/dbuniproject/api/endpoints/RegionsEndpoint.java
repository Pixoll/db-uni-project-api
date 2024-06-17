package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;

public class RegionsEndpoint extends Endpoint implements Endpoint.GetMethod {
    public RegionsEndpoint() {
        super("/regions");
    }

    @Override
    public void get(Context ctx) {
        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(db.getRegionsWithCommunes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
