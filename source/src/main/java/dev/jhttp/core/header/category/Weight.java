package dev.jhttp.core.header.category;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

/**
 * The {@code Weight} interface represents a weighted element, typically used in HTTP headers
 * where a weight factor "q=1.0;" is present.
 * The weight factor is used to indicate the
 * relative preference or importance of the element.
 * Some values may include an optional
 * weight factor, in which case {@link #getWeight()} may return null.
 *
 * @param <T> the type of the value being weighted
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface Weight<T> {

    // Static initializers

    /**
     * Creates a new {@code Weight} instance with the specified weight and value.
     *
     * @param <E>    the type of the value
     * @param weight the weight factor, or {@code null} if no weight is specified
     * @param value  the value associated with this weight
     * @return a new {@code Weight} instance
     */
    static <E> @NotNull Weight<E> create(@Nullable Float weight, @UnknownNullability E value) {
        return new Weight<E>() {
            @Override
            public @Nullable Float getWeight() {
                return weight;
            }
            @Override
            public @UnknownNullability E getValue() {
                return value;
            }

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (!(object instanceof Weight<?>)) return false;
                Weight<?> that = (Weight<?>) object;
                return Objects.equals(getWeight(), that.getWeight()) && Objects.equals(getValue(), that.getValue());
            }
            @Override
            public int hashCode() {
                return Objects.hash(weight, value);
            }

            @Override
            public @NotNull String toString() {
                return value + (weight != null ? ";q=" + weight : "");
            }
        };
    }

    // Object

    /**
     * Retrieves the weight factor of this element.
     *
     * @return the weight factor, or {@code null} if no weight is specified
     */
    @Nullable Float getWeight();

    /**
     * Retrieves the value associated with this weight.
     *
     * @return the value associated with this weight
     */
    @UnknownNullability T getValue();

}
