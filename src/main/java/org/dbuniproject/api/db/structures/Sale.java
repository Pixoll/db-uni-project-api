package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.json.JSONEncodable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public record Sale(
        long id,
        @Nonnull Date date,
        @Nonnull String cashierRut,
        @Nonnull String clientRut,
        @Nonnull ArrayList<ProductSale> productSales
) implements JSONEncodable, Validatable {
    private static final Date MISSING_DATE = new Date(-1);
    private static final int ONE_MINUTE = 60_000;

    public Sale(JSONObject json) throws ValidationException {
        this(
                -1,
                new Date(json.optLong("date", -1)),
                json.optString("cashierRut"),
                json.optString("clientRut"),
                Util.jsonArrayToList(json.optJSONArray("productSales", new JSONArray()), ProductSale.class)
        );

        this.validate();
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", this.id)
                .put("date", this.date)
                .put("cashierRut", this.cashierRut)
                .put("clientRut", this.clientRut)
                .put("productSales", this.productSales.stream().map(ProductSale::toJSON));
    }

    @Override
    public void validate(@NotNull String parentName) throws ValidationException {
        if (this.date.equals(MISSING_DATE)) {
            throw new ValidationException("date", "Date is empty.");
        }

        final Date now = new Date();
        if (this.date.after(now) || this.date.before(new Date(now.getTime() - ONE_MINUTE))) {
            throw new ValidationException("date", "Date is out of bounds.");
        }

        if (this.cashierRut.isEmpty()) {
            throw new ValidationException("cashierRut", "Cashier rut is empty.");
        }

        if (this.clientRut.isEmpty()) {
            throw new ValidationException("clientRut", "Client rut is empty.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (!db.doesCashierExist(this.cashierRut)) {
                throw new ValidationException("cashierRut", "Cashier does not exist.");
            }

            if (!db.doesClientExist(this.clientRut)) {
                throw new ValidationException("clientRut", "Client does not exist.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (this.productSales.isEmpty()) {
            throw new ValidationException("productSales", "Product sales is empty.");
        }

        for (int i = 0; i < this.productSales.size(); i++) {
            final ProductSale productSale = this.productSales.get(i);
            productSale.validate("productSale[" + i + "]");
        }
    }
}
