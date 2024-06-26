package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.json.JSONEncodable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.SQLException;

public record ProductSale(long sku, int quantity) implements JSONEncodable, Validatable {
    @SuppressWarnings("unused")
    public ProductSale(JSONObject json) {
        this(json.optLong("sku", -1), json.optInt("quantity", -1));
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("sku", this.sku)
                .put("quantity", this.quantity);
    }

    @Override
    public void validate(@NotNull String parentName) throws ValidationException {
        final String keyPrefix = !parentName.isEmpty() ? parentName + "." : "";

        if (this.sku == -1) {
            throw new ValidationException(keyPrefix + "sku", "Product sku is empty.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (!db.doesProductExist(this.sku)) {
                throw new ValidationException(
                        keyPrefix + "sku",
                        "Product with sku " + this.sku + " does not exist"
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (this.quantity == -1) {
            throw new ValidationException(keyPrefix + "quantity", "Quantity is empty.");
        }

        if (this.quantity <= 0) {
            throw new ValidationException(keyPrefix + "quantity", "Quantity must be greater than zero");
        }
    }
}
