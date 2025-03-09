package dev.jhttp.core.header.module;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum SiteData {

    CACHE("cache"),
    CLIENT_HINTS("clientHints"),
    COOKIES("cookies"),
    STORAGE("storage"),
    EXECUTION_CONTEXTS("executionContexts"),
    ;

    private final @NotNull String id;

    SiteData(@NotNull String id) {
        this.id = id;
    }

    // Getters

    public @NotNull String getId() {
        return id;
    }

    // Static initializers

    public static @NotNull SiteData getById(@NotNull String id) {
        @NotNull Optional<SiteData> optional = Arrays.stream(values()).filter(type -> type.getId().equalsIgnoreCase(id)).findFirst();
        return optional.orElseThrow(() -> new NullPointerException("there's no site data type with id '" + id + "'"));
    }

}
