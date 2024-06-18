package org.dbuniproject.api.db;

import org.dbuniproject.api.Api;
import org.dbuniproject.api.db.structures.Region;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }
}
