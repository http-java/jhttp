package dev.jhttp.core.header.module;

import dev.jhttp.core.body.HttpBody;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public interface Digest {

    // Static initializers

    // todo: Digest must have streams
    static @NotNull Digest create(@NotNull Algorithm algorithm, @NotNull HttpBody body) {
        return create(algorithm, body.toString());
    }
    static @NotNull Digest create(@NotNull Algorithm algorithm, @NotNull String string) {
        try {
            return algorithm.encode(string);
        } catch (@NotNull Throwable e) {
            throw new RuntimeException("cannot encode algorithm '" + algorithm.getId() + "'", e);
        }
    }

    // Object

    @NotNull Algorithm getAlgorithm();
    @NotNull String getValue();

    enum Algorithm {

        SHA512("sha-512", Status.STANDARD) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws NoSuchAlgorithmException {
                string = Algorithm.hash(string, "sha-512");
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return Algorithm.validateBase64(string);
            }
        },
        @Deprecated
        @ApiStatus.Experimental
        ID_SHA512("id-sha-512", Status.STANDARD) {
            @Override
            public @NotNull Digest encode(@NotNull String string) {
                return Algorithm.create(this, Base64.getEncoder().encodeToString(string.getBytes()));
            }
            @Override
            public boolean validate(@NotNull String string) {
                return Algorithm.validateBase64(string);
            }
        },

        SHA256("sha-256", Status.STANDARD) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws NoSuchAlgorithmException {
                string = Algorithm.hash(string, "sha-256");
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return Algorithm.validateBase64(string);
            }
        },
        @Deprecated
        @ApiStatus.Experimental
        ID_SHA256("id-sha-256", Status.STANDARD) {
            @Override
            public @NotNull Digest encode(@NotNull String string) {
                return Algorithm.create(this, Base64.getEncoder().encodeToString(string.getBytes()));
            }
            @Override
            public boolean validate(@NotNull String string) {
                return Algorithm.validateBase64(string);
            }
        },

        MD5("md5", Status.INSECURE) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws NoSuchAlgorithmException {
                string = Algorithm.hash(string, "md5");
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return string.matches("[a-fA-F0-9]{32}");
            }
        },
        SHA("sha", Status.INSECURE) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws NoSuchAlgorithmException {
                string = Algorithm.hash(string, "sha-1");
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return string.matches("[a-fA-F0-9]{40}");
            }
        },
        @ApiStatus.Experimental
        UNIXSUM("unixsum", Status.INSECURE) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws UnsupportedOperationException {
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return true;
            }
        },
        @ApiStatus.Experimental
        UNIXCKSUM("unixcksum", Status.INSECURE) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws UnsupportedOperationException {
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return true;
            }
        },
        @ApiStatus.Experimental
        ADLER32("adler32", Status.INSECURE) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws UnsupportedOperationException {
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return true;
            }
        },
        @ApiStatus.Experimental
        CRC32C("crc32c", Status.INSECURE) {
            @Override
            public @NotNull Digest encode(@NotNull String string) throws UnsupportedOperationException {
                return Algorithm.create(this, string);
            }
            @Override
            public boolean validate(@NotNull String string) {
                return true;
            }
        },
        ;

        private final @NotNull String id;
        private final @NotNull Status status;

        Algorithm(@NotNull String id, @NotNull Status status) {
            this.id = id;
            this.status = status;
        }

        // Getters

        public @NotNull String getId() {
            return id;
        }
        public @NotNull Status getStatus() {
            return status;
        }

        public abstract @NotNull Digest encode(@NotNull String string) throws NoSuchAlgorithmException;
        public abstract boolean validate(@NotNull String string);

        // Static initializers

        public static @NotNull Algorithm getById(@NotNull String id) {
            @NotNull Optional<Algorithm> optional = Arrays.stream(values()).filter(type -> type.getId().equalsIgnoreCase(id)).findFirst();
            return optional.orElseThrow(() -> new NullPointerException("there's no digest algorithm with id '" + id + "'"));
        }

        // Classes

        public enum Status {
            STANDARD,
            INSECURE
        }

        // Utilities

        private static boolean validateBase64(@NotNull String string) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(string);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        private static @NotNull String hash(@NotNull String input, @NotNull String algorithm) throws NoSuchAlgorithmException {
            @NotNull MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(input.getBytes());

            @NotNull StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        }
        private static @NotNull Digest create(@NotNull Algorithm algorithm, @NotNull String value) {
            return new Digest() {

                // Object

                @Override
                public @NotNull Algorithm getAlgorithm() {
                    return algorithm;
                }
                @Override
                public @NotNull String getValue() {
                    return value;
                }

                // Implementations

                @Override
                public boolean equals(@Nullable Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    @NotNull Digest that = (Digest) o;
                    return getAlgorithm().equals(that.getAlgorithm());
                }
                @Override
                public int hashCode() {
                    return Objects.hash(getAlgorithm());
                }

            };
        }

    }
    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        public static @NotNull String serialize(@NotNull Digest digest) {
            return digest.getAlgorithm().getId() + "=" + digest.getValue();
        }
        public static @NotNull Digest deserialize(@NotNull String string) throws ParseException {
            if (validate(string)) {
                @NotNull String[] split = string.split("=", 2);
                @NotNull Algorithm algorithm = Algorithm.getById(split[0]);

                return create(algorithm, string);
            } else {
                throw new ParseException("cannot parse '" + string + "' as a digest", 0);
            }
        }

        public static boolean validate(@NotNull String string) {
            @NotNull String[] split = string.split("=", 2);

            if (split.length != 2) {
                return false;
            } else try {
                return Algorithm.getById(split[0]).validate(split[1]);
            } catch (@NotNull NullPointerException ignore) {
                return false;
            }
        }

    }

}
