package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.dbuniproject.api.Api;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.db.structures.Region;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnection implements AutoCloseable {
    private static final String POSTGRES_DB_URL = Api.DOTENV.get("POSTGRES_DB_URL");
    private final Connection connection;

    public DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(POSTGRES_DB_URL);
    }

    public ArrayList<Region> getRegionsWithCommunes() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("""
                SELECT R.numero AS regionNumber, R.nombre AS regionName, C.id AS communeId, C.nombre AS communeName
                    FROM project.comuna AS C
                    INNER JOIN project.region AS R ON R.numero = C.region"""
        );

        final ArrayList<Region> regions = new ArrayList<>();

        while (result.next()) {
            final short regionNumber = result.getShort("regionNumber");
            final short communeId = result.getShort("communeId");
            final String communeName = result.getString("communeName");

            boolean addedCommune = false;
            for (final Region region : regions) {
                if (region.number == regionNumber) {
                    region.addCommune(communeId, communeName);
                    addedCommune = true;
                    break;
                }
            }

            if (!addedCommune) {
                final Region region = new Region(regionNumber, result.getString("regionName"));
                region.addCommune(communeId, communeName);
                regions.add(region);
            }
        }

        return regions;
    }

    public ArrayList<JSONObject> getProductSizes() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("SELECT * FROM project.talla");

        final ArrayList<JSONObject> productSizes = new ArrayList<>();

        while (result.next()) {
            productSizes.add(new JSONObject()
                    .put("id", result.getInt("id"))
                    .put("name", result.getString("nombre"))
            );
        }

        return productSizes;
    }

    @Nullable
    public JSONObject getProductSize(int id) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("SELECT * FROM project.talla WHERE id = ?");
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("nombre"))
                : null;
    }

    @Nullable
    public JSONObject getProductSize(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM project.talla WHERE nombre ILIKE '%' || ? || '%'"
        );
        query.setString(1, name.toLowerCase());

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("nombre"))
                : null;
    }

    public void insertProductSize(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("INSERT INTO project.talla (nombre) VALUES (?)");
        query.setString(1, name);

        query.executeUpdate();
    }

    public ArrayList<JSONObject> getProductTypes() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("SELECT * FROM project.tipo");

        final ArrayList<JSONObject> productTypes = new ArrayList<>();

        while (result.next()) {
            productTypes.add(new JSONObject()
                    .put("id", result.getInt("id"))
                    .put("name", result.getString("nombre"))
                    .put("description", result.getString("descripcion"))
            );
        }

        return productTypes;
    }

    @Nullable
    public JSONObject getProductType(int id) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("SELECT * FROM project.tipo WHERE id = ?");
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("nombre"))
                .put("description", result.getString("descripcion"))
                : null;
    }

    @Nullable
    public JSONObject getProductType(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM project.tipo WHERE ? LIKE nombre ILIKE '%' || ? || '%'"
        );
        query.setString(1, name.toLowerCase());

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("nombre"))
                .put("description", result.getString("descripcion"))
                : null;
    }

    public void insertProductType(@Nonnull String name, @Nonnull String description) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "INSERT INTO project.tipo (nombre, descripcion) VALUES (?, ?)"
        );
        query.setString(1, name);
        query.setString(2, description);

        query.executeUpdate();
    }

    public ArrayList<JSONObject> getBrands() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("SELECT * FROM project.marca");

        final ArrayList<JSONObject> brands = new ArrayList<>();

        while (result.next()) {
            brands.add(new JSONObject()
                    .put("id", result.getInt("id"))
                    .put("name", result.getString("nombre"))
            );
        }

        return brands;
    }

    @Nullable
    public JSONObject getBrand(int id) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("SELECT * FROM project.marca WHERE id = ?");
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("nombre"))
                : null;
    }

    @Nullable
    public JSONObject getBrand(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM project.marca WHERE ? LIKE nombre ILIKE '%' || ? || '%'"
        );
        query.setString(1, name);

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("nombre"))
                : null;
    }

    public void insertBrand(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("INSERT INTO project.marca (nombre) VALUES (?)");
        query.setString(1, name);

        query.executeUpdate();
    }

    public ArrayList<JSONObject> getProducts(
            @Nullable String name,
            @Nonnull List<Integer> types,
            @Nonnull List<Integer> sizes,
            @Nonnull List<Integer> brands,
            @Nonnull List<Integer> colors,
            @Nonnull List<Integer> regions,
            @Nonnull List<Integer> communes,
            @Nullable Integer minPrice,
            @Nullable Integer maxPrice,
            @Nullable Boolean sortByNameAsc,
            @Nullable Boolean sortByPriceAsc
    ) throws SQLException {
        final AtomicInteger argumentCounter = new AtomicInteger(1);
        int nameArgPosition = -1;
        int minPriceArgPosition = -1;
        int maxPriceArgPosition = -1;
        int typesArgPosition = -1;
        int sizesArgPosition = -1;
        int brandsArgPosition = -1;
        int colorsArgPosition = -1;
        int communesArgPosition = -1;
        int regionsArgPosition = -1;

        @SuppressWarnings("SqlShouldBeInGroupBy")
        String sql = """
                SELECT
                    P.sku,
                    P.nombre AS name,
                    M.nombre AS brand,
                    P.color,
                    project.aplicar_iva(P.precio_sin_iva) AS price,
                    SUM(ST.actual + ST.bodega) > 0 AS available
                    FROM project.Producto AS P
                    INNER JOIN project.Stock AS ST ON ST.sku_producto = P.sku
                    INNER JOIN project.Marca AS M ON M.id = P.id_marca""";

        if (!communes.isEmpty() || !regions.isEmpty()) {
            sql += " INNER JOIN project.Sucursal AS SU ON SU.id = ST.id_sucursal";
            sql += " INNER JOIN project.Comuna as C ON C.id = SU.id_comuna";
        }

        if (!regions.isEmpty()) {
            sql += " INNER JOIN project.Region as R ON R.numero = C.region";
        }

        sql += " WHERE P.eliminado = FALSE";

        if (name != null && !name.isEmpty()) {
            sql += " AND P.nombre ILIKE '%' || ? || '%'";
            nameArgPosition = argumentCounter.getAndIncrement();
        }
        if (minPrice != null) {
            sql += " AND project.aplicar_iva(P.precio_sin_iva) >= ?";
            minPriceArgPosition = argumentCounter.getAndIncrement();
        }
        if (maxPrice != null) {
            sql += " AND project.aplicar_iva(P.precio_sin_iva) >= ?";
            maxPriceArgPosition = argumentCounter.getAndIncrement();
        }
        if (!types.isEmpty()) {
            sql += " AND P.id_tipo = ANY (?)";
            typesArgPosition = argumentCounter.getAndIncrement();
        }
        if (!sizes.isEmpty()) {
            sql += " AND P.id_talla = ANY (?)";
            sizesArgPosition = argumentCounter.getAndIncrement();
        }
        if (!brands.isEmpty()) {
            sql += " AND P.id_marca = ANY (?)";
            brandsArgPosition = argumentCounter.getAndIncrement();
        }
        if (!colors.isEmpty()) {
            sql += " AND P.color = ANY (?)";
            colorsArgPosition = argumentCounter.getAndIncrement();
        }
        if (!communes.isEmpty()) {
            sql += " AND C.id = ANY (?)";
            communesArgPosition = argumentCounter.getAndIncrement();
        }
        if (!regions.isEmpty()) {
            sql += " AND R.numero = ANY (?)";
            regionsArgPosition = argumentCounter.getAndIncrement();
        }

        sql += " GROUP BY P.sku, P.nombre, M.nombre, P.color, P.precio_sin_iva";

        if (sortByNameAsc != null || sortByPriceAsc != null) {
            final ArrayList<String> sorts = new ArrayList<>();
            if (sortByPriceAsc != null) sorts.add("P.precio_sin_iva " + (sortByPriceAsc ? "ASC" : "DESC"));
            if (sortByNameAsc != null) sorts.add("P.nombre " + (sortByNameAsc ? "ASC" : "DESC"));

            sql += " ORDER BY " + String.join(", ", sorts);
        }

        final PreparedStatement query = connection.prepareStatement(sql);

        if (nameArgPosition != -1) query.setString(nameArgPosition, name.toLowerCase());
        if (minPriceArgPosition != -1) query.setInt(minPriceArgPosition, minPrice);
        if (maxPriceArgPosition != -1) query.setInt(maxPriceArgPosition, maxPrice);
        if (typesArgPosition != -1) {
            query.setArray(typesArgPosition, this.connection.createArrayOf("INT", types.toArray()));
        }
        if (sizesArgPosition != -1) {
            query.setArray(sizesArgPosition, this.connection.createArrayOf("INT", sizes.toArray()));
        }
        if (brandsArgPosition != -1) {
            query.setArray(brandsArgPosition, this.connection.createArrayOf("INT", brands.toArray()));
        }
        if (colorsArgPosition != -1) {
            query.setArray(colorsArgPosition, this.connection.createArrayOf("INT", colors.toArray()));
        }
        if (communesArgPosition != -1) {
            query.setArray(communesArgPosition, this.connection.createArrayOf("INT", communes.toArray()));
        }
        if (regionsArgPosition != -1) {
            query.setArray(regionsArgPosition, this.connection.createArrayOf("INT", regions.toArray()));
        }

        final ResultSet result = query.executeQuery();

        final ArrayList<JSONObject> products = new ArrayList<>();

        while (result.next()) {
            products.add(new JSONObject()
                    .put("sku", result.getLong("sku"))
                    .put("name", result.getString("name"))
                    .put("brand", result.getString("brand"))
                    .put("color", result.getInt("color"))
                    .put("price", result.getInt("price"))
                    .put("available", result.getBoolean("available"))
            );
        }

        return products;
    }

    public ArrayList<JSONObject> getProductsByEmployee(@Nonnull String rut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT
                    P.sku,
                    P.nombre AS name,
                    P.descripcion AS description,
                    P.color AS color,
                    project.aplicar_iva(P.precio_sin_iva) AS price,
                    TI.nombre AS type,
                    TA.nombre AS size,
                    M.nombre AS brand,
                    ST.actual AS stockForSale,
                    ST.bodega AS stockInStorage,
                    ST.min AS minStock,
                    ST.max AS maxStock
                    FROM project.producto AS P
                    INNER JOIN project.tipo AS TI ON TI.id = P.id_tipo
                    INNER JOIN project.talla AS TA ON TA.id = P.id_talla
                    INNER JOIN project.marca AS M ON M.id = P.id_marca
                    INNER JOIN project.stock AS ST ON P.sku = ST.sku_producto
                    INNER JOIN project.sucursal AS SU ON SU.id = ST.id_sucursal
                    INNER JOIN project.gerente AS E ON SU.id = E.id_sucursal
                    WHERE P.eliminado = FALSE AND E.rut = ?"""
        );
        query.setString(1, rut);

        final ResultSet result = query.executeQuery();

        final ArrayList<JSONObject> products = new ArrayList<>();

        while (result.next()) {
            products.add(new JSONObject()
                    .put("sku", result.getLong("sku"))
                    .put("name", result.getString("name"))
                    .put("description", result.getString("description"))
                    .put("color", result.getInt("color"))
                    .put("price", result.getInt("price"))
                    .put("type", result.getString("type"))
                    .put("size", result.getString("size"))
                    .put("brand", result.getString("brand"))
                    .put("stockForSale", result.getInt("stockForSale"))
                    .put("stockInStorage", result.getInt("stockInStorage"))
                    .put("minStock", result.getInt("minStock"))
                    .put("maxStock", result.getInt("maxStock"))
            );
        }

        return products;
    }

    @Nullable
    public JSONObject getProduct(long sku) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("""
                SELECT
                     P.nombre AS name,
                     P.descripcion AS description,
                     M.nombre AS brand,
                     TI.nombre AS type,
                     TA.nombre AS size,
                     P.color,
                     project.aplicar_iva(P.precio_sin_iva) AS price
                     FROM project.producto AS P
                     INNER JOIN project.tipo AS TI ON TI.id = P.id_tipo
                     INNER JOIN project.talla AS TA ON TA.id = P.id_talla
                     INNER JOIN project.marca AS M on M.id = P.id_marca
                     WHERE P.sku = ?"""
        );
        query.setLong(1, sku);

        final ResultSet result = query.executeQuery();

        return result.next() ? new JSONObject()
                .put("name", result.getString("name"))
                .put("description", result.getString("description"))
                .put("brand", result.getString("brand"))
                .put("type", result.getString("type"))
                .put("size", result.getString("size"))
                .put("color", result.getInt("color"))
                .put("price", result.getInt("price"))
                : null;
    }

    public boolean doesProductExist(long sku) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT 1 FROM project.producto WHERE sku = ?"
        );
        query.setLong(1, sku);

        final ResultSet result = query.executeQuery();

        return result.next();
    }

    public ArrayList<JSONObject> getProductStocks(long sku) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT
                    SU.id AS storeId,
                    SU.nombre AS storeName,
                    ST.actual + ST.bodega AS stock
                    FROM project.stock AS ST
                    INNER JOIN project.sucursal AS SU ON SU.id = ST.id_sucursal
                    WHERE ST.sku_producto = ?"""
        );
        query.setLong(1, sku);

        final ResultSet result = query.executeQuery();

        final ArrayList<JSONObject> stocks = new ArrayList<>();

        while (result.next()) {
            stocks.add(new JSONObject()
                    .put("storeId", result.getInt("storeId"))
                    .put("storeName", result.getString("storeName"))
                    .put("stock", result.getInt("stock"))
            );
        }

        return stocks;
    }

    public ArrayList<JSONObject> getStores() throws SQLException {
        final ResultSet result = this.connection.createStatement().executeQuery("""
                SELECT
                    S.id,
                    S.nombre AS name,
                    S.direccion_calle AS addressStreet,
                    S.direccion_numero AS addressNumber,
                    C.nombre AS commune
                    FROM project.sucursal AS S
                    INNER JOIN project.comuna AS C ON C.id = S.id_comuna"""
        );

        final ArrayList<JSONObject> stores = new ArrayList<>();

        while (result.next()) {
            stores.add(new JSONObject()
                    .put("id", result.getInt("id"))
                    .put("name", result.getString("name"))
                    .put("addressStreet", result.getString("addressStreet"))
                    .put("addressNumber", result.getShort("addressNumber"))
                    .put("commune", result.getString("commune"))
            );
        }

        return stores;
    }

    @Nullable
    public JSONObject getStore(int id) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT
                    S.id,
                    S.nombre AS name,
                    S.direccion_calle AS addressStreet,
                    S.direccion_numero AS addressNumber,
                    C.nombre AS commune
                    FROM project.sucursal AS S
                    INNER JOIN project.comuna AS C ON C.id = S.id_comuna
                    WHERE S.id = ?"""
        );
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new JSONObject()
                .put("id", result.getInt("id"))
                .put("name", result.getString("name"))
                .put("addressStreet", result.getString("addressStreet"))
                .put("addressNumber", result.getShort("addressNumber"))
                .put("commune", result.getString("commune"))
                : null;
    }

    @Nullable
    public EmployeeCredentials getEmployeeCredentials(
            @Nonnull String rut,
            @Nonnull SessionTokenManager.Token.Type type
    ) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(type == SessionTokenManager.Token.Type.CASHIER
                ? "SELECT contraseña AS password, salt FROM project.vendedor WHERE rut = ?"
                : "SELECT contraseña AS password, salt FROM project.gerente WHERE rut = ?"
        );
        query.setString(1, rut);

        final ResultSet result = query.executeQuery();

        return result.next() ? new EmployeeCredentials(
                result.getString("password"),
                result.getString("salt")
        ) : null;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }
}
