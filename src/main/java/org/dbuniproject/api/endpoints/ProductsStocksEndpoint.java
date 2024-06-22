package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.ProductStock;
import org.json.JSONObject;

import java.sql.SQLException;

public class ProductsStocksEndpoint extends Endpoint implements Endpoint.GetMethod, Endpoint.PatchMethod {
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

    @Override
    public void patch(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final long sku = body.optLong("sku", -1);
        final int min = body.optInt("min", -1);
        final int max = body.optInt("max", -1);
        final int forSale = body.optInt("forSale", -1);
        final int inStorage = body.optInt("inStorage", -1);

        if (sku == -1) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Expected sku in request body.");
        }

        if (min == -1 && max == -1 && forSale == -1 && inStorage == -1) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Required parameters missing in request body.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            final ProductStock stock = db.getProductStock(sku, sessionToken.rut());
            if (stock == null) {
                throw new EndpointException(HttpStatus.NOT_FOUND, "Product does not exist.");
            }

            if (min != -1) stock.min = min;
            if (max != -1) stock.max = max;
            if (forSale != -1) stock.forSale = forSale;
            if (inStorage != -1) stock.inStorage = inStorage;

            if (stock.max <= stock.min) {
                throw new EndpointException(HttpStatus.BAD_REQUEST, "Max must be greater than min.");
            }

            if (stock.forSale < 0) {
                throw new EndpointException(HttpStatus.BAD_REQUEST, "Stock for sale must be greater than zero.");
            }

            if (stock.inStorage < 0) {
                throw new EndpointException(HttpStatus.BAD_REQUEST, "Stock in storage must be greater than zero.");
            }

            db.updateProductStock(stock);
            ctx.status(HttpStatus.OK);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
