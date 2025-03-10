package dev.jhttp.core.body.content;

import dev.jhttp.core.body.HttpBody;
import dev.jhttp.core.version.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;

/**
 * A content object is directly linked to an {@link HttpBody}. When an {@link HttpBody} uses the method
 * {@link HttpBody#getContent(HttpVersion, ContentType)}, it creates an instance of this class.
 * <p>
 * When this class undergoes a {@link Flushable#flush()}, the new data of this class is written to the
 * {@link HttpBody#getInputStream()}. When a content is created, the data from {@link #getData()} is deserialized by
 * {@link ContentParser#deserialize(HttpVersion, InputStream, ContentType.Parameter...)} and is already loaded into memory.
 * </p>
 *
 * @param <T> the type of the content data
 */
public interface Content<T> extends Flushable {

    // Static initializers

    /**
     * Gets the media type (MIME type) of this content.
     *
     * @return the media type of this content, never null
     */
    @NotNull ContentType<T> getContentType();

    /**
     * Gets the media parser of this content
     *
     * @return the media parser of this content, never null
     */
    default @NotNull ContentParser<T> getContentParser() {
        return getContentType().getParser();
    }

    /**
     * Gets the {@link HttpBody} associated with this content.
     *
     * @return the associated HTTP body, never null
     */
    @NotNull HttpBody getBody();

    /**
     * Gets the http version this content is from
     *
     * @return the http version
     */
    @NotNull HttpVersion getVersion();

    /**
     * Obtains the data of this content.
     *
     * @return the data of this content, never null
     */
    @NotNull T getData();

    /**
     * Sets the data for this content. If autoFlush is true, the new data is immediately written to the associated
     * {@link HttpBody}.
     *
     * @param data the new data to set, must not be null
     * @param autoFlush whether to automatically flush the data to the HTTP body
     * @throws IOException if an IO exception happens when flush
     */
    void setData(@NotNull T data, boolean autoFlush) throws IOException;

    /**
     * Sets the data for this content and automatically flushes it to the associated {@link HttpBody}.
     *
     * @param data the new data to set, must not be null
     * @throws IOException if an IO exception happens when flush
     */
    default void setData(@NotNull T data) throws IOException {
        setData(data, true);
    }

    /**
     * Flushes the current data and loads it into the assigned {@link HttpBody}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    void flush() throws IOException;

}