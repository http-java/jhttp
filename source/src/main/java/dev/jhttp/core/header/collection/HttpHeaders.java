package dev.jhttp.core.header.collection;

import dev.jhttp.core.header.Target;
import dev.jhttp.core.header.HttpHeader;
import dev.jhttp.core.header.factory.HttpHeaderFactory;
import dev.jhttp.core.version.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a collection of HTTP headers with version and target-specific validations.
 *
 * <p>
 * The {@code HttpHeaders} interface extends {@link Collection} and provides a comprehensive API for managing a set of
 * {@link HttpHeader} objects. It is designed to work with a specific {@link HttpVersion} and {@link Target} to enforce
 * protocol-specific rules. For example, if the HTTP version is set to 1.1, headers introduced in later versions (e.g., HTTP/2.0)
 * will not be accepted. Additionally, this interface supports operations tailored for request, response, or both targets.
 * </p>
 *
 * <p>
 * This interface not only allows standard collection operations (such as adding, removing, and iterating over headers)
 * but also provides specialized methods to query headers by name or key, retrieve the first or last occurrence, and count
 * the number of headers matching a given criteria.
 * </p>
 *
 * <h2>Static Initializers</h2>
 * <p>
 * Two static factory methods are provided for creating instances of {@code HttpHeaders}:
 * </p>
 * <ul>
 *   <li>
 *     {@link #create(HttpVersion, HttpHeader...)} - Creates a header collection for both request and response targets.
 *   </li>
 *   <li>
 *     {@link #create(HttpVersion, Target, HttpHeader...)} - Creates a header collection for a specific target (either
 *     {@code Target.REQUEST} or {@code Target.RESPONSE}). The resulting collection is pre-populated with the provided headers.
 *   </li>
 * </ul>
 *
 * <h2>Version and Target Enforcement</h2>
 * <p>
 * Implementations of this interface must enforce that only headers valid for the specified {@link HttpVersion} and target
 * are included. This means that the collection will automatically filter out headers that do not comply with the version-specific
 * rules, ensuring that only appropriate headers are present.
 * </p>
 *
 * <h2>Header Management Operations</h2>
 * <p>
 * The interface offers various methods for header management:
 * </p>
 * <ul>
 *   <li>
 *     <strong>Adding Headers:</strong> Use {@link #put(HttpHeader)} or {@link #add(HttpHeader)} to insert headers into the collection.
 *   </li>
 *   <li>
 *     <strong>Removing Headers:</strong> Headers can be removed by header instance, key, or header name. Overloaded methods like
 *     {@link #remove(HttpHeader)} and {@link #remove(HttpHeader.Key)} delegate to removal by name.
 *   </li>
 *   <li>
 *     <strong>Querying Headers:</strong> Methods such as {@link #get(String)}, {@link #first(String)}, {@link #last(String)},
 *     {@link #contains(String)}, and {@link #count(String)} provide various ways to search for headers based on their names.
 *   </li>
 *   <li>
 *     <strong>Stream Operations:</strong> The {@link #stream()} method returns a sequential {@link Stream} of headers, allowing
 *     for further processing using the Java Streams API.
 *   </li>
 * </ul>
 *
 * @see HttpHeader
 * @see HttpHeaderFactory
 * @see HttpVersion
 * @see Target
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface HttpHeaders extends Collection<HttpHeader<?>> {

    // Static initializers

    /**
     * Creates an {@code HttpHeaders} collection for the specified HTTP version and for both request and response targets.
     *
     * <p>
     * This static factory method delegates to {@link #create(HttpVersion, Target, HttpHeader...)} with {@code Target.BOTH}.
     * The created collection will only accept headers that are valid for the specified {@link HttpVersion}.
     * </p>
     *
     * @param version the HTTP version for which the headers should be validated; must not be null
     * @param headers an array of initial {@link HttpHeader} objects to populate the collection; must not be null
     * @return a non-null {@code HttpHeaders} instance containing the specified headers and valid for both targets
     */
    static @NotNull HttpHeaders create(@NotNull HttpVersion version, @NotNull HttpHeader<?> @NotNull ... headers) {
        return create(version, Target.BOTH, headers);
    }

    /**
     * Creates an {@code HttpHeaders} collection for the specified HTTP version and target.
     *
     * <p>
     * This method obtains a new header collection from the header factory associated with the given HTTP version,
     * tailored for the provided {@link Target} (e.g., {@code Target.REQUEST} or {@code Target.RESPONSE}). The collection
     * is pre-populated with the headers provided in the {@code headers} parameter. The factory enforces that only headers
     * valid for the specified HTTP version and target are included.
     * </p>
     *
     * @param version the HTTP version for which the headers should be validated; must not be null
     * @param target  the target (request, response, or both) for which the header collection is created; must not be null
     * @param headers an array of initial {@link HttpHeader} objects to be added to the collection; must not be null
     * @return a non-null {@code HttpHeaders} instance containing the specified headers and valid for the given target and version
     */
    static @NotNull HttpHeaders create(@NotNull HttpVersion version, @NotNull Target target, @NotNull HttpHeader<?> @NotNull ... headers) {
        @NotNull HttpHeaders a = version.getHeaderFactory().createCollection(target);
        a.addAll(Arrays.asList(headers));
        return a;
    }

    // Object

    /**
     * Retrieves the HTTP version associated with this header collection.
     *
     * <p>
     * The returned {@link HttpVersion} represents the protocol version that governs the validity and behavior
     * of the headers contained in this collection.
     * </p>
     *
     * @return a non-null {@link HttpVersion} that this collection is based on
     */
    @NotNull HttpVersion getVersion();

    /**
     * Retrieves the target for which this header collection is intended.
     *
     * <p>
     * The target indicates whether the headers in this collection are meant for requests, responses, or both.
     * </p>
     *
     * @return a non-null {@link Target} specifying the intended usage of the headers
     */
    @NotNull Target getTarget();

    /**
     * Inserts the specified {@link HttpHeader} into the collection, replacing any existing header with the same name.
     *
     * <p>
     * This method provides a way to add or update a header in the collection. If a header with the same name already exists,
     * it is replaced by the new header.
     * </p>
     *
     * @param header the {@link HttpHeader} to be added or updated; must not be null
     */
    void put(@NotNull HttpHeader<?> header);

    /**
     * Adds the specified {@link HttpHeader} to the collection.
     *
     * <p>
     * If a header with the same name already exists in the collection, the behavior is implementation-specific.
     * This method returns {@code true} if the header was successfully added.
     * </p>
     *
     * @param header the {@link HttpHeader} to add; must not be null
     * @return {@code true} if the header was added successfully, {@code false} otherwise
     */
    boolean add(@NotNull HttpHeader<?> header);

    /**
     * Removes the specified {@link HttpHeader} from the collection by matching its header name.
     *
     * <p>
     * This default method delegates removal to {@link #remove(String)} by obtaining the header name from the provided
     * {@link HttpHeader}.
     * </p>
     *
     * @param header the {@link HttpHeader} to remove; must not be null
     * @return {@code true} if the header was found and removed, {@code false} otherwise
     */
    default boolean remove(@NotNull HttpHeader<?> header) {
        return this.remove(header.getKey().getName());
    }

    /**
     * Removes the header with the specified key from the collection.
     *
     * <p>
     * This default method delegates removal to {@link #remove(String)} by obtaining the header name from the provided
     * {@link HttpHeader.Key}.
     * </p>
     *
     * @param key the key of the header to remove; must not be null
     * @return {@code true} if a header with the given key was found and removed, {@code false} otherwise
     */
    default boolean remove(@NotNull HttpHeader.Key<?> key) {
        return this.remove(key.getName());
    }

    /**
     * Removes the header(s) with the specified name from the collection.
     *
     * <p>
     * The header name matching is case-insensitive. Implementations should remove all headers that match the provided name.
     * </p>
     *
     * @param name the name of the header(s) to remove; must not be null
     * @return {@code true} if one or more headers were removed, {@code false} otherwise
     */
    boolean remove(@NotNull String name);

    /**
     * Retrieves all headers that have a name matching the specified value.
     *
     * <p>
     * The search is case-insensitive, and the method returns an array of {@link HttpHeader} objects that match the provided name.
     * </p>
     *
     * @param name the header name to search for; must not be null
     * @return an array of {@link HttpHeader} objects with names that match the specified name
     */
    default @NotNull HttpHeader<?> @NotNull [] get(@NotNull String name) {
        return stream().filter(header -> header.getName().equalsIgnoreCase(name)).toArray(HttpHeader[]::new);
    }

    /**
     * Checks if a header with the specified key is present in the collection.
     *
     * <p>
     * This default method checks for the existence of a header by delegating to {@link #contains(String)}
     * using the header key's name.
     * </p>
     *
     * @param key the key to check for; must not be null
     * @return {@code true} if at least one header with the given key is present, {@code false} otherwise
     */
    default boolean contains(@NotNull HttpHeader.Key<?> key) {
        return contains(key.getName());
    }

    /**
     * Checks if a header with the specified name exists in the collection.
     *
     * <p>
     * The search is performed in a case-insensitive manner.
     * </p>
     *
     * @param name the name of the header to check for; must not be null
     * @return {@code true} if at least one header with the specified name exists, {@code false} otherwise
     */
    default boolean contains(@NotNull String name) {
        return stream().anyMatch(header -> header.getName().equalsIgnoreCase(name));
    }

    /**
     * Returns a sequential {@link Stream} with this collection as its source.
     *
     * <p>
     * This method provides a convenient way to process the headers using the Java Streams API.
     * </p>
     *
     * @return a non-null {@link Stream} of {@link HttpHeader} objects contained in this collection
     */
    @NotNull Stream<HttpHeader<?>> stream();

    /**
     * Returns the number of headers in the collection.
     *
     * @return the total number of headers
     */
    int size();

    /**
     * Removes all headers from the collection.
     */
    void clear();

    /**
     * Counts the number of headers that match the specified key.
     *
     * <p>
     * The matching is performed in a case-insensitive manner using the key's name.
     * </p>
     *
     * @param key the header key for which to count occurrences; must not be null
     * @return the number of headers matching the specified key
     */
    int count(@NotNull HttpHeader.Key<?> key);

    /**
     * Counts the number of headers with the specified name.
     *
     * <p>
     * The header name matching is case-insensitive.
     * </p>
     *
     * @param name the name of the headers to count; must not be null
     * @return the number of headers that match the specified name
     */
    int count(@NotNull String name);

    /**
     * Retrieves the first header in the collection that matches the specified name.
     *
     * <p>
     * The search is case-insensitive. If no matching header is found, an empty {@link Optional} is returned.
     * </p>
     *
     * @param name the header name to search for; must not be null
     * @return an {@link Optional} containing the first matching header, or empty if none is found
     */
    default @NotNull Optional<HttpHeader<?>> first(@NotNull String name) {
        return stream().filter(header -> header.getName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Retrieves the last header in the collection that matches the specified name.
     *
     * <p>
     * The search is case-insensitive. The method collects all matching headers and returns the last one in the sequence.
     * If no matching header is found, an empty {@link Optional} is returned.
     * </p>
     *
     * @param name the header name to search for; must not be null
     * @return an {@link Optional} containing the last matching header, or empty if none is found
     */
    default @NotNull Optional<HttpHeader<?>> last(@NotNull String name) {
        return stream()
                .filter(header -> header.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList())
                .stream()
                .reduce((first, second) -> second);
    }

    /**
     * Retrieves the first header matching the specified key.
     *
     * <p>
     * This method performs a case-insensitive search based on the key's name. The result is cast to the appropriate generic type.
     * If no matching header is found, an empty {@link Optional} is returned.
     * </p>
     *
     * @param <E> the type of the header value associated with the key
     * @param key the header key to search for; must not be null
     * @return an {@link Optional} containing the first matching header cast to the correct type, or empty if none is found
     * @throws ClassCastException if the found header cannot be cast to the expected type
     */
    default <E> @NotNull Optional<HttpHeader<E>> first(@NotNull HttpHeader.Key<E> key) throws ClassCastException {
        //noinspection unchecked
        return Optional.ofNullable((HttpHeader<E>) first(key.getName()).orElse(null));
    }

    /**
     * Retrieves the last header matching the specified key.
     *
     * <p>
     * This method performs a case-insensitive search based on the key's name. The result is cast to the appropriate generic type.
     * If no matching header is found, an empty {@link Optional} is returned.
     * </p>
     *
     * @param <E> the type of the header value associated with the key
     * @param key the header key to search for; must not be null
     * @return an {@link Optional} containing the last matching header cast to the correct type, or empty if none is found
     */
    default <E> @NotNull Optional<HttpHeader<E>> last(@NotNull HttpHeader.Key<E> key) {
        //noinspection unchecked
        return Optional.ofNullable((HttpHeader<E>) last(key.getName()).orElse(null));
    }

    /**
     * Retrieves all headers matching the specified key.
     *
     * <p>
     * This method collects all headers from the collection whose names match the provided key (case-insensitive)
     * and returns them as an array cast to the appropriate generic type.
     * </p>
     *
     * @param <E> the type of the header value associated with the key
     * @param key the header key used to filter the collection; must not be null
     * @return an array of {@link HttpHeader} objects that match the specified key, cast to the appropriate type
     */
    @SuppressWarnings("unchecked")
    default <E> @NotNull HttpHeader<E> @NotNull [] get(@NotNull HttpHeader.Key<E> key) {
        @NotNull List<HttpHeader<E>> headers = new LinkedList<>();
        for (@NotNull HttpHeader<?> header : get(key.getName())) {
            headers.add((HttpHeader<E>) header);
        }
        return headers.toArray(new HttpHeader[0]);
    }
}