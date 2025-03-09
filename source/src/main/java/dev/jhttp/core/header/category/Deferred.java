package dev.jhttp.core.header.category;

import dev.jhttp.core.header.exception.DeferredException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;

/**
 * Represents a deferred value that may not yet be available but can be accessed
 * through its raw string representation.
 *
 * <p>
 * This interface defines the contract for handling deferred data retrieval,
 * where the actual value might not be available immediately. Implementations
 * should provide the logic to check availability and retrieve the data when needed.
 * </p>
 *
 * @param <T> The type of data to be retrieved.
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface Deferred<T> {

    /**
     * Retrieves the stored data if available, otherwise throws a {@link DeferredException}.
     *
     * @return The retrieved data.
     * @throws DeferredException if the deferred data is not available (null).
     */
    @NotNull T retrieve();

    /**
     * Checks if the deferred data is available.
     *
     * @return {@code true} if the data is available, {@code false} otherwise.
     */
    boolean available();

    /**
     * Gets the available value or returns the provided fallback value if not available.
     *
     * @param value The value to return if the deferred data is not available.
     * @return The available deferred value or the provided fallback value.
     */
    @UnknownNullability T orElse(@Nullable T value);

    /**
     * Returns an {@link Optional} containing the deferred value if available, otherwise an empty Optional.
     *
     * @return an Optional containing the deferred value if available
     */
    @NotNull Optional<T> asOptional();

    /**
     * Returns the raw string representation of the deferred data.
     *
     * @return The raw string associated with the deferred data.
     */
    @NotNull String getRaw();
}
