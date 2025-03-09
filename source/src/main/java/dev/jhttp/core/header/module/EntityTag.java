package dev.jhttp.core.header.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Entity Tag (ETag) used in HTTP headers to identify specific versions of resources.
 * <p>
 * An EntityTag can be either strong or weak. Strong ETags are precise validators that change whenever the resource
 * changes. Weak ETags are less precise and can be used for comparison but not for exact matching.
 * <p>
 * This interface provides methods to retrieve the name of the EntityTag and to check whether it is weak or strong.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.3">RFC 7232 Section 2.3</a>
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface EntityTag {

    // Static initializers

    /**
     * Creates a new {@link EntityTag} instance with the specified name and weakness flag.
     * <p>
     * This method returns an anonymous implementation of the {@code EntityTag} interface.
     *
     * @param name The name of the EntityTag. Must not be null.
     * @param weak Whether the EntityTag is weak.
     * @return A new {@link EntityTag} instance.
     * @throws NullPointerException If the {@code name} parameter is null.
     */
    static @NotNull EntityTag create(final @NotNull String name, final boolean weak) {
        return new EntityTag() {

            // Object

            @Override
            public @NotNull String getName() {
                return name;
            }
            @Override
            public boolean isWeak() {
                return weak;
            }

            // Implementations

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                @NotNull EntityTag tag = (EntityTag) object;
                return isWeak() == tag.isWeak() && Objects.equals(getName(), tag.getName());
            }
            @Override
            public int hashCode() {
                return Objects.hash(name, weak);
            }

            @Override
            public @NotNull String toString() {
                return Parser.serialize(this);
            }

        };
    }

    // Getters

    /**
     * Returns the name of this EntityTag.
     * <p>
     * The name must comply with the restrictions defined by the HTTP/1.1 specification (RFC 7232), which states that it should be
     * a string of ASCII characters excluding double quotes ("") and backslashes (\).
     *
     * @return The name of this EntityTag. Will not be null.
     */
    @NotNull String getName();

    /**
     * Indicates whether this EntityTag is weak.
     * <p>
     * Weak EntityTags are prefixed with "W/" and are used for comparison rather than exact matching.
     *
     * @return {@code true} if this EntityTag is weak; {@code false} otherwise.
     */
    boolean isWeak();

    // Classes

    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        /**
         * A regular expression pattern to validate the name of an EntityTag (ETag) according to the HTTP/1.1 specification.
         * <p>
         * An ETag name is a string of ASCII characters excluding double quotes ("") and backslashes (\).
         * It is used to uniquely identify a version of a resource.
         * This pattern ensures that ETag names adhere to the restrictions specified in RFC 7232.
         *
         * @see <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.3">RFC 7232 Section 2.3</a>
         */
        private static final @NotNull Pattern ETAG_NAME_PATTERN = Pattern.compile("^[\\x21\\x23-\\x5B\\x5D-\\x7E]+$");

        /**
         * Serializes an {@link EntityTag} into its string representation according to the HTTP/1.1 specification.
         * <p>
         * This method converts an {@code EntityTag} object into a string that can be used in HTTP headers. The serialization
         * process takes into account whether the entity tag is weak or strong. A weak entity tag is prefixed with {@code "W/"},
         * while a strong entity tag is not. The name of the entity tag is enclosed in double quotes.
         * <p>
         * For example:
         * <ul>
         *     <li>A strong entity tag with the name "example" will be serialized as {@code "\"example\""}.</li>
         *     <li>A weak entity tag with the name "example" will be serialized as {@code "W/\"example\""}.</li>
         * </ul>
         * <p>
         * The name of the entity tag must comply with the restrictions defined by the HTTP/1.1 specification (RFC 7232), which
         * states that it should be a string of ASCII characters excluding double quotes ("") and backslashes (\).
         *
         * @param tag The {@link EntityTag} to serialize. Must not be null.
         * @return The string representation of the {@code EntityTag}. Will not be null.
         * @throws NullPointerException If the {@code tag} parameter is null.
         * @see <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.3">RFC 7232 Section 2.3</a>
         */
        public static @NotNull String serialize(@NotNull EntityTag tag) {
            return (tag.isWeak() ? "W/" : "") + "\"" + tag.getName() + "\"";
        }

        /**
         * Parse a string representation of an EntityTag into an EntityTag object.
         *
         * @param string The string representation of the EntityTag.
         * @return The parsed EntityTag object.
         * @throws IllegalArgumentException If the input string is not a valid EntityTag.
         * @throws ParseException If the string param isn't a valid entity tag
         */
        public static @NotNull EntityTag deserialize(@NotNull String string) throws ParseException {
            @NotNull Pattern internal = Pattern.compile("\"(?<tag>[^\"]*)\"");
            @NotNull Matcher matcher = internal.matcher(string);

            if (!validate(string) || !matcher.find()) {
                throw new ParseException("cannot parse '" + string + "' as a valid entity tag", 0);
            } else {
                boolean weak = string.startsWith("W/") || string.startsWith("w/");
                @NotNull String name = matcher.group("tag");

                return create(name, weak);
            }
        }

        /**
         * Validate if a string is a valid EntityTag, according to the HTTP/1.1 specification.
         *
         * @param string The string to validate.
         * @return True if the string is a valid EntityTag, false otherwise.
         */
        public static boolean validate(@NotNull String string) {
            if ((string.startsWith("W/\"") || string.startsWith("\"")) && string.endsWith("\"")) {
                @NotNull Pattern internal = Pattern.compile("\"(?<tag>[^\"]*)\"");
                @NotNull Matcher matcher = internal.matcher(string);
                if (!matcher.find()) return false;

                return matcher.group("tag").matches(ETAG_NAME_PATTERN.pattern());
            } else {
                return false;
            }
        }

    }

}
