package dev.jhttp.core.body.content;

import dev.jhttp.core.body.content.ContentType.Parameter;
import dev.jhttp.core.version.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Media parsers are media transformers.
 * Each {@link ContentType} has its own parser.
 * <p>
 * For example, an "application/json" parser transforms the string into a functional JSON element.
 *
 * @param <T> the type of the content that this parser handles
 */
public interface ContentParser<T> {

    // Static initializers

    static @NotNull ContentParser<String> getDefault() {
        return TextContentType.getInstance().getParser();
    }

    // Object

    /**
     * Deserializes a given string into a {@link Content} object based on the specified {@link ContentType}.
     *
     * @param version the version to deserialize
     * @param stream the string representation of the content to be deserialized
     * @param parameters the MIME type parameters of this stream
     * @return the deserialized content object
     *
     * @throws IOException if an I/O error occurs during deserialization
     * @throws MediaParserException if an error occurs during deserialization
     */
    @NotNull T deserialize(@NotNull HttpVersion version, @NotNull InputStream stream, @NotNull Parameter @NotNull ... parameters) throws MediaParserException, IOException;

    /**
     * Serializes a given {@link Content} object into its string representation.
     *
     * @param version the version to serialize
     * @param content the content object to be serialized
     * @param parameters the MIME type parameters of this stream
     * @return the string representation of the serialized content
     *
     * @throws IOException if an I/O exception occurs, trying to serialize
     * @throws MediaParserException if there's an issue parsing to serialize
     */
    @NotNull InputStream serialize(@NotNull HttpVersion version, @NotNull T content, @NotNull Parameter @NotNull ... parameters) throws IOException, MediaParserException;

}