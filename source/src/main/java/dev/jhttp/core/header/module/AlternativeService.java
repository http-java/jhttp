package dev.jhttp.core.header.module;

import dev.jhttp.core.url.UriAuthority;
import dev.jhttp.utilities.KeyUtilities;
import dev.jhttp.utilities.StringUtils;
import dev.jhttp.core.version.HttpVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class AlternativeService {

    // Static initializers

    public static boolean validate(@NotNull String string) {
        try {
            parse(string);
            return true;
        } catch (@NotNull ParseException | @NotNull UnknownHostException | @NotNull URISyntaxException ignore) {
            return false;
        }
    }
    public static @NotNull AlternativeService parse(@NotNull String string) throws ParseException, UnknownHostException, URISyntaxException {
        @NotNull Map<String, String> keys = KeyUtilities.read(string, '=', ';');

        // Duration and age
        @Nullable Duration age = keys.containsKey("ma") && StringUtils.isInteger(keys.get("ma")) ? Duration.ofSeconds(Integer.parseInt(keys.get("ma"))) : null;
        boolean persist = keys.containsKey("persist") && keys.get("persist").trim().equals("1");

        // Protocol
        @NotNull Optional<String> optional = keys.keySet().stream().filter(key -> !key.equalsIgnoreCase("ma") && !key.equalsIgnoreCase("persist")).findFirst();

        if (optional.isPresent()) {
            @NotNull String protocol = optional.get();
            @Nullable UriAuthority authority = UriAuthority.parse(keys.get(protocol));

            return new AlternativeService(protocol.getBytes(StandardCharsets.UTF_8), authority, age, persist);
        } else {
            throw new ParseException("cannot parse '" + string + "' into a valid alternative service because it's missing protocol", 0);
        }
    }

    public static @NotNull AlternativeService create(@NotNull HttpVersion version, @NotNull UriAuthority authority) {
        return new AlternativeService(version.getId(), authority, Duration.ofDays(1), false);
    }
    public static @NotNull AlternativeService create(@NotNull HttpVersion version, @NotNull UriAuthority authority, @NotNull Duration age, boolean persist) {
        return new AlternativeService(version.getId(), authority, age, persist);
    }
    public static @NotNull AlternativeService create(byte[] protocolId, @NotNull UriAuthority authority, @NotNull Duration age, boolean persist) {
        return new AlternativeService(protocolId, authority, age, persist);
    }

    // Object

    private final byte[] version;
    private final @NotNull UriAuthority authority;
    private final @Nullable Duration age;
    private final boolean persistent;

    private AlternativeService(byte[] version, @NotNull UriAuthority authority, @Nullable Duration age, boolean persistent) {
        this.version = version;
        this.authority = authority;
        this.age = age;
        this.persistent = persistent;
    }

    // Getters

    public byte[] getVersion() {
        return version;
    }
    public @NotNull UriAuthority getAuthority() {
        return authority;
    }
    public @Nullable Duration getAge() {
        return age;
    }

    public boolean isPersistent() {
        return persistent;
    }

    // Implementations

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlternativeService that = (AlternativeService) o;
        return Objects.equals(getAge() != null ? getAge().getSeconds() : null, that.getAge() != null ? that.getAge().getSeconds() : null) && isPersistent() == that.isPersistent() && Arrays.equals(getVersion(), that.getVersion()) && Objects.equals(getAuthority(), that.getAuthority());
    }
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(version), authority, age, persistent);
    }

    @Override
    public @NotNull String toString() {
        @NotNull StringBuilder builder = new StringBuilder(new String(getVersion()) + "=\"" + getAuthority() + "\"");

        if (getAge() != null) {
            builder.append("; ma=").append(getAge().getSeconds());
        }
        if (isPersistent()) builder.append("; persist=1");

        return builder.toString();
    }

}
