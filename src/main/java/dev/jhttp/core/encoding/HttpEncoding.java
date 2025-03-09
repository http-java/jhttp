package dev.jhttp.core.encoding;

import dev.jhttp.core.encoding.exception.DecodingException;
import dev.jhttp.core.encoding.exception.EncodingException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents an HTTP encoding mechanism used to encode and decode data streams as part of HTTP communication.
 *
 * <p>
 * This interface defines the contract for HTTP encodings that transform data for transmission over HTTP.
 * Implementations of this interface are responsible for processing data streams incrementally, handling
 * large volumes of data efficiently by reading and writing in chunks rather than processing the entire stream
 * at once. This approach is especially useful for streaming scenarios and when working with limited memory.
 * </p>
 *
 * <p>
 * Each HTTP encoding has a canonical name, retrievable via {@link #getName()}, and may support multiple
 * alias names, retrievable via {@link #getAliases()}. These identifiers facilitate interoperability and ensure
 * that the encoding can be recognized by various clients and servers that might refer to it by different names.
 * </p>
 *
 * <h2>Decoding and Encoding Operations</h2>
 * <p>
 * The {@link #read(InputStream, OutputStream)} method decodes data from an input stream and writes the decoded
 * data to an output stream. This operation is performed in a piecemeal fashion to accommodate the specifics
 * of the encoding and to optimize memory usage. Conversely, the {@link #write(InputStream, OutputStream)} method
 * reads raw data from an input stream, encodes it according to this encoding, and writes the encoded data to the
 * output stream.
 * </p>
 *
 * <p>
 * Both methods are designed to handle I/O operations robustly, throwing appropriate exceptions if an error occurs
 * during processing. Specifically, an {@link IOException} is thrown for general I/O errors, while {@link DecodingException}
 * or {@link EncodingException} is thrown when the data cannot be decoded or encoded properly due to format issues.
 * </p>
 *
 * @see IOException
 * @see EncodingException
 * @see DecodingException
 */
public interface HttpEncoding {

    /**
     * Returns the canonical name of this HTTP encoding.
     *
     * <p>
     * The canonical name serves as the primary identifier for the encoding. For instance, for a gzip encoding,
     * this method might return "gzip".
     * </p>
     *
     * @return a non-null {@link String} representing the canonical name of the encoding
     */
    @NotNull String getName();

    /**
     * Returns an array of alias names for this HTTP encoding.
     *
     * <p>
     * Aliases provide alternative identifiers that may be used to refer to the same encoding. This is useful
     * when dealing with variations in naming conventions or legacy systems that recognize the encoding by a different name.
     * </p>
     *
     * @return a non-null array of {@link String} objects, each representing an alias for this encoding
     */
    @NotNull String @NotNull [] getAliases();

    /**
     * Reads data from the specified input stream, decodes it according to this encoding, and writes the decoded data
     * to the provided output stream.
     *
     * <p>
     * This method processes the input stream incrementally, reading and decoding data in small chunks rather than all at once.
     * Such incremental processing helps to efficiently handle large data streams and minimizes memory consumption.
     * The decoded output is written gradually to the output stream.
     * </p>
     *
     * <p>
     * If the input data does not conform to the expected encoding format, or if an error occurs during the decoding process,
     * a {@link DecodingException} is thrown. Additionally, any I/O errors encountered during reading or writing will result in
     * an {@link IOException}.
     * </p>
     *
     * @param in  the input stream containing the encoded data; must not be null
     * @param out the output stream where the decoded data will be written; must not be null
     * @throws IOException if an I/O error occurs during reading or writing
     * @throws DecodingException if the input data is invalid or cannot be decoded according to the encoding's rules
     */
    void read(@NotNull InputStream in, @NotNull OutputStream out) throws IOException, DecodingException;

    /**
     * Reads data from the specified input stream, encodes it according to this encoding, and writes the encoded data
     * to the provided output stream.
     *
     * <p>
     * This method processes the input stream incrementally by encoding data in small chunks rather than processing the entire
     * stream at once. This chunk-based processing is crucial for handling large data sets efficiently and ensuring that the
     * encoded output is generated smoothly.
     * </p>
     *
     * <p>
     * If an error occurs during the encoding process, such as encountering data that cannot be properly encoded, an
     * {@link EncodingException} is thrown. Additionally, any I/O errors during the operation will cause an {@link IOException} to be thrown.
     * </p>
     *
     * @param in  the input stream containing the raw data to be encoded; must not be null
     * @param out the output stream where the encoded data will be written; must not be null
     * @throws IOException if an I/O error occurs during reading or writing
     * @throws EncodingException if the data cannot be encoded due to format or processing errors
     */
    void write(@NotNull InputStream in, @NotNull OutputStream out) throws IOException, EncodingException;

}
