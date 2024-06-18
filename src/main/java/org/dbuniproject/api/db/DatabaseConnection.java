package org.dbuniproject.api.db;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.dbuniproject.api.Api;
import org.dbuniproject.api.db.structures.Brand;
import org.dbuniproject.api.db.structures.ProductSize;
import org.dbuniproject.api.db.structures.ProductType;
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
                SELECT R.numero AS region_number, R.nombre AS region_name, C.id AS commune_id, C.nombre AS commune_name
                    FROM project.comuna AS C
                    INNER JOIN project.region AS R ON R.numero = C.region"""
        );

        final ArrayList<Region> regions = new ArrayList<>();

        while (result.next()) {
            final short regionNumber = result.getShort("region_number");
            final short communeId = result.getShort("commune_id");
            final String communeName = result.getString("commune_name");

            boolean addedCommune = false;
            for (final Region region : regions) {
                if (region.number == regionNumber) {
                    region.addCommune(communeId, communeName);
                    addedCommune = true;
                    break;
                }
            }

            if (!addedCommune) {
                final Region region = new Region(regionNumber, result.getString("region_name"));
                region.addCommune(communeId, communeName);
                regions.add(region);
            }
        }

        return regions;
    }

    public ArrayList<ProductSize> getProductSizes() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("SELECT * FROM project.talla");

        final ArrayList<ProductSize> productSizes = new ArrayList<>();

        while (result.next()) {
            final int id = result.getInt("id");
            final String name = result.getString("nombre");

            productSizes.add(new ProductSize(id, name));
        }

        return productSizes;
    }

    @Nullable
    public ProductSize getProductSize(int id) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("SELECT * FROM project.talla WHERE id = ?");
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next()
                ? new ProductSize(result.getInt("id"), result.getString("nombre"))
                : null;
    }

    @Nullable
    public ProductSize getProductSize(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM project.talla WHERE nombre ILIKE '%' || ? || '%'"
        );
        query.setString(1, name.toLowerCase());

        final ResultSet result = query.executeQuery();

        return result.next() ? new ProductSize(
                result.getInt("id"),
                result.getString("nombre")
        ) : null;
    }

    public void insertProductSize(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("INSERT INTO project.talla (nombre) VALUES (?)");
        query.setString(1, name);

        query.executeUpdate();
    }

    public ArrayList<ProductType> getProductTypes() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("SELECT * FROM project.tipo");

        final ArrayList<ProductType> productTypes = new ArrayList<>();

        while (result.next()) {
            final int id = result.getInt("id");
            final String name = result.getString("nombre");
            final String description = result.getString("descripcion");

            productTypes.add(new ProductType(id, name, description));
        }

        return productTypes;
    }

    @Nullable
    public ProductType getProductType(int id) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("SELECT * FROM project.tipo WHERE id = ?");
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next() ? new ProductType(
                result.getInt("id"),
                result.getString("nombre"),
                result.getString("descripcion")
        ) : null;
    }

    @Nullable
    public ProductType getProductType(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM project.tipo WHERE ? LIKE nombre ILIKE '%' || ? || '%'"
        );
        query.setString(1, name.toLowerCase());

        final ResultSet result = query.executeQuery();

        return result.next() ? new ProductType(
                result.getInt("id"),
                result.getString("nombre"),
                result.getString("descripcion")
        ) : null;
    }

    public void insertProductType(@Nonnull String name, @Nonnull String description) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "INSERT INTO project.tipo (nombre, descripcion) VALUES (?, ?)"
        );
        query.setString(1, name);
        query.setString(2, description);

        query.executeUpdate();
    }

    public ArrayList<Brand> getBrands() throws SQLException {
        final ResultSet result = connection.createStatement().executeQuery("SELECT * FROM project.marca");

        final ArrayList<Brand> brands = new ArrayList<>();

        while (result.next()) {
            final int id = result.getInt("id");
            final String name = result.getString("nombre");

            brands.add(new Brand(id, name));
        }

        return brands;
    }

    @Nullable
    public Brand getBrand(int id) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("SELECT * FROM project.marca WHERE id = ?");
        query.setInt(1, id);

        final ResultSet result = query.executeQuery();

        return result.next() ? new Brand(
                result.getInt("id"),
                result.getString("nombre")
        ) : null;
    }

    @Nullable
    public Brand getBrand(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM project.marca WHERE ? LIKE nombre ILIKE '%' || ? || '%'"
        );
        query.setString(1, name);

        final ResultSet result = query.executeQuery();

        return result.next() ? new Brand(
                result.getInt("id"),
                result.getString("nombre")
        ) : null;
    }

    public void insertBrand(@Nonnull String name) throws SQLException {
        final PreparedStatement query = connection.prepareStatement("INSERT INTO project.marca (nombre) VALUES (?)");
        query.setString(1, name);

        query.executeUpdate();
    }

    public ArrayList<JSONObject> getProducts(
            @Nullable String name,
            List<Integer> types,
            List<Integer> sizes,
            List<Integer> brands,
            List<Integer> colors,
            List<Integer> regions,
            List<Integer> communes,
            @Nullable Integer minPrice,
            @Nullable Integer maxPrice,
            boolean sortByName,
            boolean sortByPrice
    ) throws SQLException {
        final boolean typesFilter = types != null && !types.isEmpty();
        final boolean sizesFilter = sizes != null && !sizes.isEmpty();
        final boolean brandsFilter = brands != null && !brands.isEmpty();
        final boolean colorsFilter = colors != null && !colors.isEmpty();
        final boolean regionsFilter = regions != null && !regions.isEmpty();
        final boolean communesFilter = communes != null && !communes.isEmpty();

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

        if (communesFilter || regionsFilter) {
            sql += " INNER JOIN project.Sucursal AS SU ON SU.id = ST.id_sucursal";
            sql += " INNER JOIN project.Comuna as C ON C.id = SU.id_comuna";
        }

        if (regionsFilter) {
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
        if (typesFilter) {
            sql += " AND P.id_tipo = ANY (?)";
            typesArgPosition = argumentCounter.getAndIncrement();
        }
        if (sizesFilter) {
            sql += " AND P.id_talla = ANY (?)";
            sizesArgPosition = argumentCounter.getAndIncrement();
        }
        if (brandsFilter) {
            sql += " AND P.id_marca = ANY (?)";
            brandsArgPosition = argumentCounter.getAndIncrement();
        }
        if (colorsFilter) {
            sql += " AND P.color = ANY (?)";
            colorsArgPosition = argumentCounter.getAndIncrement();
        }
        if (communesFilter) {
            sql += " AND C.id = ANY (?)";
            communesArgPosition = argumentCounter.getAndIncrement();
        }
        if (regionsFilter) {
            sql += " AND R.numero = ANY (?)";
            regionsArgPosition = argumentCounter.getAndIncrement();
        }

        sql += " GROUP BY P.sku, P.nombre, M.nombre, P.color, P.precio_sin_iva";

        if (sortByName || sortByPrice) {
            final ArrayList<String> sorts = new ArrayList<>();
            if (sortByName) sorts.add("P.nombre");
            if (sortByPrice) sorts.add("P.precio_sin_iva");

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
                    .put("color", result.getString("color"))
                    .put("price", result.getInt("price"))
                    .put("available", result.getBoolean("available"))
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

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }
}
