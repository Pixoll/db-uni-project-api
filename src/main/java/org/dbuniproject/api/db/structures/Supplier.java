package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.db.DatabaseConnection;
import org.dbuniproject.api.json.JSONEncodable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

public record Supplier(
        @Nonnull String rut,
        @Nonnull String firstName,
        @Nonnull String secondName,
        @Nonnull String firstLastName,
        @Nonnull String secondLastName,
        @Nonnull String email,
        int phone,
        @Nonnull String addressStreet,
        short addressNumber,
        short communeId,
        @Nonnull String communeName,
        @Nonnull ArrayList<Integer> brandIds,
        @Nonnull ArrayList<String> brandNames
) implements JSONEncodable, Validatable {
    public Supplier(JSONObject json) throws ValidationException {
        this(
                json.optString("rut"),
                json.optString("firstName"),
                json.optString("secondName"),
                json.optString("firstLastName"),
                json.optString("secondLastName"),
                json.optString("email"),
                json.optInt("phone", -1),
                json.optString("addressStreet"),
                (short) json.optInt("addressNumber", -1),
                (short) json.optInt("communeId", -1),
                "",
                Util.jsonArrayToList(json.optJSONArray("brandIds", new JSONArray()), Integer.class),
                new ArrayList<>()
        );

        this.validate();
    }

    @Nonnull
    @Override
    public JSONObject toJSON() {
        return new JSONObject()
                .put("rut", this.rut)
                .put("firstName", this.firstName)
                .put("secondName", this.secondName)
                .put("firstLastName", this.firstLastName)
                .put("secondLastName", this.secondLastName)
                .put("email", this.email)
                .put("phone", this.phone)
                .put("addressStreet", this.addressStreet)
                .put("addressNumber", this.addressNumber)
                .put("commune", this.communeName)
                .put("brands", this.brandNames);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void validate(@Nonnull String parentName) throws ValidationException {
        if (this.rut.isEmpty()) {
            throw new ValidationException("rut", "Rut cannot be empty.");
        }

        if (!Util.isValidRut(this.rut)) {
            throw new ValidationException("rut", "Invalid rut.");
        }

        if (this.firstName.isEmpty()) {
            throw new ValidationException("firstName", "First name cannot be empty.");
        }

        if (this.secondName.isEmpty()) {
            throw new ValidationException("secondName", "Second name cannot be empty.");
        }

        if (this.firstLastName.isEmpty()) {
            throw new ValidationException("firstLastName", "First last name cannot be empty.");
        }

        if (this.secondLastName.isEmpty()) {
            throw new ValidationException("secondLastName", "Second last name cannot be empty.");
        }

        if (!this.email.matches(Util.EMAIL_REGEX)) {
            throw new ValidationException("email", "Invalid email address.");
        }

        if (this.phone == -1) {
            throw new ValidationException("phone", "Phone number cannot be empty.");
        }

        if (String.valueOf(this.phone).length() != 9) {
            throw new ValidationException("phone", "Invalid phone number.");
        }

        if (this.addressStreet.isEmpty()) {
            throw new ValidationException("addressStreet", "Address street cannot be empty.");
        }

        if (this.addressNumber <= 0) {
            throw new ValidationException("addressNumber", "Address number must be greater than 0.");
        }

        if (this.communeId == -1) {
            throw new ValidationException("communeId", "Commune id cannot be empty.");
        }

        if (this.brandIds.isEmpty()) {
            throw new ValidationException("brandIds", "Brand ids cannot be empty.");
        }

        try (final DatabaseConnection db = new DatabaseConnection()) {
            if (!db.doesCommuneExist(this.communeId)) {
                throw new ValidationException("communeId", "Commune with id " + this.communeId + " does not exist.");
            }

            for (int i = 0; i < this.brandIds.size(); i++) {
                final Integer brandId = this.brandIds.get(i);
                if (db.getBrand(brandId) == null) {
                    throw new ValidationException("brandIds[" + i + "]", "Brand " + brandId + " does not exist.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
