package dev.jhttp.core.header.module;

import codes.laivy.address.domain.Domain;
import codes.laivy.address.host.HttpHost;
import dev.jhttp.utilities.DateUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an HTTP cookie with key-value pairs and additional attributes.
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public class Cookie {

    // Static initializers

    /**
     * Creates a new {@code Cookie} instance with the given key and value.
     *
     * @param key   the key of the cookie must match the cookie key pattern.
     * @param value the value of the cookie must match the cookie value pattern.
     * @return a new {@code Cookie} instance.
     * @throws IllegalArgumentException if the key or value is invalid.
     */
    public static @NotNull Cookie create(final @NotNull String key, final @NotNull String value) {
        return new Cookie(key, value);
    }

    // Object

    private final @NotNull String name;
    private final @NotNull String value;

    private Cookie(@NotNull String name, @NotNull String value) {
        this.name = name;
        this.value = value;

        if (!name.matches(Parser.COOKIE_NAME_PATTERN.pattern())) {
            throw new IllegalArgumentException("invalid cookie key name '" + name + "'");
        } else if (!value.matches(Parser.COOKIE_VALUE_PATTERN.pattern())) {
            throw new IllegalArgumentException("invalid cookie value name '" + value +  "'");
        }
    }

    // Getters

    /**
     * Gets the name of the cookie.
     * @return the name of the cookie.
     */
    public final @NotNull String getName() {
        return name;
    }

    /**
     * Gets the value of the cookie.
     * @return the value of the cookie.
     */
    public final @NotNull String getValue() {
        return value;
    }

    // Implementations

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        @NotNull Cookie cookie = (Cookie) object;
        return Objects.equals(getName(), cookie.getName()) && Objects.equals(getValue(), cookie.getValue());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public @NotNull String toString() {
        return getName() + "=" + getValue();
    }

    // Classes

    /**
     * Utility class for cookie parsing and validation.
     */
    public static final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        private static final @NotNull Pattern COOKIE_NAME_PATTERN = Pattern.compile("^[\\x21\\x23-\\x27\\x2A\\x2B\\x2D\\x2E\\x30-\\x39\\x41-\\x5A\\x5E-\\x7A\\x7C\\x7E]+$");
        private static final @NotNull Pattern COOKIE_VALUE_PATTERN = Pattern.compile("^(?:[^\\x00-\\x1F\\x22\\x2C\\x3B\\x5C\\x7F]+|\"(?:[^\\x00-\\x1F\\x22\\x2C\\x3B\\x5C\\x7F]|\")*\")?$");
        private static final @NotNull Pattern COOKIE_PATTERN = Pattern.compile("^(?<key>[\\x21\\x23-\\x27\\x2A\\x2B\\x2D\\x2E\\x30-\\x39\\x41-\\x5A\\x5E-\\x7A\\x7C\\x7E]+)\\s*=\\s*(?<value>[^\\x00-\\x1F\\x22\\x2C\\x3B\\x5C\\x7F]+|\"(?:[^\\x00-\\x1F\\x22\\x2C\\x3B\\x5C\\x7F]|\")*\")?$");

        /**
         * Serializes a {@code Cookie} to its string representation.
         *
         * @param cookie the cookie to serialize.
         * @return the serialized string representation of the cookie.
         * @throws IllegalArgumentException if the cookie name or value is invalid.
         */
        public static @NotNull String serialize(@NotNull Cookie cookie) {
            if (!cookie.getName().matches(Parser.COOKIE_NAME_PATTERN.pattern())) {
                throw new IllegalArgumentException("invalid cookie name '" + cookie.getName() + "'");
            } else if (!cookie.getValue().matches(Parser.COOKIE_VALUE_PATTERN.pattern())) {
                throw new IllegalArgumentException("invalid cookie value name '" + cookie.getValue() +  "'");
            }

            return cookie.getName() + "=" + cookie.getValue();
        }

        /**
         * Deserializes a string into a {@code Cookie}.
         *
         * @param string the string to deserialize.
         * @return the deserialized {@code Cookie}.
         * @throws ParseException if the string cannot be parsed as a cookie.
         */
        public static @NotNull Cookie deserialize(@NotNull String string) throws ParseException {
            @NotNull Matcher matcher = COOKIE_PATTERN.matcher(string);

            if (matcher.matches()) {
                @NotNull String name = matcher.group("key");
                @NotNull String value = matcher.group("value");

                return create(name, value);
            } else {
                throw new ParseException("cannot parse '" + string + "' as a cookie", 0);
            }
        }

        /**
         * Validates if a string is a valid cookie representation.
         *
         * @param string the string to validate.
         * @return {@code true} if the string is a valid cookie, {@code false} otherwise.
         */
        public static boolean validate(@NotNull String string) {
            return COOKIE_PATTERN.matcher(string).matches();
        }

    }

    /**
     * Represents an HTTP cookie request with additional attributes.
     */
    public static final class Request extends Cookie {

        // Static initializers

        /**
         * Creates a new {@code Builder} instance for constructing a {@code Request}.
         *
         * @param name  the name of the cookie.
         * @param value the value of the cookie.
         * @return a new {@code Builder} instance.
         */
        public static @NotNull Builder builder(@NotNull String name, @NotNull String value) {
            return new Builder(name, value);
        }

        /**
         * Creates a new {@code Builder} instance for a cookie expiration with the name
         *
         * @param name the name of the cookie that will be expired.
         * @return a new cookie builder instance with the expiration order
         */
        public static @NotNull Builder expire(@NotNull String name) {
            return Request.builder(name, "").expires(Instant.ofEpochMilli(0));
        }

        /**
         * Creates a new {@code Request} instance with the given name and value.
         *
         * @param name   the name of the cookie.
         * @param value the value of the cookie.
         * @return a new {@code Request} instance.
         */
        public static @NotNull Request create(@NotNull String name, @NotNull String value) {
            return new Request(name, value, null, null, null, null, null, false, false, false);
        }

        // Object

        private final @Nullable HttpHost<Domain> domain;
        private final @Nullable Instant expires;
        private final @Nullable Duration maxAge;
        private final @Nullable URI path;
        private final @Nullable SameSite sameSite;

        private final boolean httpOnly;
        private final boolean partitioned;
        private final boolean secure;

        private Request(@NotNull String name, @NotNull String value, @Nullable HttpHost<Domain> domain, @Nullable Instant expires, @Nullable Duration maxAge, @Nullable URI path, @Nullable SameSite sameSite, boolean httpOnly, boolean partitioned, boolean secure) {
            super(name, value);

            this.domain = domain;
            this.expires = expires;
            this.maxAge = maxAge;
            this.path = path;
            this.sameSite = sameSite;
            this.httpOnly = httpOnly;
            this.partitioned = partitioned;
            this.secure = secure;

            if ((name.startsWith("__Secure-") || name.startsWith("__Host-")) && !isSecure()) {
                throw new IllegalArgumentException("The prefix '__Secure-' or '__Host-' at cookie request name can only be used if the secure attribute is true");
            } else if (name.startsWith("__Host-") && (getPath() == null || !getPath().toString().equals("/"))) {
                throw new IllegalArgumentException("The cookie request path must be '/' when the name starts with '__Host-'");
            }
        }

        // Getters

        /**
         * Gets the domain of the cookie.
         *
         * @return the domain of the cookie, or {@code null} if not set
         */
        public @Nullable HttpHost<Domain> getDomain() {
            return domain;
        }

        /**
         * Gets the expiration date of the cookie.
         *
         * @return the expiration date of the cookie, or {@code null} if not set
         */
        public @Nullable Instant getExpires() {
            return expires;
        }

        /**
         * Gets the maximum age of the cookie.
         *
         * @return the maximum age of the cookie, or {@code null} if not set
         */
        public @Nullable Duration getMaxAge() {
            return maxAge;
        }

        /**
         * Gets the path of the cookie.
         *
         * @return the path of the cookie, or {@code null} if not set
         */
        public @Nullable URI getPath() {
            return path;
        }

        /**
         * Gets the SameSite attribute of the cookie.
         *
         * @return the SameSite attribute of the cookie, or {@code null} if not set
         */
        public @Nullable SameSite getSameSite() {
            return sameSite;
        }

        /**
         * Checks if the cookie is HTTP only.
         *
         * @return {@code true} if the cookie is HTTP only, {@code false} otherwise
         */
        public boolean isHttpOnly() {
            return httpOnly;
        }

        /**
         * Checks if the cookie is partitioned.
         *
         * @return {@code true} if the cookie is partitioned, {@code false} otherwise
         */
        public boolean isPartitioned() {
            return partitioned;
        }

        /**
         * Checks if the cookie is secure.
         *
         * @return {@code true} if the cookie is secure, {@code false} otherwise
         */
        public boolean isSecure() {
            return secure;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            if (!super.equals(object)) return false;
            @NotNull Request request = (Request) object;
            return httpOnly == request.httpOnly && partitioned == request.partitioned && secure == request.secure && Objects.equals(domain, request.domain) && Objects.equals(expires, request.expires) && Objects.equals(maxAge, request.maxAge) && Objects.equals(path, request.path) && sameSite == request.sameSite;
        }
        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), domain, expires, maxAge, path, sameSite, httpOnly, partitioned, secure);
        }

        @Override
        public @NotNull String toString() {
            return Parser.serialize(this);
        }

        // Classes

        /**
         * Enum representing the SameSite attribute of a cookie.
         */
        public enum SameSite {

            STRICT("Strict"),
            LAX("Lax"),
            NONE("None"),
            ;

            private final @NotNull String id;

            SameSite(@NotNull String id) {
                this.id = id;
            }

            /**
             * Gets the identifier of the SameSite attribute.
             *
             * @return the identifier of the SameSite attribute
             */
            public @NotNull String getId() {
                return id;
            }

            // Static initializers

            /**
             * Retrieves a {@link SameSite} enum based on its identifier.
             *
             * @param id the identifier of the SameSite attribute
             * @return the corresponding {@link SameSite} enum
             * @throws NullPointerException if no SameSite attribute matches the given identifier
             */
            public static @NotNull SameSite getById(@NotNull String id) {
                @NotNull Optional<SameSite> optional = Arrays.stream(values()).filter(sameSite -> sameSite.getId().equalsIgnoreCase(id)).findFirst();
                return optional.orElseThrow(() -> new NullPointerException("There's no same site cookie request enum with id '" + id + "'"));
            }

        }

        /**
         * Builder class for creating {@link Request} instances.
         */
        public static final class Builder {

            private final @NotNull String name;
            private final @NotNull String value;

            private @Nullable HttpHost<Domain> domain;
            private @Nullable Instant expires;
            private @Nullable Duration maxAge;
            private @Nullable URI path;
            private @Nullable SameSite sameSite;

            private boolean httpOnly;
            private boolean partitioned;
            private boolean secure;

            private Builder(@NotNull String name, @NotNull String value) {
                this.name = name;
                this.value = value;

                if (!name.matches(Cookie.Parser.COOKIE_NAME_PATTERN.pattern())) {
                    throw new IllegalArgumentException("invalid cookie name '" + name + "'");
                } else if (!value.matches(Cookie.Parser.COOKIE_VALUE_PATTERN.pattern())) {
                    throw new IllegalArgumentException("invalid cookie value name '" + value +  "'");
                }
            }

            // Getters

            /**
             * Sets the domain for the cookie.
             *
             * @param domain the domain to set
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder domain(@NotNull HttpHost<Domain> domain) {
                this.domain = domain;
                return this;
            }

            /**
             * Sets the expiration date for the cookie.
             *
             * @param expires the expiration date to set
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder expires(@NotNull Instant expires) {
                this.expires = expires;
                return this;
            }

            /**
             * Sets the maximum age for the cookie.
             *
             * @param maxAge the maximum age to set
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder maxAge(@NotNull Duration maxAge) {
                this.maxAge = maxAge;
                return this;
            }

            /**
             * Sets the path for the cookie.
             *
             * @param path the path to set
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder path(@NotNull URI path) {
                this.path = path;
                return this;
            }

            /**
             * Sets the SameSite attribute for the cookie.
             *
             * @param sameSite the SameSite attribute to set
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder sameSite(@NotNull SameSite sameSite) {
                this.sameSite = sameSite;
                return this;
            }

            /**
             * Sets whether the cookie is HTTP only.
             *
             * @param httpOnly {@code true} if the cookie should be HTTP only, {@code false} otherwise
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder httpOnly(boolean httpOnly) {
                this.httpOnly = httpOnly;
                return this;
            }

            /**
             * Sets whether the cookie is partitioned.
             *
             * @param partitioned {@code true} if the cookie should be partitioned, {@code false} otherwise
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder partitioned(boolean partitioned) {
                this.partitioned = partitioned;
                return this;
            }

            /**
             * Sets whether the cookie is secure.
             *
             * @param secure {@code true} if the cookie should be secure, {@code false} otherwise
             * @return this builder
             */
            @Contract("_->this")
            public @NotNull Builder secure(boolean secure) {
                this.secure = secure;
                return this;
            }

            // Builder

            /**
             * Builds the {@link Request} instance with the specified attributes.
             *
             * @return the built {@link Request} instance
             */
            public @NotNull Request build() {
                return new Request(name, value, domain, expires, maxAge, path, sameSite, httpOnly, partitioned, secure);
            }

        }

        /**
         * Utility class for parsing and validating cookie requests.
         */
        public static final class Parser {
            private Parser() {
                throw new UnsupportedOperationException();
            }

            // Serializers

            /**
             * Serializes a {@link Request} to its string representation.
             *
             * @param request the request to serialize
             * @return the string representation of the request
             */
            public static @NotNull String serialize(@NotNull Request request) {
                @NotNull StringBuilder builder = new StringBuilder(request.getName()).append("=").append(request.getValue()).append("; ");

                if (request.getDomain() != null) {
                    builder.append("Domain=").append(request.getDomain().getName()).append("; ");
                } if (request.getExpires() != null) {
                    builder.append("Expires=").append(DateUtils.RFC822.convert(request.getExpires().atOffset(ZoneOffset.UTC))).append("; ");
                } if (request.getMaxAge() != null) {
                    builder.append("Max-Age=").append(request.getMaxAge().getSeconds()).append("; ");
                } if (request.getPath() != null) {
                    builder.append("Path=").append(request.getPath()).append("; ");
                } if (request.getSameSite() != null) {
                    builder.append("SameSite=").append(request.getSameSite().getId()).append("; ");
                }

                if (request.isHttpOnly()) {
                    builder.append("HttpOnly").append("; ");
                } if (request.isPartitioned()) {
                    builder.append("Partitioned").append("; ");
                } if (request.isSecure()) {
                    builder.append("Secure").append("; ");
                }

                return builder.substring(0, builder.length() - 2);
            }

            /**
             * Deserializes a string to a {@link Request}.
             *
             * @param string the string representation of the request
             * @return the deserialized {@link Request}
             * @throws ParseException if the string cannot be parsed as a request
             */
            public static @NotNull Request deserialize(@NotNull String string) throws ParseException {
                @NotNull String name;
                @NotNull String value;

                @Nullable HttpHost<Domain> domain = null;
                @Nullable Instant expires = null;
                @Nullable Duration maxAge = null;
                @Nullable URI path = null;
                @Nullable SameSite sameSite = null;

                boolean httpOnly = false;
                boolean partitioned = false;
                boolean secure = false;

                {
                    @NotNull String[] parts = string.split("\\s*=\\s*", 2);

                    name = parts[0];
                    value = parts[1].split("\\s*;\\s*")[0];

                    if (!name.matches(Cookie.Parser.COOKIE_NAME_PATTERN.pattern())) {
                        throw new ParseException("invalid cookie request name '" + name + "'", 0);
                    } else if (!value.matches(Cookie.Parser.COOKIE_VALUE_PATTERN.pattern())) {
                        throw new ParseException("invalid cookie request value '" + value + "'", 0);
                    }
                }

                for (@NotNull String part : Arrays.stream(string.split("\\s*;\\s*")).toArray(String[]::new)) {
                    if (part.equalsIgnoreCase("HttpOnly")) {
                        httpOnly = true;
                    } else if (part.equalsIgnoreCase("Partitioned")) {
                        partitioned = true;
                    } else if (part.equalsIgnoreCase("Secure")) {
                        secure = true;
                    } else {
                        @NotNull String[] parts = part.split("\\s*=\\s*", 2);

                        @NotNull String key = parts[0];
                        @NotNull String data = parts[1];

                        if (key.equals(name)) {
                            continue;
                        }

                        if (key.equalsIgnoreCase("expires")) {
                            expires = DateUtils.RFC822.convert(data).toInstant();
                        } else if (key.equalsIgnoreCase("max-age")) {
                            maxAge = Duration.ofSeconds(Long.parseLong(data));
                        } else if (key.equalsIgnoreCase("path")) {
                            path = URI.create(data);
                        } else if (key.equalsIgnoreCase("samesite")) {
                            sameSite = SameSite.getById(data);
                        } else if (key.equalsIgnoreCase("domain")) {
                            @NotNull HttpHost<?> host = HttpHost.parse(data);

                            if (!(host.getAddress() instanceof Domain)) {
                                throw new ParseException("invalid cookie request domain type '" + data + "'. The domain type must be a name domain!", 0);
                            }

                            //noinspection unchecked
                            domain = (HttpHost<Domain>) host;
                        } else {
                            throw new ParseException("unknown cookie request parameter '" + key + "' with value '" + data + "'", 0);
                        }
                    }
                }

                return new Request(name, value, domain, expires, maxAge, path, sameSite, httpOnly, partitioned, secure);
            }

            /**
             * Validates if a string is a valid cookie request representation.
             *
             * @param string the string to validate
             * @return {@code true} if the string is a valid request representation, {@code false} otherwise
             */
            public static boolean validate(@NotNull String string) {
                try {
                    deserialize(string);
                    return true;
                } catch (@NotNull Throwable throwable) {
                    return false;
                }
            }

        }

    }

}
