package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.db.structures.Supplier;

import java.sql.SQLException;
import java.util.ArrayList;

public class SuppliersEndpoint extends Endpoint implements Endpoint.GetMethod {
    public SuppliersEndpoint() {
        super("/suppliers");
    }

    @Override
    public void get(Context ctx) throws EndpointException {
        final SessionTokenManager.Token sessionToken = getSessionToken(ctx);
        if (sessionToken == null || !sessionToken.isManager()) {
            throw new EndpointException(HttpStatus.UNAUTHORIZED, "Not a manager.");
        }

        final String supplierRut = ctx.queryParam("rut");
        final String supplierEmail = ctx.queryParam("email");
        final Integer supplierPhone = Util.getQueryParam(ctx, "rut", Integer.class);

        if ((supplierRut != null && !supplierRut.isEmpty())
            || (supplierEmail != null && !supplierEmail.isEmpty())
            || supplierPhone != null
        ) {
            try (final DatabaseConnection db = new DatabaseConnection()) {
                final Supplier supplier = db.getSupplier(
                        supplierRut != null ? supplierRut : "",
                        supplierEmail != null ? supplierEmail : "",
                        supplierPhone != null ? supplierPhone : 0
                );
                if (supplier == null) {
                    throw new EndpointException(HttpStatus.NOT_FOUND, "Supplier does not exist.");
                }

                ctx.status(HttpStatus.OK).json(supplier);
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        final ArrayList<Integer> brands = Util.getQueryParamAsIntegerList(ctx, "brand");
        final ArrayList<Integer> communes = Util.getQueryParamAsIntegerList(ctx, "commune");

        try (final DatabaseConnection db = new DatabaseConnection()) {
            ctx.status(HttpStatus.OK).json(db.getSuppliers(brands, communes));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
