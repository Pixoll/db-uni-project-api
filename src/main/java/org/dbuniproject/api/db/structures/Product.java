package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Product extends Structure {
    public final long sku;
    @Nonnull
    public String name;
    @Nonnull
    public String description;
    public int color;
    public int priceWithoutTax;
    public boolean deleted;
    public int typeId;
    public int sizeId;
    public int brandId;

    public Product(
            long sku,
            @Nonnull String name,
            @Nonnull String description,
            int color,
            int priceWithoutTax,
            boolean deleted,
            int typeId,
            int sizeId,
            int brandId
    ) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.color = color;
        this.priceWithoutTax = priceWithoutTax;
        this.deleted = deleted;
        this.typeId = typeId;
        this.sizeId = sizeId;
        this.brandId = brandId;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("sku", this.sku)
                .put("name", this.name)
                .put("description", this.description)
                .put("color", this.color)
                .put("priceWithoutTax", this.priceWithoutTax)
                .put("deleted", this.deleted)
                .put("typeId", this.typeId)
                .put("sizeId", this.sizeId)
                .put("brandId", this.brandId);
    }

    @Nonnull
    @Override
    public Product clone() {
        return new Product(
                this.sku,
                this.name,
                this.description,
                this.color,
                this.priceWithoutTax,
                this.deleted,
                this.typeId,
                this.sizeId,
                this.brandId
        );
    }
}
