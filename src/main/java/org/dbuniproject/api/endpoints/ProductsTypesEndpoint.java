package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.ProductType;

import java.sql.SQLException;

public class ProductsTypesEndpoint extends Endpoint implements Endpoint.GetMethod {
    public ProductsTypesEndpoint() {
        super("/products/types");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final Validator<Integer> id = ctx.queryParamAsClass("id", Integer.class);
        final String name = ctx.queryParam("name");

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (id.hasValue()) {
                final ProductType type = db.getProductType(id.get());
                if (type == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Product type does not exist.");
                }

                ctx.status(HttpStatus.OK).json(type);
                return;
            }

            if (name != null && !name.isEmpty()) {
                final ProductType type = db.getProductType(name);
                if (type == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Product type does not exist.");
                }

                ctx.status(HttpStatus.OK).json(type);
                return;
            }

            ctx.status(HttpStatus.OK).json(db.getProductTypes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
