package dev.jhttp.core.header.module;

import codes.laivy.address.domain.Domain;
import dev.jhttp.core.protocol.HttpProtocol;
import dev.jhttp.core.url.Host;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Objects;

public class Location implements ContentSecurityPolicy.Source {

    // Static initializers

    public static boolean validate(@NotNull String string) {
        try {
            if (!string.startsWith("/")) {
                // Get the protocol
                @Nullable HttpProtocol protocol;

                if (string.toLowerCase().startsWith("https://") || string.toLowerCase().startsWith("http://")) {
                    if (string.toLowerCase().startsWith("https://")) {
                        protocol = HttpProtocol.HTTPS;
                    } else if (string.toLowerCase().startsWith("http://")) {
                        protocol = HttpProtocol.HTTP;
                    } else {
                        return false;
                    }

                    string = string.substring(protocol.getName().length());
                }

                // Get domain
                @NotNull String[] parts = string.split("/", 2);
                if (!Host.validate(parts[0])) return false;

                // Finish with uri
                string = "/" + (parts.length == 2 ? parts[1] : "");
            }

            // Parse uri
            new URI(URLDecoder.decode(string, "UTF-8"));

            // Finish successfully
            return true;
        } catch (@NotNull UnsupportedEncodingException | @NotNull URISyntaxException ignore) {
        }

        return false;
    }
    public static @NotNull Location parse(@NotNull String string) {
        if (validate(string)) try {
            @Nullable Domain domain = null;
            @NotNull URI uri;

            if (string.startsWith("/")) { // It's a path
                uri = URI.create(URLDecoder.decode(string, "UTF-8"));
            } else { // Parse domain and path
                @Nullable HttpProtocol protocol = null;

                if (string.toLowerCase().startsWith("https://") || string.toLowerCase().startsWith("http://")) {
                    if (string.toLowerCase().startsWith("https://")) {
                        protocol = HttpProtocol.HTTPS;
                    } else if (string.toLowerCase().startsWith("http://")) {
                        protocol = HttpProtocol.HTTP;
                    } else {
                        throw new IllegalArgumentException("cannot retrieve protocol from location");
                    }

                    string = string.substring(protocol.getName().length());
                }

                // Get domain
                @NotNull String[] parts = string.split("/", 2);

                domain = Domain.parse(parts[0]);
                uri = URI.create("/" + (parts.length == 2 ? URLDecoder.decode(parts[1], "UTF-8") : ""));
            }

            return create(domain, uri);
        } catch (@NotNull UnsupportedEncodingException e) {
            throw new RuntimeException("cannot find UTF-8 charset on system", e);
        } else {
            throw new IllegalArgumentException("cannot parse '" + string + "' as a valid location");
        }
    }

    public static @NotNull Location create(@Nullable Domain domain, @NotNull URI uri) {
        return new Location(domain, uri);
    }

    // Object

    private final @Nullable Domain domain;
    private final @NotNull URI uri;

    protected Location(@Nullable Domain domain, @NotNull URI uri) {
        this.domain = domain;
        this.uri = uri;
    }

    // Getters

    public @Nullable Domain getDomain() {
        return domain;
    }
    public @NotNull URI getURI() {
        return uri;
    }

    // Implementations

    @Override
    public final boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof Location)) return false;
        @NotNull Location location = (Location) object;
        return Objects.equals(getDomain(), location.getDomain()) && Objects.equals(getURI(), location.getURI());
    }
    @Override
    public final int hashCode() {
        return Objects.hash(getDomain(), getURI());
    }

    @Override
    public final @NotNull String toString() {
        if (getDomain() != null) {
            @NotNull StringBuilder builder = new StringBuilder(getDomain().toString());

            if (!getURI().toString().isEmpty() && !getURI().toString().startsWith("/")) {
                builder.append("/");
            }

            builder.append(getURI());
            return builder.toString();
        } else {
            return getURI().toString();
        }
    }

}
