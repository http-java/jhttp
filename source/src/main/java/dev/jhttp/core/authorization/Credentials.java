package dev.jhttp.core.authorization;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Flushable;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;

public interface Credentials extends CharSequence, Flushable {

    byte @NotNull [] getBytes();

    @Override
    default int length() {
        return getBytes().length;
    }

    @Override
    default char charAt(int index) {
        return (char) getBytes()[index];
    }

    @Override
    default @NotNull CharSequence subSequence(int start, int end) {
        return new String(getBytes()).subSequence(start, end);
    }

    // Classes

    final class Unknown implements Credentials {

        // Static initializers

        public static @NotNull Unknown create(char[] chars) {
            return new Unknown(chars);
        }
        public static @NotNull Unknown create(@NotNull String credentials) {
            return new Unknown(credentials.toCharArray());
        }

        // Object

        private final char[] chars;
        private volatile boolean flushed = false;

        private Unknown(char[] chars) {
            this.chars = chars;
        }

        // Getters

        private char[] bytes() {
            if (flushed) {
                throw new IllegalStateException("this credential has been flushed and cannot be used anymore");
            }
            return chars;
        }

        @Override
        public byte @NotNull [] getBytes() {
            return toString().getBytes();
        }

        @Override
        public synchronized void flush() {
            Arrays.fill(bytes(), (char) 0);
            flushed = true;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Unknown that = (Unknown) object;
            return Objects.deepEquals(bytes(), that.bytes());
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes());
        }

        @Override
        public @NotNull String toString() {
            return new String(bytes());
        }

    }
    class Bearer implements Credentials {

        // Object

        private final char[] token;
        private volatile boolean flushed = false;

        public Bearer(char @NotNull [] token) {
            this.token = token;
        }
        public Bearer(@NotNull String token) {
            this.token = token.toCharArray();
        }

        // Getters

        private char[] token() {
            if (flushed) {
                throw new IllegalStateException("this credential has been flushed and cannot be used anymore");
            }
            return token;
        }

        @Override
        public byte @NotNull [] getBytes() {
            return toString().getBytes();
        }

        @Override
        public synchronized void flush() {
            Arrays.fill(token(), (char) 0);
            flushed = true;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Bearer bearer = (Bearer) object;
            return Objects.deepEquals(token(), bearer.token());
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(token());
        }

        @Override
        public @NotNull String toString() {
            return "Bearer " + new String(token());
        }

    }
    class Basic implements Credentials {

        // Static initializers

        public static @NotNull Basic parse(@NotNull String basic) throws ParseException {
            int lastIndex = basic.lastIndexOf(":");

            if (lastIndex == -1) {
                throw new ParseException("basic authorization missing ':' separator", 0);
            }

            @NotNull String prefix = basic.substring(0, lastIndex);
            @NotNull String suffix = basic.substring(lastIndex + 1);

            return new Basic(prefix, suffix.toCharArray());
        }

        // Object

        private final @NotNull String username;
        private final char @NotNull [] password;
        private volatile boolean flushed = false;

        public Basic(@NotNull String username, char @NotNull [] password) {
            this.username = username;
            this.password = password;
        }
        public Basic(@NotNull String username, @NotNull String password) {
            this.username = username;
            this.password = password.toCharArray();
        }

        // Getters

        private char[] password() {
            if (flushed) {
                throw new IllegalStateException("this credential has been flushed and cannot be used anymore");
            }
            return password;
        }

        @Contract(pure = true)
        public final @NotNull String getUsername() {
            return this.username;
        }

        public final char[] getPassword() {
            return this.password();
        }

        // Implementations

        @Override
        public synchronized void flush() {
            Arrays.fill(password(), (char) 0);
            flushed = true;
        }

        @Override
        public byte @NotNull [] getBytes() {
            return ("Basic " + getUsername() + ":" + new String(getPassword())).getBytes();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Basic)) return false;
            Basic basic = (Basic) object;
            return Objects.equals(getUsername(), basic.getUsername()) && Arrays.equals(getPassword(), basic.getPassword());
        }
        @Override
        public int hashCode() {
            return Objects.hash(getUsername(), Arrays.hashCode(getPassword()));
        }

        @Override
        public @NotNull String toString() {
            return new String(getBytes(), StandardCharsets.UTF_8);
        }

    }

}
