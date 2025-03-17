package dev.jhttp.core.element;

import dev.jhttp.core.header.collection.HttpHeaders;
import dev.jhttp.core.version.HttpVersion;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code HttpElement} interface represents a basic HTTP element that comprises
 * the version, headers, and body.
 * This interface is intended to be implemented by
 * various HTTP components, such as requests and responses, providing a standard way
 * to access their common properties.
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface HttpElement {

    /**
     * Retrieves the version of this HTTP element.
     *
     * @return the {@link HttpVersion} representing the version of this element.
     */
    @NotNull HttpVersion getVersion();

    /**
     * Retrieves the headers of this HTTP element.
     *
     * @return the {@link HttpHeaders} containing the headers of this element.
     */
    @NotNull HttpHeaders getHeaders();

    /**
     * Retrieves the body of this HTTP element.
     *
     * @return the {@link HttpBody} representing the message body of this element.
     */
    @NotNull HttpBody getBody();

}