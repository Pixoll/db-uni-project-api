package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ProductsEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsEndpoint() {
        super("/products");
    }

    @Override
    public void get(Context ctx) {
        final String name = ctx.queryParam("name");
        final List<Integer> types = Util.queryListToIntegerList(ctx.queryParams("type"));
        final List<Integer> sizes = Util.queryListToIntegerList(ctx.queryParams("size"));
        final List<Integer> colors = Util.queryListToIntegerList(ctx.queryParams("color"));
        final List<Integer> brands = Util.queryListToIntegerList(ctx.queryParams("brand"));
        final List<Integer> regions = Util.queryListToIntegerList(ctx.queryParams("region"));
        final List<Integer> communes = Util.queryListToIntegerList(ctx.queryParams("commune"));
        final List<String> sortBy = ctx.queryParams("sortBy").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .toList();
        Integer minPrice;
        Integer maxPrice;

        try {
            minPrice = ctx.queryParamAsClass("minPrice", Integer.class).getOrDefault(null);
        } catch (Exception ignored) {
            minPrice = null;
        }

        try {
            maxPrice = ctx.queryParamAsClass("maxPrice", Integer.class).getOrDefault(null);
        } catch (Exception ignored) {
            maxPrice = null;
        }

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
