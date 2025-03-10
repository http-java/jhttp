package dev.jhttp.core.body;

import dev.jhttp.core.body.content.Content;
import dev.jhttp.core.body.content.ContentType;
import dev.jhttp.core.encoding.exception.EncodingException;
import dev.jhttp.core.header.collection.HttpHeaders;
import dev.jhttp.core.header.exception.DeferredException;
import dev.jhttp.core.version.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the body of an HTTP request or response. The raw content is represented as a {@link CharSequence},
 * which reflects the request in its original form, excluding any encodings (such as those specified by the
 * "Content-Encoding" header) or transformations.
 * <p>
 * This interface is designed to handle HTTP bodies and provides methods to retrieve decoded and transformed content
 * when possible. It allows for the creation of an {@link HttpBody} instance from a given {@link Content} object,
 * and offers methods to access the content in its various forms.
 * </p>
 *
 * @see CharSequence
 * @see Content
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface HttpBody extends Closeable {

    // Static initializers

    static @NotNull HttpBody empty() {
        try {
            return create(new byte[0]);
        } catch (@NotNull IOException e) {
            throw new RuntimeException(e);
        }
    }

    static @NotNull HttpBody create(@NotNull Chunk @NotNull ... chunks) throws IOException {
        return new HttpChunkedBody(chunks);
    }
    static @NotNull HttpBody create(byte @NotNull [] bytes) throws IOException {
        if (bytes.length >= HttpBigBody.MIN_BIG_BODY_SIZE.getBytes()) {
            return new HttpBigBody(bytes);
        } else {
            return new HttpSimpleBody(bytes);
        }
    }
    static @NotNull HttpBody create(@NotNull InputStream stream) throws IOException {
        if (stream.available() >= HttpBigBody.MIN_BIG_BODY_SIZE.getBytes()) {
            return new HttpBigBody(stream);
        } else {
            return new HttpSimpleBody(stream);
        }
    }
    static <T> @NotNull Content<T> create(@NotNull HttpVersion version, @NotNull ContentType<T> ContentType, @NotNull T data) {
        try (@NotNull InputStream stream = ContentType.getParser().serialize(version, data, ContentType.getParameters())) {
            int available = stream.available();

            @NotNull HttpBody body;
            if (available >= HttpBigBody.MIN_BIG_BODY_SIZE.getBytes()) {
                body = new HttpBigBody(stream);
            } else {
                body = new HttpSimpleBody(stream);
            }

            return body.getContent(version, ContentType);
        } catch (@NotNull IOException | @NotNull MediaParserException e) {
            throw new RuntimeException(e);
        }
    }

    // Object

    /**
     * Retrieves or create the content of the HTTP body, decoded and transformed to the specified media type.
     *
     * @param version the http version of the content
     * @param ContentType the media type to which the content should be transformed must not be null
     * @param <T> the type of the content after transformation
     * @return the content of the HTTP body transformed to the specified media type
     *
     * @throws MediaParserException if an exception occurs, trying to parse the content
     * @throws IOException if an exception occurs, trying to read content
     */
    <T> @NotNull Content<T> getContent(@NotNull HttpVersion version, @NotNull ContentType<T> ContentType) throws MediaParserException, IOException;

    /**
     * Provides an {@link InputStream} for reading the raw content of the HTTP body.
     *
     * @return an input stream for reading the raw HTTP body content, never null
     * @throws IOException if an I/O exception occurs while perform
     */
    @NotNull InputStream getInputStream() throws IOException;

    /**
     * Writes the body content to an output stream according the headers specifications
     * It auto applies the content encodings and transfer encodings.
     *
     * @param headers the headers used to retrieve some information for a precise write
     * @param stream the output stream the data will be written
     *
     * @throws IOException if an I/O exception occurs while writing.
     * @throws EncodingException if an exception occurs trying to encode.
     * @throws DeferredException if any of encodings are deferred
     */
    void write(@NotNull HttpHeaders headers, @NotNull OutputStream stream) throws IOException, EncodingException, DeferredException;

    /**
     * Clones the {@link HttpBody} using the specified version.
     *
     * @param version the http version to create the body
     * @return a new http body with the selected version
     * @throws IOException if an I/O exception occurs while perform
     */
    default @NotNull HttpBody clone(@NotNull HttpVersion version) throws IOException {
        return create(getInputStream());
    }

}
