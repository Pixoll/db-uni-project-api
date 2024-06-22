package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.dbuniproject.api.Api;
import org.dbuniproject.api.SessionTokenManager;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.structures.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                if (region.number() == regionNumber) {
                    region.addCommune(communeId, communeName);
                    addedCommune = true;
                    break;
                }
            }

            if (!addedCommune) {
                final Region region = new Region(
                        regionNumber,
                        result.getString("regionName"),
                        new ArrayList<>()
                );
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

    public ArrayList<String> getProductColors() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery(
                "SELECT DISTINCT color FROM project.producto ORDER BY color"
        );

        final ArrayList<String> colors = new ArrayList<>();

        while (result.next()) {
            colors.add(Util.intColorToHexString(result.getInt("color")));
        }

        return colors;
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
            sql += " AND project.aplicar_iva(P.precio_sin_iva) <= ?";
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
                    .put("color", Util.intColorToHexString(result.getInt("color")))
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
                    INNER JOIN (
                        SELECT rut, id_sucursal FROM project.gerente
                        UNION SELECT rut, id_sucursal FROM project.vendedor
                    ) AS E ON SU.id = E.id_sucursal
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
                    .put("color", Util.intColorToHexString(result.getInt("color")))
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
                .put("color", Util.intColorToHexString(result.getInt("color")))
                .put("price", result.getInt("price"))
                : null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean doesProductExist(long sku) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT 1 FROM project.producto WHERE sku = ?"
        );
        query.setLong(1, sku);

        final ResultSet result = query.executeQuery();

        return result.next();
    }

    public boolean isProductSoldAtEmployeeStore(long sku, @Nonnull String rut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT 1
                    FROM project.producto AS P
                    INNER JOIN project.stock AS ST ON P.sku = ST.sku_producto
                    INNER JOIN project.sucursal AS SU ON SU.id = ST.id_sucursal
                    INNER JOIN (
                        SELECT rut, id_sucursal FROM project.gerente
                        UNION SELECT rut, id_sucursal FROM project.vendedor
                    ) AS E ON SU.id = E.id_sucursal
                    WHERE P.eliminado = FALSE AND P.sku = ? AND E.rut = ?"""
        );
        query.setLong(1, sku);
        query.setString(2, rut);

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

    public int getProductStock(long sku, @Nonnull String cashierRut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT ST.actual + ST.bodega AS stock
                    FROM project.vendedor AS V
                    INNER JOIN project.stock AS ST on ST.id_sucursal = V.id_sucursal
                    WHERE ST.sku_producto = ? AND V.rut = ?"""
        );
        query.setLong(1, sku);
        query.setString(2, cashierRut);

        final ResultSet result = query.executeQuery();

        return result.next() ? result.getInt("stock") : -1;
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

    public boolean doesEmployeeExist(@Nonnull String rut, @Nonnull String email, int phone) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT 1 FROM (
                    SELECT rut, email, telefono FROM project.gerente
                    UNION SELECT rut, email, telefono FROM project.vendedor
                ) AS E WHERE E.rut = ? OR E.email = ? OR E.telefono = ?"""
        );
        query.setString(1, rut);
        query.setString(2, email);
        query.setInt(3, phone);

        final ResultSet result = query.executeQuery();

        return result.next();
    }

    @Nullable
    public Client getClient(@Nonnull String rut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT * FROM project.cliente WHERE rut = ?"
        );
        query.setString(1, rut);

        final ResultSet result = query.executeQuery();

        return result.next() ? new Client(
                result.getString("rut"),
                result.getString("nombre_primero"),
                result.getString("nombre_segundo"),
                result.getString("nombre_ap_paterno"),
                result.getString("nombre_ap_materno"),
                result.getString("email"),
                result.getInt("telefono")
        ) : null;
    }

    public boolean doesClientExist(@Nonnull Client client) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT 1 FROM project.cliente WHERE rut = ? OR email = ? OR telefono = ?"
        );
        query.setString(1, client.rut());
        query.setString(2, client.email());
        query.setInt(3, client.phone());

        final ResultSet result = query.executeQuery();

        return result.next();
    }

    public boolean doesClientExist(@Nonnull String rut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT 1 FROM project.cliente WHERE rut = ?"
        );
        query.setString(1, rut);

        final ResultSet result = query.executeQuery();

        return result.next();
    }

    public void insertClient(@Nonnull Client client) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "INSERT INTO project.cliente VALUES (?, ?, ?, ?, ?, ?, ?)"
        );
        query.setString(1, client.rut());
        query.setString(2, client.firstName());
        query.setString(3, client.secondName());
        query.setString(4, client.firstLastName());
        query.setString(5, client.secondLastName());
        query.setString(6, client.email());
        query.setInt(7, client.phone());

        query.executeUpdate();
    }

    public boolean doesCashierExist(@Nonnull String rut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT 1 FROM project.vendedor WHERE rut = ?"
        );
        query.setString(1, rut);

        final ResultSet result = query.executeQuery();

        return result.next();
    }

    public ArrayList<Sale> getSalesInStore(@Nonnull String managerRut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                -- noinspection SqlAggregates
                SELECT
                    S.id,
                    S.fecha AS date,
                    S.rut_vendedor AS cashierrut,
                    S.rut_cliente AS clientrut,
                    C.tipo AS type,
                    C.total,
                    (SELECT json_agg(json_build_object(
                        'sku', VP.sku_producto, 'quantity', VP.cantidad
                    )) FROM project.ventadeproducto AS VP WHERE VP.id_venta = S.id) AS products
                    FROM project.gerente AS G
                    INNER JOIN project.vendedor AS V ON V.id_sucursal = G.id_sucursal
                    INNER JOIN project.venta AS S ON S.rut_vendedor = V.rut
                    INNER JOIN project.comprobante AS C ON C.id = S.id
                    WHERE G.rut = ?
                    GROUP BY S.id, C.id
                    ORDER BY S.fecha;"""
        );
        query.setString(1, managerRut);

        final ResultSet result = query.executeQuery();

        final ArrayList<Sale> sales = new ArrayList<>();

        while (result.next()) {
            final JSONArray jsonProducts = new JSONArray(result.getString("products"));
            final ArrayList<ProductSale> products = new ArrayList<>();

            for (final Object json : jsonProducts) {
                products.add(new ProductSale((JSONObject) json));
            }

            sales.add(new Sale(
                    result.getLong("id"),
                    result.getTimestamp("date"),
                    result.getString("cashierRut"),
                    result.getString("clientRut"),
                    Objects.requireNonNull(Util.stringToEnum(result.getString("type"), Sale.Type.class)),
                    result.getInt("total"),
                    products
            ));
        }

        return sales;
    }

    public ArrayList<Sale> getSalesOfCashier(@Nonnull String cashierRut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                -- noinspection SqlAggregates
                SELECT
                    S.id,
                    S.fecha AS date,
                    S.rut_vendedor AS cashierrut,
                    S.rut_cliente AS clientrut,
                    C.tipo AS type,
                    C.total,
                    (SELECT json_agg(json_build_object(
                        'sku', VP.sku_producto, 'quantity', VP.cantidad
                    )) FROM project.ventadeproducto AS VP WHERE VP.id_venta = S.id) AS products
                    FROM project.vendedor AS V
                    INNER JOIN project.venta AS S ON S.rut_vendedor = V.rut
                    INNER JOIN project.comprobante AS C ON C.id = S.id
                    WHERE V.rut = ?
                    GROUP BY S.id, C.id
                    ORDER BY S.fecha;"""
        );
        query.setString(1, cashierRut);

        final ResultSet result = query.executeQuery();

        final ArrayList<Sale> sales = new ArrayList<>();

        while (result.next()) {
            final JSONArray jsonProducts = new JSONArray(result.getString("products"));
            final ArrayList<ProductSale> products = new ArrayList<>();

            for (final Object json : jsonProducts) {
                products.add(new ProductSale((JSONObject) json));
            }

            sales.add(new Sale(
                    result.getLong("id"),
                    result.getTimestamp("date"),
                    result.getString("cashierRut"),
                    result.getString("clientRut"),
                    Objects.requireNonNull(Util.stringToEnum(result.getString("type"), Sale.Type.class)),
                    result.getInt("total"),
                    products
            ));
        }

        return sales;
    }

    @Nullable
    public Sale getSale(long id) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT
                    V.fecha AS date,
                    V.rut_vendedor AS cashierrut,
                    V.rut_cliente AS clientrut,
                    C.tipo AS type,
                    C.total,
                    (SELECT json_agg(json_build_object(
                        'sku', VP.sku_producto, 'quantity', VP.cantidad
                    )) FROM project.ventadeproducto AS VP WHERE VP.id_venta = V.id) AS products
                    FROM project.venta AS V
                    INNER JOIN project.comprobante AS C ON C.id = V.id
                    WHERE V.id = ?
                    GROUP BY V.id, C.id"""
        );
        query.setLong(1, id);

        final ResultSet result = query.executeQuery();

        if (!result.next()) return null;

        final JSONArray jsonProducts = new JSONArray(result.getString("products"));
        final ArrayList<ProductSale> products = new ArrayList<>();

        for (final Object json : jsonProducts) {
            products.add(new ProductSale((JSONObject) json));
        }

        return new Sale(
                id,
                result.getTimestamp("date"),
                result.getString("cashierRut"),
                result.getString("clientRut"),
                Objects.requireNonNull(Util.stringToEnum(result.getString("type"), Sale.Type.class)),
                result.getInt("total"),
                products
        );
    }

    public long insertSale(@Nonnull Sale sale) throws SQLException {
        final int productsAmount = sale.products().size();
        final String products = "(project.id_ultima_venta(), ?, ?),\n".repeat(productsAmount)
                .replaceFirst(",\n$", "");

        final PreparedStatement query = this.connection.prepareStatement(
                """
                        INSERT INTO project.Venta (fecha, rut_vendedor, rut_cliente) VALUES
                            (CURRENT_TIMESTAMP, ?, ?);
                        INSERT INTO project.VentaDeProducto VALUES
                        """ + products + ";\n" + """
                        INSERT INTO project.Comprobante VALUES (
                            project.id_ultima_venta(),
                            ?::project.tipo_comprobante,
                            project.total_venta(project.id_ultima_venta())
                        );
                        SELECT project.id_ultima_venta();"""
        );
        query.setString(1, sale.cashierRut());
        query.setString(2, sale.clientRut());

        for (int i = 0; i < productsAmount; i++) {
            final ProductSale productSale = sale.products().get(i);
            query.setLong(i * 2 + 3, productSale.sku());
            query.setInt(i * 2 + 4, productSale.quantity());
        }

        query.setString(productsAmount * 2 + 3, sale.type().toString());

        boolean hasResultSet = query.execute();

        while (!hasResultSet && query.getUpdateCount() != -1) {
            hasResultSet = query.getMoreResults();
        }

        final ResultSet result = query.getResultSet();
        result.next();

        return result.getLong(1);
    }

    @Nullable
    public Integer getManagerStoreId(@Nonnull String managerRut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "SELECT id_sucursal FROM project.gerente WHERE rut = ?"
        );
        query.setString(1, managerRut);

        final ResultSet result = query.executeQuery();

        return result.next() ? result.getInt(1) : null;
    }

    public ArrayList<Cashier> getCashiers(@Nonnull String managerRut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT
                    V.rut,
                    V.nombre_primero AS firstName,
                    V.nombre_segundo AS secondName,
                    V.nombre_ap_paterno AS firstLastName,
                    V.nombre_ap_materno AS secondLastName,
                    V.email,
                    V.telefono AS phone,
                    V.full_time AS fullTime
                    FROM project.gerente AS G
                    INNER JOIN project.vendedor AS V on V.id_sucursal = G.id_sucursal
                    WHERE G.rut = ?"""
        );
        query.setString(1, managerRut);

        final ResultSet result = query.executeQuery();

        final ArrayList<Cashier> cashiers = new ArrayList<>();

        while (result.next()) {
            cashiers.add(new Cashier(
                    result.getString("rut"),
                    result.getString("firstName"),
                    result.getString("secondName"),
                    result.getString("firstLastName"),
                    result.getString("secondLastName"),
                    result.getString("email"),
                    result.getInt("phone"),
                    result.getBoolean("fullTime"),
                    "",
                    "",
                    -1
            ));
        }

        return cashiers;
    }

    @Nullable
    public Cashier getCashier(@Nonnull String cashierRut) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement("""
                SELECT
                    rut,
                    nombre_primero AS firstName,
                    nombre_segundo AS secondName,
                    nombre_ap_paterno AS firstLastName,
                    nombre_ap_materno AS secondLastName,
                    email,
                    telefono AS phone,
                    full_time AS fullTime
                    FROM project.vendedor
                    WHERE rut = ?"""
        );
        query.setString(1, cashierRut);

        final ResultSet result = query.executeQuery();

        return result.next() ? new Cashier(
                result.getString("rut"),
                result.getString("firstName"),
                result.getString("secondName"),
                result.getString("firstLastName"),
                result.getString("secondLastName"),
                result.getString("email"),
                result.getInt("phone"),
                result.getBoolean("fullTime"),
                "",
                "",
                -1
        ) : null;
    }

    public void insertCashier(@Nonnull Cashier cashier) throws SQLException {
        final PreparedStatement query = this.connection.prepareStatement(
                "INSERT INTO project.vendedor VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        query.setString(1, cashier.rut());
        query.setString(2, cashier.firstName());
        query.setString(3, cashier.secondName());
        query.setString(4, cashier.firstLastName());
        query.setString(5, cashier.secondLastName());
        query.setString(6, cashier.email());
        query.setInt(7, cashier.phone());
        query.setBoolean(8, cashier.fullTime());
        query.setString(9, cashier.password());
        query.setString(10, cashier.salt());
        query.setInt(11, cashier.storeId());

        query.executeUpdate();
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }
}
