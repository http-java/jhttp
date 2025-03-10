package dev.jhttp.core.body.content;

import dev.jhttp.core.header.category.Deferred;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public interface ContentType<T> {

    // Getters

    /**
     * Returns the type of this media type.
     *
     * @return the type of this media type
     */
    @NotNull Type getType();

    /**
     * Returns the parser associated with this media type.
     *
     * @return the parser associated with this media type
     */
    @NotNull ContentParser<T> getParser();

    /**
     * The parameters of a media type.
     * <p>
     * As an example, the media type "text/plain" can have the parameter "charset" with the value "utf-8"
     *
     * @return the parameter array of this media type
     */
    @NotNull Parameter @NotNull [] getParameters();

    /**
     * Returns the parameter with the specified key, if present.
     *
     * @param key the key of the parameter to retrieve
     * @return an optional containing the parameter if present, or an empty optional if not
     */
    default @NotNull Optional<Parameter> getParameter(@NotNull String key) {
        return Arrays.stream(getParameters()).filter(p -> p.getKey().equalsIgnoreCase(key)).findFirst();
    }

    /**
     * Returns the charset parameter of this media type, if present.
     *
     * @return the charset parameter if present, or null if not
     */
    default @Nullable Deferred<Charset> getCharset() {
        @Nullable Parameter parameter = getParameter("charset").orElse(null);
        return parameter != null ? Deferred.charset(parameter.getValue()) : null;
    }

    /**
     * Returns the boundary parameter of this media type, if present.
     *
     * @return the boundary parameter if present, or null if not
     */
    default @Nullable Boundary getBoundary() {
        @Nullable Parameter parameter = getParameter("boundary").orElse(null);

        if (parameter != null) {
            return new Boundary(parameter.getValue());
        } else {
            return null;
        }
    }

    // Classes

    /**
     * Represents the type of media type, consisting of a type and an optional subtype.
     *
     * @author Daniel Meinicke
     * @since 0.1
     */
    final class Type implements CharSequence {

        // Static initializers

        /**
         * Parses a string into a media type {@link Type}.
         *
         * @param string the string to parse
         * @return the parsed media type {@link Type}
         */
        public static @NotNull Type parse(@NotNull String string) {
            @NotNull String[] split = string.split("/", 2);

            if (split.length == 2) {
                return new Type(split[0], split[1]);
            } else if (split.length == 1) {
                return new Type(split[0], null);
            } else {
                throw new IllegalArgumentException("cannot parse '" + string + "' as a valid media type");
            }
        }

        // Object

        private final @NotNull String type;
        private final @Nullable String subtype;

        public Type(@NotNull String type, @Nullable String subtype) {
            this.type = type;
            this.subtype = subtype;

            if ((type.contains(";") || type.contains(",")) || (subtype != null && (subtype.contains(";") || subtype.contains(",")))) {
                throw new IllegalArgumentException("type or subtype with illegal characters");
            }
        }

        // Getters

        /**
         * Returns the type of this media type {@link Type}.
         *
         * @return the type of this media type {@link Type}
         */
        public @NotNull String getType() {
            return type;
        }

        /**
         * Returns the subtype of this media type {@link Type}.
         *
         * @return the subtype of this media type {@link Type}, or null if none
         */
        public @Nullable String getSubType() {
            return subtype;
        }

        /**
         * Determines whether this media type {@link Type} is multipart.
         *
         * @return true if this media type {@link Type} is multipart, false otherwise
         */
        public boolean isMultipart() {
            return getType().equalsIgnoreCase("multipart");
        }

        // Implementations

        @Override
        public int length() {
            return toString().length();
        }
        @Override
        public char charAt(int index) {
            return toString().charAt(index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return toString().subSequence(start, end);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Type type = (Type) o;
            return this.type.equalsIgnoreCase(type.type) && (subtype != null && type.subtype != null && subtype.equalsIgnoreCase(type.subtype));
        }
        @Override
        public int hashCode() {
            return Objects.hash(type.toLowerCase(), (subtype != null ? subtype.toLowerCase() : null));
        }
        @Override
        public @NotNull String toString() {
            return type + (subtype != null ? "/" + subtype : "");
        }

    }

    /**
     * Represents a parameter of a media type, consisting of a key-value pair.
     *
     * @author Daniel Meinicke
     * @since 0.1
     */
    final class Parameter {

        private final @NotNull String key;
        private final @NotNull String value;

        public Parameter(@NotNull String key, @NotNull String value) {
            this.key = key;
            this.value = value;

            if (key.contains(";") || key.contains(",") || value.contains(";") || value.contains(",")) {
                throw new IllegalArgumentException("content type parameter key or value with illegal characters");
            }
        }

        // Getters

        /**
         * Returns the key of this parameter.
         *
         * @return the key of this parameter
         */
        public @NotNull String getKey() {
            return key;
        }

        /**
         * Returns the value of this parameter.
         *
         * @return the value of this parameter
         */
        public @NotNull String getValue() {
            return value;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            @NotNull Parameter parameter = (Parameter) o;
            return Objects.equals(getKey(), parameter.getKey()) && Objects.equals(getValue(), parameter.getValue());
        }
        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getValue());
        }

        @Override
        public @NotNull String toString() {
            return getKey() + "=" + getValue();
        }

    }

    /**
     * Represents a boundary parameter for multipart media types.
     *
     * @author Daniel Meinicke
     * @since 0.1
     */
    @ApiStatus.Experimental
    final class Boundary implements CharSequence {

        private final @NotNull String name;

        public Boundary(@NotNull String name) {
            this.name = name;
        }

        /**
         * Returns the name of this boundary.
         *
         * @return the name of this boundary
         */
        public @NotNull String getName() {
            return name;
        }

        // Implementations

        @Override
        public int length() {
            return name.length();
        }
        @Override
        public char charAt(int index) {
            return name.charAt(index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return name.subSequence(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Boundary boundary = (Boundary) o;
            return Objects.equals(name, boundary.name);
        }
        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public @NotNull String toString() {
            return name;
        }
    }

}
