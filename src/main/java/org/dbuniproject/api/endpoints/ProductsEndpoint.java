package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class ProductsEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsEndpoint() {
        super("/products");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken != null) {
            try (final DatabaseConnection db = new DatabaseConnection()) {
                ctx.status(HttpStatus.OK).json(db.getProductsByEmployee(sessionToken.rut()));
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        final Long sku = Util.getQueryParam(ctx, "sku", Long.class);
        if (sku != null) {
            try (final DatabaseConnection db = new DatabaseConnection()) {
                final JSONObject product = db.getProduct(sku);
                if (product == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Product does not exist.");
                }

                ctx.status(HttpStatus.OK).json(product);
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        final String name = ctx.queryParam("name");
        final List<Integer> types = Util.getQueryParamAsIntegerList(ctx, "type");
        final List<Integer> sizes = Util.getQueryParamAsIntegerList(ctx, "size");
        final List<Integer> colors = Util.getQueryParamAsIntegerList(ctx, "color");
        final List<Integer> brands = Util.getQueryParamAsIntegerList(ctx, "brand");
        final List<Integer> regions = Util.getQueryParamAsIntegerList(ctx, "region");
        final List<Integer> communes = Util.getQueryParamAsIntegerList(ctx, "commune");
        final Integer minPrice = Util.getQueryParam(ctx, "minPrice", Integer.class);
        final Integer maxPrice = Util.getQueryParam(ctx, "maxPrice", Integer.class);
        final String sortByNameString = ctx.queryParam("sortByName");
        final String sortByPriceString = ctx.queryParam("sortByPrice");

        final Boolean sortByName = sortByNameString == null ? null
                : sortByNameString.equalsIgnoreCase("asc") ? Boolean.TRUE
                : sortByNameString.equalsIgnoreCase("desc") ? Boolean.FALSE
                : null;
        final Boolean sortByPrice = sortByPriceString == null ? null
                : sortByPriceString.equalsIgnoreCase("asc") ? Boolean.TRUE
                : sortByPriceString.equalsIgnoreCase("desc") ? Boolean.FALSE
                : null;

        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(db.getProducts(
                    name,
                    types,
                    sizes,
                    brands,
                    colors,
                    regions,
                    communes,
                    minPrice,
                    maxPrice,
                    sortByName,
                    sortByPrice
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
