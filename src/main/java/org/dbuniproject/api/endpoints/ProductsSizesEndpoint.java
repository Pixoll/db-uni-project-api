package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.ProductSize;

import java.sql.SQLException;

public class ProductsSizesEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsSizesEndpoint() {
        super("/products/sizes");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final Validator<Integer> id = ctx.queryParamAsClass("id", Integer.class);
        final String name = ctx.queryParam("name");

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (id.hasValue()) {
                final ProductSize size = db.getProductSize(id.get());
                if (size == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Product size does not exist.");
                }

                ctx.status(HttpStatus.OK).json(size);
                return;
            }

            if (name != null && !name.isEmpty()) {
                final ProductSize size = db.getProductSize(name);
                if (size == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Product size does not exist.");
                }

                ctx.status(HttpStatus.OK).json(size);
                return;
            }

            ctx.status(HttpStatus.OK).json(db.getProductSizes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
