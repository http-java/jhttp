package dev.jhttp.core.header.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Objects;

public interface Upgrade {

    static @NotNull Upgrade create(@NotNull String name, @Nullable String version) {
        if (name.length() > 256) {
            throw new IllegalArgumentException("illegal name size");
        } else if (!name.matches("^[a-zA-Z0-9.]+$")) {
            throw new IllegalArgumentException("illegal name (contains illegal characters): \"" + name + "\"");
        } else if (version != null && !version.matches("^[a-zA-Z0-9.]+$")) {
            throw new IllegalArgumentException("illegal version name (contains illegal characters)");
        }

        return new Upgrade() {

            // Object

            @Override
            public @NotNull String getName() {
                return name;
            }
            @Override
            public @Nullable String getVersion() {
                return version;
            }

            // Implementations

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                @NotNull Upgrade that = (Upgrade) object;
                return Objects.equals(getName(), that.getName()) && Objects.equals(getVersion(), that.getVersion());
            }
            @Override
            public int hashCode() {
                return Objects.hash(getName(), getVersion());
            }

            @Override
            public @NotNull String toString() {
                return Parser.serialize(this);
            }

        };
    }

    // Getters

    @NotNull String getName();
    @Nullable String getVersion();

    // Classes

    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        public static @NotNull String serialize(@NotNull Upgrade upgrade) {
            if (upgrade.getName().length() > 256) {
                throw new IllegalArgumentException("cannot serialize this upgrade because it's name is more than 256 characters");
            } else if (!upgrade.getName().matches("^[a-zA-Z0-9.]+$")) {
                throw new IllegalArgumentException("cannot serialize this upgrade because the name has illegal characters '" + upgrade.getName() + "'");
            }

            @NotNull StringBuilder builder = new StringBuilder(upgrade.getName());
            if (upgrade.getVersion() != null) builder.append("/").append(upgrade.getVersion());

            return builder.toString();
        }
        public static @NotNull Upgrade deserialize(@NotNull String string) throws ParseException {
            if (validate(string)) {
                @NotNull String[] parts = string.split("/", 2);
                @NotNull String name = parts[0];
                @Nullable String version = parts.length > 1 ? parts[1] : null;

                return create(name, version);
            } else {
                throw new ParseException("cannot parse '" + string + "' as a valid upgrade", 0);
            }
        }

        public static boolean validate(@NotNull String string) {
            @NotNull String[] parts = string.split("/", 2);
            @NotNull String name = parts[0];
            @Nullable String version = parts.length > 1 ? parts[1] : null;

            return name.matches("^[a-zA-Z0-9.]+$") && name.length() <= 256 && (version == null || version.matches("^[a-zA-Z0-9.]+$"));
        }

    }

}
