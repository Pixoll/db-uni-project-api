package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;

public class ProductsTypesEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsTypesEndpoint() {
        super("/products/types");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(db.getProductTypes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
