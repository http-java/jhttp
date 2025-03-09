package dev.jhttp.core.header.category;

import dev.jhttp.core.header.exception.WildcardValueException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

/**
 * The {@code Wildcard} interface represents a value that can either be a wildcard
 * or a specific value.
 * Wildcards are typically used in contexts where a value can
 * be unspecified or match any value, such as in pattern matching or search filters.
 *
 * @param <T> the type of the value that can be held by this wildcard
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface Wildcard<T> {

    // Static initializers

    /**
     * Creates a new wildcard instance representing a wildcard value.
     *
     * @param <E> the type of the value
     * @return a new {@code Wildcard} instance representing a wildcard
     */
    static <E> @NotNull Wildcard<E> create() {
        return new Wildcard<E>() {
            @Override
            public boolean isWildcard() {
                return true;
            }
            @Override
            public @NotNull E getValue() throws WildcardValueException {
                throw new WildcardValueException("this is a wildcard (*) and doesn't have a value!");
            }

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (!(object instanceof Wildcard<?>)) return false;
                Wildcard<?> that = (Wildcard<?>) object;
                return that.isWildcard();
            }
            @Override
            public int hashCode() {
                return Objects.hash(isWildcard());
            }

            @Override
            public @NotNull String toString() {
                return "*";
            }
        };
    }

    /**
     * Creates a new wildcard instance with a specific value.
     *
     * @param <E> the type of the value
     * @param value the specific value
     * @return a new {@code Wildcard} instance with the specified value
     */
    static <E> @NotNull Wildcard<E> create(@UnknownNullability E value) {
        return new Wildcard<E>() {
            @Override
            public boolean isWildcard() {
                return false;
            }
            @Override
            public @UnknownNullability E getValue() throws WildcardValueException {
                return value;
            }

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (!(object instanceof Wildcard<?>)) return false;
                Wildcard<?> that = (Wildcard<?>) object;
                return !that.isWildcard() && Objects.equals(getValue(), that.getValue());
            }
            @Override
            public int hashCode() {
                return Objects.hash(isWildcard(), getValue());
            }

            @Override
            public @NotNull String toString() {
                return String.valueOf(getValue());
            }
        };
    }

    // Object

    /**
     * Checks if this instance is a wildcard.
     *
     * @return {@code true} if this is a wildcard, {@code false} otherwise
     */
    boolean isWildcard();

    /**
     * Retrieves the value associated with this wildcard.
     *
     * @return the value associated with this wildcard
     * @throws WildcardValueException if this instance is a wildcard and does not have a value
     */
    @UnknownNullability T getValue() throws WildcardValueException;

}
