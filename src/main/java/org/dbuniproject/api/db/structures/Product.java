package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.db.DatabaseConnection;
import org.json.JSONObject;

import java.sql.SQLException;

public record Product(
        @Nonnull String name,
        @Nonnull String description,
        int color,
        int priceWithoutTax,
        int typeId,
        int sizeId,
        int brandId,
        int minStock,
        int maxStock,
        int storeId
) implements Validatable {
    public Product(JSONObject json) throws ValidationException {
        this(
                json.optString("name"),
                json.optString("description"),
                json.optInt("color", -1),
                json.optInt("priceWithoutTax", -1),
                json.optInt("typeId", -1),
                json.optInt("sizeId", -1),
                json.optInt("brandId", -1),
                json.optInt("minStock", -1),
                json.optInt("maxStock", -1),
                json.optInt("storeId", -1)
        );

        this.validate();
    }

    @Override
    public void validate(@Nonnull String parentName) throws ValidationException {
        if (this.name.isEmpty()) {
            throw new ValidationException("name", "Name cannot be empty.");
        }

        if (this.description.isEmpty()) {
            throw new ValidationException("description", "Description cannot be empty.");
        }

        if (this.color == -1) {
            throw new ValidationException("color", "Color cannot be empty.");
        }

        if (this.color < 0 || this.color > 0xffffff) {
            throw new ValidationException("color", "Invalid color");
        }

        if (this.priceWithoutTax == -1) {
            throw new ValidationException("priceWithoutTax", "Price cannot be empty.");
        }

        if (this.priceWithoutTax <= 0) {
            throw new ValidationException("priceWithoutTax", "Price must be greater than zero.");
        }

        if (this.typeId == -1) {
            throw new ValidationException("typeId", "Type id cannot be empty.");
        }

        if (this.sizeId == -1) {
            throw new ValidationException("sizeId", "Size id cannot be empty.");
        }

        if (this.brandId == -1) {
            throw new ValidationException("brandId", "Brand id cannot be empty.");
        }

        if (this.minStock == -1) {
            throw new ValidationException("minStock", "Min stock cannot be empty.");
        }

        if (this.minStock <= 0) {
            throw new ValidationException("minStock", "Min stock must be greater than zero.");
        }

        if (this.maxStock == -1) {
            throw new ValidationException("maxStock", "Max stock cannot be empty.");
        }

        if (this.maxStock <= 0) {
            throw new ValidationException("maxStock", "Max stock must be greater than zero.");
        }

        if (this.maxStock <= this.minStock) {
            throw new ValidationException("maxStock", "Max stock must be greater than minimum stock.");
        }

        if (this.storeId == -1) {
            throw new RuntimeException("Missing store id.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (!db.doesProductTypeExist(this.typeId)) {
                throw new ValidationException("typeId", "Type " + this.typeId + " does not exist.");
            }

            if (!db.doesProductSizeExist(this.sizeId)) {
                throw new ValidationException("sizeId", "Size " + this.sizeId + " does not exist.");
            }

            if (!db.doesBrandExist(this.brandId)) {
                throw new ValidationException("brandId", "Brand " + this.brandId + " does not exist.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
