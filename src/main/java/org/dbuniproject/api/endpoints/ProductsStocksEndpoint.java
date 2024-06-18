package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;

public class ProductsStocksEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsStocksEndpoint() {
        super("/products/stocks");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final Long sku = Util.getQueryParam(ctx, "sku", Long.class);
        if (sku == null) {
            throw new EndpointException(HttpStatus.NOT_FOUND, "Expected sku in query.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (!db.doesProductExist(sku)) {
                throw new EndpointException(HttpStatus.NOT_FOUND, "Product does not exist.");
            }

            ctx.status(HttpStatus.OK).json(db.getProductStocks(sku));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
