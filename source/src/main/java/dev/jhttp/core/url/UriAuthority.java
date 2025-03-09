package dev.jhttp.core.url;

import dev.jhttp.core.authorization.Credentials.Basic;
import dev.jhttp.core.protocol.HttpProtocol;
import org.jetbrains.annotations.*;

import java.net.IDN;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: replace all #validate and #parse methods from String to CharSequence
public final class UriAuthority {

    // Static initializers

    @ApiStatus.Internal
    private static final @NotNull Pattern PARSE_PATTERN = Pattern.compile("^(https?://)?(([^:@]*:[^@]*)@)?([^:/]*)(:[0-9]+)?(/.*)?$");

    public static boolean validate(@NotNull CharSequence uri) {
        return PARSE_PATTERN.matcher(uri).matches();
    }
    public static @NotNull UriAuthority parse(@NotNull CharSequence string) throws URISyntaxException {
        @Nullable Basic userInfo = null;
        @NotNull String hostName;
        int port;

        @NotNull Pattern pattern = PARSE_PATTERN;
        @NotNull Matcher matcher = pattern.matcher(string);

        if (matcher.find()) {
            @Nullable String scheme = matcher.group(1);

            try {
                @Nullable String temp = matcher.group(3);
                if (temp != null) userInfo = Basic.parse(temp);
            } catch (@NotNull ParseException throwable) {
                throw new URISyntaxException(string.toString(), throwable.getMessage(), matcher.start(3));
            }

            @NotNull String hostGroup = matcher.group(4);

            if (hostGroup.contains("@")) {
                throw new URISyntaxException(string.toString(), "invalid user info information", matcher.start(4));
            }

            hostName = IDN.toASCII(hostGroup);

            if (matcher.group(5) != null) {
                port = Integer.parseInt(matcher.group(5).substring(1));

                if (port < 0 || port > 65535) {
                    throw new URISyntaxException(string.toString(), "port out of range '" + port + "'", matcher.start(5) + 1);
                }
            } else if ("https://".equals(scheme)) {
                port = HttpProtocol.HTTPS.getPort();
            } else {
                port = HttpProtocol.HTTP.getPort();
            }
        } else {
            throw new URISyntaxException(string.toString(), "cannot parse into a valid uri authority", -1);
        }

        return new UriAuthority(userInfo, InetSocketAddress.createUnresolved(hostName, port));
    }

    public static @NotNull UriAuthority create(@NotNull Basic userInfo, @NotNull InetSocketAddress address) {
        return new UriAuthority(userInfo, address);
    }
    public static @NotNull UriAuthority create(@NotNull String address, @Range(from = 0, to = 65535) int port) {
        return new UriAuthority(null, InetSocketAddress.createUnresolved(address, port));
    }

    // Object

    private final @Nullable Basic userInfo;
    private final @NotNull InetSocketAddress address;

    private UriAuthority(@Nullable Basic userInfo, @NotNull InetSocketAddress address) {
        this.userInfo = userInfo;
        this.address = address;
    }

    // Getters

    public @Nullable Basic getUserInfo() {
        return userInfo;
    }

    @Contract(pure = true)
    public @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Contract(pure = true)
    public @NotNull String getHostName() {
        return getAddress().getHostName();
    }
    @Contract(pure = true)

    @Range(from = 0, to = 65535)
    public int getPort() {
        return getAddress().getPort();
    }

    // Implementations

    @Override
    public @NotNull String toString() {
        @NotNull StringBuilder builder = new StringBuilder();

        // Protocol ("http://" or "https://")
        if (!getHostName().isEmpty()) {
            for (@NotNull HttpProtocol protocol : HttpProtocol.values()) {
                if (protocol.getPort() == getPort()) {
                    builder.append(protocol.getName());
                }
            }
        }

        // User info
        if (getUserInfo() != null) {
            builder.append(getUserInfo().getUsername()).append(":").append(getUserInfo().getPassword()).append("@");
        }

        // Host
        builder.append(getHostName());

        // Port
        boolean visible = getHostName().isEmpty() || Arrays.stream(HttpProtocol.values()).noneMatch(protocol -> protocol.getPort() == getPort());
        if (visible) {
            builder.append(":").append(getPort());
        }

        return builder.toString();
    }
    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof UriAuthority)) return false;
        UriAuthority authority = (UriAuthority) object;
        return Objects.equals(getUserInfo(), authority.getUserInfo()) && Objects.equals(getAddress(), authority.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserInfo(), getAddress());
    }

}
