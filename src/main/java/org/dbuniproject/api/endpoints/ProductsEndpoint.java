package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ProductsEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsEndpoint() {
        super("/products");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
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
        final List<String> sortBy = ctx.queryParams("sortBy").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .toList();
        final Integer minPrice = Util.getQueryParam(ctx, "minPrice", Integer.class);
        final Integer maxPrice = Util.getQueryParam(ctx, "maxPrice", Integer.class);

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
                    sortBy.contains("name"),
                    sortBy.contains("price")
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
