package dev.jhttp.core.url;

import codes.laivy.address.domain.SLD;
import codes.laivy.address.domain.Subdomain;
import codes.laivy.address.domain.TLD;
import codes.laivy.address.port.Port;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class Host {

    // Static initializers

    public static boolean validate(@NotNull String string) {
        return Name.validate(string) || IPv4.validate(string) || IPv6.validate(string);
    }
    public static @NotNull Host parse(@NotNull String string) throws IllegalArgumentException {
        if (Name.validate(string)) {
            return Name.parse(string);
        } else if (IPv4.validate(string)) {
            return IPv4.parse(string);
        } else if (IPv6.validate(string)) {
            return IPv6.parse(string);
        } else {
            throw new IllegalArgumentException("the value '" + string + "' isn't a valid host");
        }
    }

    // Object

    private final @Nullable Port port;

    protected Host(@Nullable Port port) {
        this.port = port;
    }

    // Getters

    public final @Nullable Port getPort() {
        return this.port;
    }

    public abstract @NotNull String getName();

    // Modules

    public final boolean isIPv4() {
        return IPv4.validate(toString());
    }
    public final boolean isIPv6() {
        return IPv6.validate(toString());
    }

    // Implementations

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof Host)) return false;
        @NotNull Host host = (Host) object;
        return Objects.equals(getPort(), host.getPort());
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(getPort());
    }

    @Override
    public abstract @NotNull String toString();

    // Classes

    public static final class Name extends Host {

        // Static initializers

        public static boolean validate(@NotNull String string) {
            @NotNull String[] parts = string.split(":");

            if (parts.length > 2 || parts.length == 0) {
                return false;
            } else try {
                if (parts.length == 2 && !Port.validate(parts[1])) {
                    return false;
                }

                parts = parts[0].split("\\.");

                if (parts.length == 0) {
                    return false;
                } else if (parts[parts.length - 1].equalsIgnoreCase("localhost")) {
                    if (parts.length > 1 && Arrays.stream(Arrays.copyOfRange(parts, 0, parts.length - 1)).anyMatch(subdomain -> !Subdomain.validate(subdomain))) {
                        return false;
                    }
                } else {
                    if (!TLD.validate(parts[parts.length - 1])) {
                        return false;
                    } else if (!SLD.validate(parts[parts.length - 2])) {
                        return false;
                    } else if (parts.length > 2 && Arrays.stream(Arrays.copyOfRange(parts, 0, parts.length - 2)).anyMatch(subdomain -> !Subdomain.validate(subdomain))) {
                        return false;
                    }
                }
            } catch (@NotNull NumberFormatException ignore) {
                return false;
            }

            return true;
        }
        public static @NotNull Name parse(@NotNull String string) throws IllegalArgumentException {
            @NotNull String[] parts = string.split(":");

            if (validate(string)) {
                @Nullable Port port = parts.length == 2 ? Port.parse(parts[1]) : null;
                parts = parts[0].split("\\.");

                @Nullable TLD tld;
                @NotNull SLD sld;
                @NotNull Subdomain[] subdomains;

                if (parts[parts.length - 1].equalsIgnoreCase("localhost")) {
                    tld = null;

                    sld = SLD.parse(parts[parts.length - 1]);
                    subdomains = parts.length > 1 ? Arrays.stream(Arrays.copyOfRange(parts, 0, parts.length - 1)).map(Subdomain::create).toArray(Subdomain[]::new) : new Subdomain[0];
                } else {
                    tld = TLD.parse(parts[parts.length - 1]);
                    sld = SLD.parse(parts[parts.length - 2]);
                    subdomains = parts.length > 2 ? Arrays.stream(Arrays.copyOfRange(parts, 0, parts.length - 2)).map(Subdomain::create).toArray(Subdomain[]::new) : new Subdomain[0];

                }

                return new Name(subdomains, sld, tld, port);
            } else {
                throw new IllegalArgumentException("cannot parse '" + string + "' as a valid name address");
            }
        }

        public static @NotNull Name create(@NotNull Subdomain @NotNull [] subdomains, @NotNull SLD sld, @Nullable TLD tld, @Nullable Port port) {
            return new Name(subdomains, sld, tld, port);
        }

        // Object

        private final @NotNull Subdomain @NotNull [] subdomains;
        private final @NotNull SLD sld;
        private final @Nullable TLD tld;

        private Name(@NotNull Subdomain @NotNull [] subdomains, @NotNull SLD sld, @Nullable TLD tld, @Nullable Port port) {
            super(port);

            this.subdomains = subdomains;
            this.sld = sld;
            this.tld = tld;
        }

        // Getters

        public boolean isLocalhost() {
            return getTLD() == null && getSLD().equalsIgnoreCase("localhost");
        }

        public @NotNull Subdomain @NotNull [] getSubdomains() {
            return subdomains;
        }
        public @NotNull SLD getSLD() {
            return sld;
        }

        /**
         * The TLD name may be null if the SLD is localhost.
         *
         * @return The host name TLD (maybe null)
         */
        public @Nullable TLD getTLD() {
            return tld;
        }

        @Override
        public @NotNull String getName() {
            @NotNull StringBuilder builder = new StringBuilder();

            for (@NotNull Subdomain subdomain : getSubdomains()) {
                builder.append(subdomain).append(".");
            }

            builder.append(getSLD());

            if (getTLD() != null) {
                builder.append(".").append(getTLD());
            }

            return builder.toString();
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (!(object instanceof Name)) return false;
            if (!super.equals(object)) return false;
            @NotNull Name name = (Name) object;
            return Objects.deepEquals(getSubdomains(), name.getSubdomains()) && Objects.equals(getSLD(), name.getSLD()) && Objects.equals(getTLD(), name.getTLD());
        }
        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), Arrays.hashCode(getSubdomains()), getSLD(), getTLD());
        }

        @Override
        public @NotNull String toString() {
            @NotNull StringBuilder builder = new StringBuilder(getName());

            if (getPort() != null) {
                builder.append(":").append(getPort());
            }

            return builder.toString();
        }

    }
    public static final class IPv4 extends Host {

        private static final @NotNull Pattern IPV4_PATTERN = Pattern.compile("^(?<bytes>[^]:]{2,39})(:(?<port>\\d{1,5}))?$");

        public static boolean validate(@NotNull String string) {

            if (string.length() > 21) {
                return false;
            } else {
                @NotNull String[] parts = string.split(":");

                if (parts.length > 2 || parts.length == 0) {
                    return false;
                } else if (parts.length == 2 && !Port.validate(parts[1])) {
                    return false;
                }

                string = parts[0];
            }

            @NotNull String[] parts = string.split("\\.");

            if (parts.length != 4) {
                return false;
            } else for (@NotNull String part : parts) try {
                int octet = Integer.parseInt(part);
                if (octet < 0 || octet > 255) return false;
            } catch (@NotNull NumberFormatException ignore) {
                return false;
            }

            return true;
        }
        public static @NotNull IPv4 parse(@NotNull String string) throws IllegalArgumentException {
            if (validate(string)) {
                @NotNull String[] parts = string.split(":");

                @NotNull String name = parts[0];
                @Nullable Port port = parts.length == 2 ? Port.parse(parts[1]) : null;

                parts = name.split("\\.");
                int[] octets = new int[4];

                for (int index = 0; index < 4; index++) {
                    octets[index] = Integer.parseInt(parts[index]);
                }

                return new IPv4(octets, port);
            } else {
                throw new IllegalArgumentException("cannot parse '" + string + "' as a valid ipv4 address");
            }
        }

        // Object

        private final int[] octets;

        private IPv4(int @NotNull [] octets, @Nullable Port port) {
            super(port);
            this.octets = octets;

            // Verifications
            if (octets.length != 4) {
                throw new IllegalArgumentException("an ipv4 address must have four octets");
            } else for (int octet : octets) {
                if (octet < 0 || octet > 255) {
                    throw new IllegalArgumentException("invalid octect '" + octet + "'");
                }
            }
        }

        // Getters

        public int[] getOctets() {
            return octets;
        }

        @Override
        public @NotNull String getName() {
            return getOctets()[0] + "." + getOctets()[1] + "." + getOctets()[2] + "." + getOctets()[3];
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (!(object instanceof IPv4)) return false;
            if (!super.equals(object)) return false;
            @NotNull IPv4 iPv4 = (IPv4) object;
            return Objects.deepEquals(getOctets(), iPv4.getOctets());
        }
        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), Arrays.hashCode(getOctets()));
        }

        @Override
        public @NotNull String toString() {
            @NotNull StringBuilder builder = new StringBuilder(getName());

            if (getPort() != null) {
                builder.append(":").append(getPort());
            }

            return builder.toString();
        }

    }
    public static final class IPv6 extends Host {

        // Static initializers

        public static boolean validate(@NotNull String string) {
            if (string.startsWith("[")) {
                string = string.substring(1);
            }

            // Parse port
            @NotNull String[] parts = string.split("]");
            if (parts.length > 2 || parts.length == 0) {
                return false;
            } else if (parts.length == 2) {
                if (!parts[1].startsWith(":")) {
                    return false;
                } else if (!Port.validate(parts[1].substring(1))) {
                    return false;
                }
            }

            string = parts[0];

            // Add the missing zero groups to the string to start address verification
            int missing = 7 - string.replace("::", "").split(":").length;

            @NotNull StringBuilder zeros = new StringBuilder();
            for (int row = 0; row < missing; row++) {
                zeros.append("0:");
            }
            string = string.replace("::", ":" + zeros);

            // Basic verifications
            parts = string.split(":");

            if (parts.length != 8) {
                return false;
            } else for (@NotNull String hex : parts) {
                try {
                    Integer.parseInt(hex, 16);
                } catch (@NotNull NumberFormatException ignore) {
                    return false;
                }
            }

            return true;
        }
        public static @NotNull IPv6 parse(@NotNull String string) throws IllegalArgumentException {
            if (validate(string)) {
                if (string.startsWith("[")) {
                    string = string.substring(1);
                }

                // Get port
                @Nullable Port port = null;

                // Parse port
                @NotNull String[] parts = string.split("]");
                if (parts.length == 2) {
                    port = Port.parse(parts[1].substring(1));
                }

                string = parts[0];

                // Parse groups
                short[] groups = new short[8];

                // Add the missing zero groups to the string
                int missing = 7 - string.replace("::", "").split(":").length;

                @NotNull StringBuilder zeros = new StringBuilder();
                for (int row = 0; row < missing; row++) {
                    zeros.append("0:");
                }
                string = string.replace("::", ":" + zeros);

                // Read all the groups one by one
                @NotNull String[] groupParts = string.split(":");
                for (int index = 0; index < groupParts.length; index++) {
                    @NotNull String hex = groupParts[index].replaceFirst("^0+(?!$)", "");
                    groups[index] = (short) Integer.parseInt(hex, 16);
                }

                // Finish
                return new IPv6(groups, port);
            } else {
                throw new IllegalArgumentException("cannot parse '" + string + "' as a valid ipv6 address");
            }
        }

        // Object

        // This array will always have 8 elements
        private final short[] groups;

        private IPv6(short[] groups, @Nullable Port port) {
            super(port);
            this.groups = groups;

            if (groups.length != 8) {
                throw new IllegalArgumentException("An IPv6 address must have eight hexadecimal groups");
            }
        }

        // Getters

        public short[] getGroups() {
            return groups;
        }

        @Override
        public @NotNull String getName() {
            // Functions
            @NotNull Function<Short, String> function = new Function<Short, String>() {
                @Override
                public @NotNull String apply(@NotNull Short group) {
                    @NotNull String hex = String.format("%04X", group);
                    while (hex.startsWith("0")) hex = hex.substring(1);

                    return hex;
                }
            };

            // Build string
            @NotNull StringBuilder builder = new StringBuilder();

            for (int index = 0; index < 8; index++) {
                short group = getGroups()[index];

                // Generate representation
                @NotNull String representation = function.apply(group);
                if (representation.equals("0000")) representation = "0";

                // Zero abbreviation check
                if (group == 0 && index != 7 && getGroups()[index + 1] == 0) {
                    continue;
                }

                // Add the ":"
                if (builder.length() > 0) builder.append(":");
                // Add representation
                builder.append(representation);
            }

            // Replace zeros

            return "[" + builder + "]";
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (!(object instanceof IPv6)) return false;
            if (!super.equals(object)) return false;
            @NotNull IPv6 iPv6 = (IPv6) object;
            return Objects.deepEquals(getGroups(), iPv6.getGroups());
        }
        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), Arrays.hashCode(getGroups()));
        }

        @Override
        public @NotNull String toString() {
            @NotNull StringBuilder builder = new StringBuilder(getName());
            if (getPort() != null) builder.append(":").append(getPort());

            return builder.toString();
        }

    }

}
