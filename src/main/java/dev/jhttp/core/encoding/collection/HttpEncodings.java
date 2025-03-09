package dev.jhttp.core.encoding.collection;

import dev.jhttp.core.encoding.HttpEncoding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents a collection of HTTP encodings available for a specific HTTP version.
 *
 * <p>
 * The {@code HttpEncodings} interface extends {@link Collection} to manage a set of {@link HttpEncoding} objects.
 * Each HTTP version has its own set of supported encodings, and this collection ensures that only the appropriate
 * encodings for a given version are present. The interface provides methods for retrieving, adding, removing, and
 * updating encodings by their canonical names or any of their aliases, ensuring uniqueness and consistency.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>
 *     <strong>Retrieval:</strong> Use {@link #get(String)} to search for an encoding by its name or any alias.
 *   </li>
 *   <li>
 *     <strong>Removal:</strong> The {@link #remove(String)} method removes an encoding based on its name or alias.
 *   </li>
 *   <li>
 *     <strong>Addition:</strong> The {@link #add(HttpEncoding)} method adds a new encoding to the collection,
 *     but will fail if an encoding with the same name or any matching alias already exists.
 *   </li>
 *   <li>
 *     <strong>Replacement:</strong> The {@link #set(HttpEncoding)} method updates the encoding corresponding to
 *     a given name or alias, returning the previous encoding if one existed.
 *   </li>
 * </ul>
 *
 * <h2>Usage Considerations</h2>
 * <p>
 * Implementations of this interface should enforce that each encoding is uniquely identifiable by its canonical name
 * and aliases. This is critical for ensuring that the correct encoding mechanisms are applied during HTTP message
 * processing, especially during content negotiation and data transformation.
 * </p>
 *
 * @see HttpEncoding
 */
public interface HttpEncodings extends Collection<HttpEncoding> {

    /**
     * Retrieves the HTTP encoding associated with the given name or alias.
     *
     * <p>
     * This method searches the collection for an encoding whose canonical name or any of its aliases matches the provided
     * {@code name} (case-insensitive). If an encoding is found, it is returned wrapped in an {@link Optional}; otherwise,
     * {@link Optional#empty()} is returned.
     * </p>
     *
     * @param name the name or alias of the encoding to retrieve; must not be null
     * @return an {@link Optional} containing the matching {@link HttpEncoding}, or empty if no match is found
     */
    @NotNull Optional<HttpEncoding> get(@NotNull String name);

    /**
     * Sets or replaces an HTTP encoding in the collection.
     *
     * <p>
     * This method updates the collection by setting the provided {@code encoding}. If an encoding with the same canonical
     * name or any alias already exists, it is replaced with the new encoding, and the previous encoding is returned.
     * If no matching encoding exists, the new encoding is added and {@code null} is returned.
     * </p>
     *
     * @param encoding the {@link HttpEncoding} to set; must not be null
     * @return the previous {@link HttpEncoding} that was replaced, or {@code null} if no matching encoding existed
     */
    @Nullable HttpEncoding set(@NotNull HttpEncoding encoding);

    /**
     * Removes an HTTP encoding from the collection based on its canonical name or any alias.
     *
     * <p>
     * The removal is performed by matching the provided {@code name} against the canonical name and all aliases of the
     * encodings in the collection. If a matching encoding is found, it is removed and the method returns {@code true};
     * if no match is found, {@code false} is returned.
     * </p>
     *
     * @param name the name or alias of the encoding to remove; must not be null
     * @return {@code true} if an encoding was found and removed, {@code false} otherwise
     */
    boolean remove(@NotNull String name);

    /**
     * Adds a new HTTP encoding to the collection.
     *
     * <p>
     * This method attempts to add the specified {@code encoding} to the collection. It will return {@code false} if there
     * is already an encoding in the collection with the same canonical name or any matching alias, ensuring that each
     * encoding remains unique.
     * </p>
     *
     * @param encoding the {@link HttpEncoding} to add; must not be null
     * @return {@code true} if the encoding was successfully added, or {@code false} if an encoding with the same name or alias already exists
     */
    boolean add(@NotNull HttpEncoding encoding);

}