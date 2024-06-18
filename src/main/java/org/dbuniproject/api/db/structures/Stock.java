package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.json.JSONObject;

public class Stock extends Structure {
    public final int storeId;
    public final long productSku;
    public int min;
    public int max;
    public int forSale;
    public int inStorage;

    public Stock(int storeId, long productSku, int min, int max, int forSale, int inStorage) {
        this.storeId = storeId;
        this.productSku = productSku;
        this.min = min;
        this.max = max;
        this.forSale = forSale;
        this.inStorage = inStorage;
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("storeId", this.storeId)
                .put("productSku", this.productSku)
                .put("min", this.min)
                .put("max", this.max)
                .put("forSale", this.forSale)
                .put("inStorage", this.inStorage);
    }

    @Nonnull
    @Override
    public Stock clone() {
        return new Stock(this.storeId, this.productSku, this.min, this.max, this.forSale, this.inStorage);
    }
}
