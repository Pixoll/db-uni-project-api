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
import java.util.Objects;

public record Sale(
        long id,
        @Nonnull Date date,
        @Nonnull String cashierRut,
        @Nonnull String clientRut,
        @Nonnull Type type,
        @Nonnull ArrayList<ProductSale> products
) implements JSONEncodable, Validatable {
    public Sale(JSONObject json) throws ValidationException {
        this(
                -1,
                new Date(),
                json.optString("cashierRut"),
                json.optString("clientRut"),
                Objects.requireNonNullElse(Util.stringToEnum(json.optString("type"), Type.class), Type.INVALID),
                Util.jsonArrayToList(json.optJSONArray("products", new JSONArray()), ProductSale.class)
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
                .put("products", this.products.stream().map(ProductSale::toJSON));
    }

    @Override
    public void validate(@NotNull String parentName) throws ValidationException {
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

        if (this.type == Type.INVALID) {
            throw new ValidationException("type", "Missing or invalid sale type.");
        }

        if (this.products.isEmpty()) {
            throw new ValidationException("products", "Products is empty.");
        }

        for (int i = 0; i < this.products.size(); i++) {
            final ProductSale productSale = this.products.get(i);
            productSale.validate("products[" + i + "]");

            final long sku = productSale.sku();

            try (final DatabaseConnection db = new DatabaseConnection()) {
                if (!db.isProductSoldAtEmployeeStore(productSale.sku(), this.cashierRut)) {
                    throw new ValidationException(
                            "products[" + i + "]",
                            "Product with sku " + sku + " not sold at " + this.cashierRut + "'s store."
                    );
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public enum Type {
        RECEIPT("comprobante"),
        INVOICE("factura"),
        INVALID("");

        public final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
