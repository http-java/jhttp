package dev.jhttp.core.header.module;

import dev.jhttp.utilities.KeyUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class KeepAlive {

    // Static initializers

    public static @NotNull KeepAlive parse(@NotNull String string) throws ParseException {
        @NotNull Map<String, String> map = KeyUtilities.read(string, '=', ',');
        @Nullable Duration timeout = null;
        @Nullable Duration maximum = null;

        for (@NotNull String key : map.keySet()) try {
            if (key.equalsIgnoreCase("timeout")) {
                timeout = Duration.ofSeconds(Long.parseLong(map.get(key)));
            } else if (key.equalsIgnoreCase("max")) {
                maximum = Duration.ofSeconds(Long.parseLong(map.get(key)));
            } else {
                throw new IllegalArgumentException("keep alive with illegal parameter '" + key + "'");
            }
        } catch (@NotNull NumberFormatException ignore) {
            throw new IllegalArgumentException("Illegal parameter value '" + key + "': " + map.get(key));
        }

        if (timeout == null) {
            throw new IllegalArgumentException("keep alive missing timeout parameter");
        }

        return new KeepAlive(timeout, maximum);
    }
    public static boolean validate(@NotNull String string) {
        @NotNull Map<String, String> map = KeyUtilities.read(string, '=', ',');

        if (!map.containsKey("timeout")) {
            return false;
        } else for (@NotNull Entry<String, String> entry : KeyUtilities.read(string, '=', ',').entrySet()) try {
            if (entry.getKey().equalsIgnoreCase("timeout")) {
                Long.parseLong(entry.getValue());
            } else if (entry.getKey().equalsIgnoreCase("max")) {
                Long.parseLong(entry.getValue());
            } else {
                return false;
            }
        } catch (@NotNull NumberFormatException ignore) {
            return false;
        }

        return true;
    }

    public static @NotNull KeepAlive create(final @NotNull Duration timeout) {
        return create(timeout, null);
    }
    public static @NotNull KeepAlive create(final @NotNull Duration timeout, final @Nullable Duration maximum) {
        return new KeepAlive(timeout, maximum);
    }

    // Object

    private final @NotNull Duration timeout;
    private final @Nullable Duration maximum;

    protected KeepAlive(@NotNull Duration timeout, @Nullable Duration maximum) {
        this.timeout = timeout;
        this.maximum = maximum;
    }

    // Getters

    public @NotNull Duration getTimeout() {
        return timeout;
    }
    public @Nullable Duration getMaximum() {
        return maximum;
    }

    // Implementations

    @Override
    public final boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof KeepAlive)) return false;
        @NotNull KeepAlive keepAlive = (KeepAlive) object;
        return Objects.equals(getTimeout(), keepAlive.getTimeout()) && Objects.equals(getMaximum(), keepAlive.getMaximum());
    }
    @Override
    public final int hashCode() {
        return Objects.hash(getTimeout(), getMaximum());
    }

    @Override
    public final @NotNull String toString() {
        @NotNull StringBuilder builder = new StringBuilder("timeout=" + getTimeout().getSeconds());

        if (getMaximum() != null) {
            builder.append(", max=").append(getMaximum().getSeconds());
        }

        return builder.toString();
    }

}
