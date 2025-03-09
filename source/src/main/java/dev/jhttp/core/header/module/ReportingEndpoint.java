package dev.jhttp.core.header.module;

import codes.laivy.address.domain.Domain;
import codes.laivy.address.host.HttpHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Objects;

public interface ReportingEndpoint {

    // Static initializers

    static @NotNull ReportingEndpoint create(@NotNull String name, @NotNull HttpHost<?> domain) {
        if (name.length() > 256) {
            throw new IllegalArgumentException("illegal name size");
        } else if (!name.matches("^[a-zA-Z0-9-]+$")) {
            throw new IllegalArgumentException("illegal name (contains illegal characters): \"" + name + "\"");
        }

        return new ReportingEndpoint() {

            // Object

            @Override
            public @NotNull String getName() {
                return name;
            }
            @Override
            public @NotNull HttpHost<?> getDomain() {
                return domain;
            }

            // Implementations

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                @NotNull ReportingEndpoint that = (ReportingEndpoint) object;
                return Objects.equals(getName(), that.getName()) && Objects.equals(getDomain(), that.getDomain());
            }
            @Override
            public int hashCode() {
                return Objects.hash(getName(), getDomain());
            }

            @Override
            public @NotNull String toString() {
                return Parser.serialize(this);
            }

        };
    }

    // Object

    @NotNull String getName();
    @NotNull HttpHost<?> getDomain();

    // Classes

    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        public static @NotNull String serialize(@NotNull ReportingEndpoint reporting) {
            if (reporting.getName().length() > 256) {
                throw new IllegalArgumentException("cannot serialize this reporting endpoint because it's name is more than 256 characters");
            } else if (!reporting.getName().matches("^[a-zA-Z0-9-]+$")) {
                throw new IllegalArgumentException("cannot serialize this reporting endpoint because the name has illegal characters '" + reporting.getName() + "'");
            }

            return reporting.getName() + "=\"" + reporting.getDomain() + "\"";
        }
        public static @NotNull ReportingEndpoint deserialize(@NotNull String string) throws ParseException {
            if (validate(string)) {
                @NotNull String[] parts = string.split("\\s*=\\s*", 2);

                @NotNull String name = parts[0];
                @NotNull HttpHost<?> domain = HttpHost.parse(parts[1]);

                return create(name, domain);
            } else {
                throw new ParseException("cannot parse '" + string + "' as a valid reporting endpoint", -1);
            }
        }

        public static boolean validate(@NotNull String string) {
            @NotNull String[] parts = string.split("\\s*=\\s*", 2);
            if (parts.length != 2) return false;

            @NotNull String name = parts[0];
            @NotNull String value = parts[1];

            if (!name.matches("^[a-zA-Z0-9-]+$")) {
                return false;
            } else if (name.length() > 256) {
                return false;
            } else if (!Domain.validate(value)) {
                return false;
            } else {
                return true;
            }
        }

    }

}
