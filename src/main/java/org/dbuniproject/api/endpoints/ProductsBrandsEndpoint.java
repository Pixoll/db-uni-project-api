package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.db.DatabaseConnection;
import org.json.JSONObject;

import java.sql.SQLException;

public class ProductsBrandsEndpoint extends Endpoint implements Endpoint.GetMethod, Endpoint.PostMethod {
    public ProductsBrandsEndpoint() {
        super("/products/brands");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final Validator<Integer> id = ctx.queryParamAsClass("id", Integer.class);
        final String name = ctx.queryParam("name");

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (id.hasValue()) {
                final JSONObject brand = db.getBrand(id.get());
                if (brand == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Brand does not exist.");
                }

                ctx.status(HttpStatus.OK).json(brand);
                return;
            }

            if (name != null && !name.isEmpty()) {
                final JSONObject brand = db.getBrand(name);
                if (brand == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Brand does not exist.");
                }

                ctx.status(HttpStatus.OK).json(brand);
                return;
            }

            ctx.status(HttpStatus.OK).json(db.getBrands());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void post(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final JSONObject body = ctx.bodyAsClass(JSONObject.class);
        final String name = body.optString("name");
        if (name.isEmpty()) {
            throw new EndpointException(HttpStatus.BAD_REQUEST, "Brand name cannot be empty.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (db.getBrand(name) != null) {
                throw new EndpointException(HttpStatus.CONFLICT, "Brand already exists.");
            }

            db.insertBrand(name);
            ctx.status(HttpStatus.CREATED);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
