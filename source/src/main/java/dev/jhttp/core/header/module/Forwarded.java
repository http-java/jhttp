package dev.jhttp.core.header.module;

import codes.laivy.address.host.HttpHost;
import dev.jhttp.core.protocol.HttpProtocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Forwarded {

    // Static initializers

    static @NotNull Forwarded create(@Nullable Target by, @Nullable Target for_, @Nullable HttpHost<?> host, @Nullable HttpProtocol protocol) {
        return new Forwarded() {

            // Object

            @Override
            public @Nullable Target getBy() {
                return by;
            }
            @Override
            public @Nullable Target getFor() {
                return for_;
            }
            @Override
            public @Nullable HttpHost<?> getHost() {
                return host;
            }
            @Override
            public @Nullable HttpProtocol getProtocol() {
                return protocol;
            }

            // Implementations

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                @NotNull Forwarded that = (Forwarded) object;
                return Objects.equals(getBy(), that.getBy()) && Objects.equals(getFor(), that.getFor()) && Objects.equals(getHost(), that.getHost()) && getProtocol() == that.getProtocol();
            }
            @Override
            public int hashCode() {
                return Objects.hash(by, for_, host, protocol);
            }
            @Override
            public @NotNull String toString() {
                return Parser.serialize(this);
            }

        };
    }

    // Getters

    @Nullable Target getBy();
    @Nullable Target getFor();
    @Nullable HttpHost<?> getHost();
    @Nullable HttpProtocol getProtocol();

    // Classes

    interface Target {

        // Static initializers

        static @NotNull Target obfuscated(@NotNull String string) {
            if (!string.startsWith("_")) {
                throw new IllegalArgumentException("obfuscated forwarded targets must start with '_'");
            }

            return new Target() {
                @Override
                public boolean equals(@Nullable Object object) {
                    if (this == object) return true;
                    if (!(object instanceof Target)) return false;
                    @NotNull Target that = (Target) object;
                    return toString().equalsIgnoreCase(that.toString());
                }
                @Override
                public int hashCode() {
                    return Objects.hash(string);
                }
                @Override
                public @NotNull String toString() {
                    return string;
                }
            };
        }
        static @NotNull Target host(@NotNull HttpHost<?> host) {
            return new Target() {
                @Override
                public boolean equals(@Nullable Object object) {
                    if (this == object) return true;
                    if (!(object instanceof Target)) return false;
                    @NotNull Target that = (Target) object;
                    return toString().equalsIgnoreCase(that.toString());
                }
                @Override
                public int hashCode() {
                    return Objects.hash(host);
                }
                @Override
                public @NotNull String toString() {
                    return host.toString();
                }
            };
        }
        static @NotNull Target unknown() {
            final @NotNull String unknown = "unknown";

            return new Target() {
                @Override
                public boolean equals(@Nullable Object object) {
                    if (this == object) return true;
                    if (!(object instanceof Target)) return false;
                    @NotNull Target that = (Target) object;
                    return toString().equalsIgnoreCase(that.toString());
                }
                @Override
                public int hashCode() {
                    return Objects.hash(unknown);
                }
                @Override
                public @NotNull String toString() {
                    return unknown;
                }
            };
        }
        static @NotNull Target hidden() {
            final @NotNull String hidden = "hidden";

            return new Target() {
                @Override
                public boolean equals(@Nullable Object object) {
                    if (this == object) return true;
                    if (!(object instanceof Target)) return false;
                    @NotNull Target that = (Target) object;
                    return toString().equalsIgnoreCase(that.toString());
                }
                @Override
                public int hashCode() {
                    return Objects.hash(hidden);
                }
                @Override
                public @NotNull String toString() {
                    return hidden;
                }
            };
        }
        static @NotNull Target secret() {
            final @NotNull String secret = "secret";

            return new Target() {
                @Override
                public boolean equals(@Nullable Object object) {
                    if (this == object) return true;
                    if (!(object instanceof Target)) return false;
                    @NotNull Target that = (Target) object;
                    return toString().equalsIgnoreCase(that.toString());
                }
                @Override
                public int hashCode() {
                    return Objects.hash(secret);
                }
                @Override
                public @NotNull String toString() {
                    return secret;
                }
            };
        }

        // Object

        default boolean isObfuscated() {
            return toString().startsWith("_");
        }
        default boolean isHidden() {
            return toString().equalsIgnoreCase("hidden");
        }
        default boolean isSecret() {
            return toString().equalsIgnoreCase("secret");
        }

        @NotNull String toString();

    }

    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        private static final @NotNull Pattern TARGET_PARSE_PATTERN = Pattern.compile("^_(unknown|^(^(localhost|(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|(?:\\d{1,3}\\.){3}\\d{1,3}|\\[(?:[a-fA-F0-9]{0,4}:){2,7}(?::[a-fA-F0-9]{0,4}){1,7})])(?::\\d{1,5})?$)$");
        private static final @NotNull Pattern FORWARDED_PATTERN = Pattern.compile(
                "(?i)(?:for\\s*=\\s*(?<for>" + TARGET_PARSE_PATTERN.pattern() + "|[^\\s*;\\s*]+)\\s*;?\\s*|" +
                        "proto\\s*=\\s*(?<protocol>https?)\\s*;?\\s*|" +
                        "by\\s*=\\s*(?<by>" + TARGET_PARSE_PATTERN.pattern() + "|[^\\s*;\\s*]+)\\s*;?\\s*|" +
                        "host\\s*=\\s*(?<host>[^\\s*;\\s*]+)\\s*;?\\s*)*\\s*"
        );

        public static @NotNull String serialize(@NotNull Forwarded forwarded) {
            @NotNull StringBuilder builder = new StringBuilder();

            if (forwarded.getBy() != null) {
                builder.append("by=").append(forwarded.getBy()).append(";");
            } if (forwarded.getFor() != null) {
                builder.append("for=").append(forwarded.getFor()).append(";");
            } if (forwarded.getHost() != null) {
                builder.append("host=").append(forwarded.getHost()).append(";");
            } if (forwarded.getProtocol() != null) {
                builder.append("proto=").append(forwarded.getProtocol().name().toLowerCase()).append(";");
            }

            if (builder.charAt(builder.length() - 1) == ';') {
                return builder.subSequence(0, builder.length() - 1).toString();
            } else {
                return builder.toString();
            }
        }
        public static @NotNull Forwarded deserialize(@NotNull String string) throws ParseException {
            @NotNull Matcher matcher = FORWARDED_PATTERN.matcher(string);

            if (matcher.matches()) {
                @Nullable Target by = null;
                @Nullable Target for_ = null;
                @Nullable HttpHost<?> host = null;
                @Nullable HttpProtocol protocol = null;

                // By
                {
                    @Nullable String value = matcher.group("by");

                    if (value != null) {
                        value = (value.startsWith("\"") && value.endsWith("\"")) ? value.substring(1, value.length() - 1) : value;

                        if (value.equalsIgnoreCase("unknown")) {
                            by = Target.unknown();
                        } else if (value.equalsIgnoreCase("hidden")) {
                            by = Target.hidden();
                        } else if (value.equalsIgnoreCase("secret")) {
                            by = Target.secret();
                        } else if (value.startsWith("_")) {
                            by = Target.obfuscated(value);
                        } else if (HttpHost.validate(value)) {
                            by = Target.host(HttpHost.parse(value));
                        } else {
                            throw new ParseException("the value '" + value + "' cannot be parsed as a valid 'by' forwarded target", matcher.start("by"));
                        }
                    }
                }

                // For
                {
                    @Nullable String value = matcher.group("for");

                    if (value != null) {
                        value = (value.startsWith("\"") && value.endsWith("\"")) ? value.substring(1, value.length() - 1) : value;

                        if (value.equalsIgnoreCase("unknown")) {
                            for_ = Target.unknown();
                        } else if (value.equalsIgnoreCase("hidden")) {
                            by = Target.hidden();
                        } else if (value.equalsIgnoreCase("secret")) {
                            by = Target.secret();
                        } else if (value.startsWith("_")) {
                            for_ = Target.obfuscated(value);
                        } else if (HttpHost.validate(value)) {
                            for_ = Target.host(HttpHost.parse(value));
                        } else {
                            throw new ParseException("the value '" + value + "' cannot be parsed as a valid 'for' forwarded target", matcher.start("for"));
                        }
                    }
                }

                // Host
                {
                    @Nullable String value = matcher.group("host");

                    if (value != null) {
                        if (HttpHost.validate(value)) {
                            host = HttpHost.parse(value);
                        } else {
                            throw new ParseException("cannot parse '" + value + "' into a valid host to forwarded object", matcher.start(0));
                        }
                    }
                }

                // Protocol
                {
                    @Nullable String value = matcher.group("protocol");

                    if (value != null) {
                        if (value.equalsIgnoreCase("http")) {
                            protocol = HttpProtocol.HTTP;
                        } else if (value.equalsIgnoreCase("https")) {
                            protocol = HttpProtocol.HTTPS;
                        } else {
                            throw new ParseException("unknown http protocol type '" + value + "'", matcher.start("protocol"));
                        }
                    }
                }

                // Finish
                return create(by, for_, host, protocol);
            } else {
                throw new ParseException("cannot parse '" + string + "' as a valid forwarded object", 0);
            }
        }

        public static boolean validate(@NotNull String string) {
            return FORWARDED_PATTERN.matcher(string).matches();
        }

    }

}
