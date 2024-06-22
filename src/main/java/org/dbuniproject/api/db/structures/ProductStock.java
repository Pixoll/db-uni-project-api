package org.dbuniproject.api.db.structures;

public class ProductStock {
    public final long productSku;
    public final int storeId;
    public int min;
    public int max;
    public int forSale;
    public int inStorage;

    public ProductStock(long productSku, int storeId, int min, int max, int forSale, int inStorage) {
        this.productSku = productSku;
        this.storeId = storeId;
        this.min = min;
        this.max = max;
        this.forSale = forSale;
        this.inStorage = inStorage;
    }
}
