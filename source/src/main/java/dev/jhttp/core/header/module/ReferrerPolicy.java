package dev.jhttp.core.header.module;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum ReferrerPolicy {

    NO_REFERRER("no-referrer"),
    NO_REFERRER_WHEN_DOWNGRADE("no-referrer-when-downgrade"),
    ORIGIN("origin"),
    ORIGIN_WHEN_CROSS_ORIGIN("origin-when-cross-origin"),
    SAME_ORIGIN("same-origin"),
    STRICT_ORIGIN("strict-origin"),
    STRICT_ORIGIN_WHEN_CROSS_ORIGIN("strict-origin-when-cross-origin"),
    UNSAFE_URL("unsafe-url"),
    ;

    private final @NotNull String id;

    ReferrerPolicy(@NotNull String id) {
        this.id = id;
    }

    @Contract(pure = true)
    public @NotNull String getId() {
        return id;
    }

    // Static initializers

    public static @NotNull ReferrerPolicy getById(@NotNull String id) {
        @NotNull Optional<ReferrerPolicy> optional = Arrays.stream(values()).filter(type -> type.getId().equalsIgnoreCase(id)).findFirst();
        return optional.orElseThrow(() -> new NullPointerException("there's no referrer policy with id '" + id + "'"));
    }

}
