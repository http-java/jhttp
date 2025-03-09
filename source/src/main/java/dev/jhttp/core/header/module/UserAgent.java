package dev.jhttp.core.header.module;

import codes.laivy.jhttp.utilities.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserAgent {

    // Static initializers

    public static @NotNull UserAgent parse(@NotNull String string) {
        @NotNull List<Product> products = new LinkedList<>();

        for (@NotNull String part : string.split("\\s(?!\\([^)]*\\)|[^()]*\\))")) {
            products.add(Product.parse(part));
        }

        // Finish
        return new UserAgent(products.toArray(new Product[0]));
    }

    // Object

    private final @NotNull Product @NotNull [] products;

    // Constructors

    public UserAgent(@NotNull Product @NotNull [] products) {
        this.products = products;
    }

    // Getters

    public @NotNull Product @NotNull [] getProducts() {
        return products;
    }

    // Implementations

    @Override
    public final boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof UserAgent)) return false;
        @NotNull UserAgent agent = (UserAgent) object;
        return Objects.deepEquals(getProducts(), agent.getProducts());
    }
    @Override
    public final int hashCode() {
        return Arrays.hashCode(getProducts());
    }

    @Override
    public final @NotNull String toString() {
        @NotNull StringBuilder builder = new StringBuilder();

        for (@NotNull Product product : getProducts()) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(product);
        }

        return builder.toString();
    }

    // Classes

    public static final class Product {

        // Static initializers

        public static @NotNull Product parse(@NotNull String string) {
            if (string.length() > 1024) {
                throw new IllegalArgumentException("product too long!");
            } else if (string.isEmpty()) {
                throw new IllegalArgumentException("empty product");
            }

            // Split parts
            @NotNull String[] parts = string.split("\\(", 2);

            // Name and version
            @NotNull String[] nameAndVersion = parts[0].trim().split("/");

            if (nameAndVersion.length > 2) {
                throw new IllegalArgumentException("illegal product name and version '" + parts[0] + "'");
            }

            @NotNull String name = nameAndVersion[0];
            @Nullable String version = nameAndVersion.length == 2 ? nameAndVersion[1] : null;

            // Comments
            @NotNull String[] comments = new String[0];

            if (parts.length == 2) {
                parts = ("(" + parts[1]).split("\\s+(?![^()]*\\))");
                comments = new String[parts.length];

                for (int index = 0; index < parts.length; index++) {
                    @NotNull String comment = parts[index];

                    if (!comment.startsWith("(") || !comment.endsWith(")")) {
                        throw new IllegalArgumentException("cannot parse product comment '" + comment + "'");
                    }

                    comments[index] = comment.substring(1, comment.length() - 1);
                }
            }

            // Finish
            return new Product(name, version, comments);
        }

        public static @NotNull Product create(
                @NotNull String name,
                @Nullable String version,

                @NotNull String @NotNull ... comments
        ) {
            return new Product(name, version, comments);
        }

        // Object

        private final @NotNull String name;
        private final @Nullable String version;

        private final @NotNull String @NotNull [] comments;

        private Product(@NotNull String name, @Nullable String version, @NotNull String @NotNull [] comments) {
            this.name = name;
            this.version = version;
            this.comments = comments;

            // Validate name and version
            if ((name.contains("\\") || name.contains("/")) || (version != null && (version.contains("\\") || version.contains("/")))) {
                throw new IllegalArgumentException("illegal product/version names");
            }

            // Validate comments
            for (@NotNull String comment : getComments()) {
                if (comment.contains("(") || comment.contains(")")) {
                    throw new IllegalArgumentException("comment '" + comment + "' with illegal characters");
                }
            }
        }

        // Getters

        public @NotNull String getName() {
            return this.name;
        }
        public @Nullable String getVersion() {
            return this.version;
        }

        public @NotNull String @NotNull [] getComments() {
            return comments;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            @NotNull Product product = (Product) object;
            return Objects.equals(getName(), product.getName()) && Objects.equals(getVersion(), product.getVersion());
        }
        @Override
        public int hashCode() {
            return Objects.hash(getName(), getVersion());
        }

        @Override
        public @NotNull String toString() {
            @NotNull StringBuilder builder = new StringBuilder();
            builder.append(getName()).append(getVersion() != null ? "/" + getVersion() : "");

            for (@NotNull String comment : getComments()) {
                if (StringUtils.isBlank(comment)) {
                    continue;
                }

                builder.append(" (").append(comment).append(")");
            }

            return builder.toString();
        }

    }

}
