package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class ProductSale extends Structure {
    public final long saleId;
    public final long productSku;
    public final int quantity;

    public ProductSale(final long saleId, final long productSku, final int quantity) {
        this.saleId = saleId;
        this.productSku = productSku;
        this.quantity = quantity;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("saleId", this.saleId)
                .put("productSku", this.productSku)
                .put("quantity", this.quantity);
    }

    @Nonnull
    @Override
    public ProductSale clone() {
        return new ProductSale(this.saleId, this.productSku, this.quantity);
    }
}
