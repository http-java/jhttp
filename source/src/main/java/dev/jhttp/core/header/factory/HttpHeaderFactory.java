package dev.jhttp.core.header.factory;

import dev.jhttp.core.header.Target;
import dev.jhttp.core.header.HttpHeader;
import dev.jhttp.core.header.HttpHeader.Key;
import dev.jhttp.core.header.collection.HttpHeaders;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * A factory for managing HTTP header keys and their associated parsers.
 *
 * <p>
 * The {@code HttpHeaderFactory} interface provides methods for registering, retrieving, and managing
 * HTTP header keys and their corresponding parser implementations. Registered keys represent HTTP header
 * fields that can be processed using custom parsing logic. When parsing a header, if no matching key or parser is found,
 * a default parser is used as a fallback.
 * </p>
 *
 * <p>
 * This interface facilitates extensibility in HTTP header processing, enabling developers to plug in
 * custom parsers for different header types and to manage a registry of header keys.
 * </p>
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface HttpHeaderFactory {

    // Modules

    /**
     * Creates a collection of HTTP headers that are valid for the current HTTP version configuration.
     *
     * <p>
     * This method generates an instance of {@code HttpHeaders} that includes header entries for both request and response targets.
     * It ensures that only headers compliant with the HTTP version of this factory are included. For instance, if the header factory
     * is configured for HTTP/1.1, headers specific to HTTP/2.0 (or later) will be omitted, and various version-specific validations
     * will be performed to ensure compliance with the protocol's standards.
     * </p>
     *
     * @return a non-null {@code HttpHeaders} instance containing headers valid for both requests and responses according to the current HTTP version
     */
    @NotNull HttpHeaders createCollection();

    /**
     * Creates a collection of HTTP headers that are valid for the specified target and the current HTTP version configuration.
     *
     * <p>
     * This method generates an instance of {@code HttpHeaders} tailored for a particular target (such as request or response).
     * It performs version-specific checks and validations, ensuring that headers not applicable to the configured HTTP version are excluded.
     * For example, if the factory is set for HTTP/1.1, any headers that are exclusive to HTTP/2.0 will be rejected or omitted.
     * </p>
     *
     * @param target the target (e.g., {@code Target.REQUEST} or {@code Target.RESPONSE}) for which the header collection should be created
     * @return a non-null {@code HttpHeaders} instance containing headers valid for the specified target and the current HTTP version
     */
    @NotNull HttpHeaders createCollection(@NotNull Target target);

    // Keys

    /**
     * Retrieves a collection of all registered HTTP header keys.
     *
     * <p>
     * The returned collection contains all keys that have been registered with this factory. Each key corresponds
     * to an HTTP header field that may be processed using an associated parser.
     * </p>
     *
     * @return a non-null collection of registered header keys
     */
    @NotNull Collection<Key<?>> getKeys();

    /**
     * Searches for a registered HTTP header key by its name.
     *
     * <p>
     * This method attempts to locate a header key matching the provided string. If the key is not found in the registry,
     * an empty {@link Optional} is returned.
     * </p>
     *
     * @param key the name of the HTTP header key to search for
     * @return an {@link Optional} containing the header key if found, or empty if not found
     */
    @NotNull Optional<Key<?>> getKey(@NotNull String key);

    /**
     * Registers a specific HTTP header key along with its associated parser.
     *
     * <p>
     * It is important to register all header keys so that the request/response parser can retrieve the correct parser
     * based on the key's generic type. If a valid key instance is not found during parsing, the default parser will be used.
     * </p>
     *
     * @param <T>    the type of the header value associated with the key
     * @param key    the HTTP header key to register
     * @param parser the parser that can parse and serialize headers corresponding to the key
     */
    <T> void setKey(@NotNull Key<T> key, @NotNull HttpHeaderParser<T> parser);

    // Default parsers

    /**
     * Retrieves the default HTTP header parser.
     *
     * <p>
     * The default parser is used as a fallback when no specific parser is found for a given header key.
     * </p>
     *
     * @return a non-null instance of the default HTTP header parser
     */
    @NotNull HttpHeaderParser<?> getDefaultParser();

    /**
     * Sets the default HTTP header parser.
     *
     * <p>
     * The default parser is used when no matching parser is registered for a header key during the parsing process.
     * </p>
     *
     * @param parser the HTTP header parser to use as the default fallback parser
     */
    void setDefaultParser(@NotNull HttpHeaderParser<?> parser);

    // Parsers

    /**
     * Retrieves the parser associated with the specified HTTP header key.
     *
     * <p>
     * This method returns an {@link Optional} containing the parser registered for the given header key.
     * If no parser has been registered for the key, an empty {@link Optional} is returned.
     * </p>
     *
     * @param <T> the type of the header value associated with the key
     * @param key the HTTP header key whose parser is to be retrieved
     * @return an {@link Optional} containing the associated HTTP header parser, or empty if none is registered
     */
    <T> @NotNull Optional<HttpHeaderParser<T>> getParser(@NotNull Key<T> key);

    /**
     * Associates a specific HTTP header parser with the provided header key.
     *
     * <p>
     * This method allows customization of the parsing logic for a particular header type by registering
     * a parser that handles conversion between the raw header string and a typed {@link HttpHeader} object.
     * </p>
     *
     * @param <T>    the type of the header value associated with the key
     * @param key    the HTTP header key for which the parser is to be set
     * @param parser the HTTP header parser to associate with the key
     */
    <T> void setParser(@NotNull Key<T> key, @NotNull HttpHeaderParser<T> parser);
}

