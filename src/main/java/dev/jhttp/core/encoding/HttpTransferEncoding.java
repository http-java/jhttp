package dev.jhttp.core.encoding;

/**
 * Represents an HTTP transfer encoding mechanism used to encode and decode message bodies in HTTP communication.
 *
 * <p>
 * This interface extends {@link HttpEncoding} and specifically focuses on transfer encodings, which are applied
 * at the transport level to modify how message bodies are sent between clients and servers. Unlike content encodings
 * (which define how data is compressed or formatted for interpretation), transfer encodings deal with the way data
 * is transmitted over the network, ensuring proper chunking, framing, or other transmission-specific transformations.
 * </p>
 *
 * <h2>Common Transfer Encodings</h2>
 * <p>
 * Some commonly used HTTP transfer encodings include:
 * <ul>
 *     <li><b>Chunked</b> - Used in HTTP/1.1 to allow dynamic data streaming without a predefined content length.</li>
 *     <li><b>Compress</b> - A legacy encoding that applies LZW compression before transmission.</li>
 *     <li><b>Deflate</b> - A more efficient compression encoding combining LZ77 and Huffman coding.</li>
 *     <li><b>Gzip</b> - A widely used encoding for compressing data before transmission.</li>
 * </ul>
 * </p>
 *
 * <h2>Usage in HTTP</h2>
 * <p>
 * Transfer encodings are primarily used in HTTP/1.1 and earlier versions, where the message body format
 * needs to be modified for efficient transmission. In HTTP/2 and HTTP/3, transfer encodings are generally
 * replaced by more efficient multiplexing and framing mechanisms.
 * </p>
 *
 * <p>
 * Implementations of this interface should ensure compliance with HTTP standards, particularly RFC 2616 (HTTP/1.1)
 * and RFC 7230 (Message Syntax and Routing). The methods inherited from {@link HttpEncoding} should be implemented
 * to handle data transformation in accordance with the specific transfer encoding rules.
 * </p>
 *
 * @see HttpEncoding
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616">RFC 2616 (HTTP/1.1)</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7230">RFC 7230 (HTTP/1.1 Message Syntax and Routing)</a>
 */
public interface HttpTransferEncoding extends HttpEncoding {
}