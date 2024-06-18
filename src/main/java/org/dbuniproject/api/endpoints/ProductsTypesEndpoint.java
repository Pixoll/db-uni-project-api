package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.ProductType;
import org.json.JSONObject;

import java.sql.SQLException;

public class ProductsTypesEndpoint extends Endpoint implements Endpoint.GetMethod, Endpoint.PostMethod {
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

    @Override
    public void post(Context ctx) throws EndpointException {
        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final String name = body.optString("name");
        final String description = body.optString("description");

        if (name.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Product size name cannot be empty.");
        }

        if (description.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Product size description cannot be empty.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (db.getProductType(name) != null) {
                throw new EndpointException(HttpStatus.BAD_REQUEST, "Product type already exists.");
            }

            db.insertProductType(name, description);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
