package org.dbuniproject.api.db.structures;

import jakarta.annotation.Nonnull;
import org.dbuniproject.api.Util;
import org.dbuniproject.api.json.JSONEncodable;
import org.json.JSONObject;

public record Client(
        @Nonnull String rut,
        @Nonnull String firstName,
        @Nonnull String secondName,
        @Nonnull String firstLastName,
        @Nonnull String secondLastName,
        @Nonnull String email,
        int phone
) implements JSONEncodable, Validatable {
    public Client(JSONObject json) throws ValidationException {
        this(
                json.optString("rut"),
                json.optString("firstName"),
                json.optString("secondName"),
                json.optString("firstLastName"),
                json.optString("secondLastName"),
                json.optString("email"),
                json.optInt("phone", -1)
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
                .put("phone", this.phone);
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

        if (this.email.isEmpty()) {
            throw new ValidationException("email", "Email cannot be empty.");
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
    }
}
