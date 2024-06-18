package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.Brand;

import java.sql.SQLException;

public class BrandsEndpoint extends Endpoint implements Endpoint.GetMethod {
    public BrandsEndpoint() {
        super("/brands");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final Validator<Integer> id = ctx.queryParamAsClass("id", Integer.class);
        final String name = ctx.queryParam("name");

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (id.hasValue()) {
                final Brand brand = db.getBrand(id.get());
                if (brand == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Brand does not exist.");
                }

                ctx.status(HttpStatus.OK).json(brand);
                return;
            }

            if (name != null && !name.isEmpty()) {
                final Brand brand = db.getBrand(name);
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
}
