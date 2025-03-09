package dev.jhttp.core.header.module;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public final class CrossOrigin {

    private CrossOrigin() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }

    // Classes

    public enum EmbedderPolicy {

        UNSAFE_NONE("unsafe-none"),
        REQUIRE_CORP("require-corp"),
        CREDENTIALLESS("credentialless"),
        ;

        private final @NotNull String id;

        EmbedderPolicy(@NotNull String id) {
            this.id = id;
        }

        public @NotNull String getId() {
            return id;
        }

        // Static initializers

        public static @NotNull EmbedderPolicy getById(@NotNull String id) {
            @NotNull Optional<EmbedderPolicy> optional = Arrays.stream(values()).filter(sameSite -> sameSite.getId().equalsIgnoreCase(id)).findFirst();
            return optional.orElseThrow(() -> new NullPointerException("There's no embedder policy enum with id '" + id + "'"));
        }

    }
    public enum OpenerPolicy {

        UNSAFE_NONE("unsafe-none"),
        SAME_ORIGIN_ALLOW_POPUPS("same-origin-allow-popups"),
        SAME_ORIGIN("same-origin"),
        ;

        private final @NotNull String id;

        OpenerPolicy(@NotNull String id) {
            this.id = id;
        }

        public @NotNull String getId() {
            return id;
        }

        // Static initializers

        public static @NotNull OpenerPolicy getById(@NotNull String id) {
            @NotNull Optional<OpenerPolicy> optional = Arrays.stream(values()).filter(sameSite -> sameSite.getId().equalsIgnoreCase(id)).findFirst();
            return optional.orElseThrow(() -> new NullPointerException("There's no opener policy enum with id '" + id + "'"));
        }

    }
    public enum ResourcePolicy {

        SAME_SITE("same-site"),
        SAME_ORIGIN("same-origin"),
        CROSS_ORIGIN("cross-origin"),
        ;

        private final @NotNull String id;

        ResourcePolicy(@NotNull String id) {
            this.id = id;
        }

        public @NotNull String getId() {
            return id;
        }

        // Static initializers

        public static @NotNull ResourcePolicy getById(@NotNull String id) {
            @NotNull Optional<ResourcePolicy> optional = Arrays.stream(values()).filter(sameSite -> sameSite.getId().equalsIgnoreCase(id)).findFirst();
            return optional.orElseThrow(() -> new NullPointerException("There's no resource policy enum with id '" + id + "'"));
        }

    }

}
