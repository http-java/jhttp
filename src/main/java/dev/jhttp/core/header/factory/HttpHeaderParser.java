package dev.jhttp.core.header.factory;

import dev.jhttp.core.header.HttpHeader;
import dev.jhttp.core.header.exception.HeaderParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a contract for parsing and serializing HTTP header values.
 *
 * <p>
 * The {@code HttpHeaderParser} interface provides methods to convert between the raw, textual representation
 * of an HTTP header's value and its structured, typed {@link HttpHeader} form. In this context, the {@code read}
 * method is responsible for parsing a string that contains <strong>only the value</strong> of an HTTP header,
 * without the header name. This design allows the parser to focus solely on the content and formatting of the header value,
 * leveraging the associated header key information provided elsewhere.
 * </p>
 *
 * <p>
 * The interface uses a generic type parameter {@code T} to ensure type safety, allowing the parsed header value to be
 * represented in its appropriate form (such as {@code String}, {@code Integer}, or custom objects) as defined by the header's key.
 * Implementations of this interface must handle any necessary validation and conversion of the raw header value.
 * </p>
 *
 * <h2>Parsing HTTP Header Values</h2>
 * <p>
 * The {@link #read(String)} method takes a non-null string that represents the header's value and attempts to create
 * a corresponding {@link HttpHeader} instance. If the string is improperly formatted or fails validation according to
 * the specific rules of the header type, a {@link HeaderParseException} is thrown. This ensures that only valid
 * header values are accepted and processed.
 * </p>
 *
 * <h2>Serializing HTTP Header Values</h2>
 * <p>
 * The {@link #write(HttpHeader)} method converts the structured {@link HttpHeader} object back into its raw textual form,
 * focusing exclusively on the header value. The method returns a non-null string that represents the serialized value,
 * which is suitable for inclusion in HTTP requests or responses. Implementations must ensure that the resulting string
 * conforms to the expected format of the header value.
 * </p>
 *
 * <h2>Integration with Header Factories and Keys</h2>
 * <p>
 * This parser interface is designed to be integrated with a header factory mechanism, where header keys are registered
 * along with their respective parsers. The header key (an instance of {@link HttpHeader.Key}) carries metadata such as the
 * header name, target, and type categorizations, while the parser focuses solely on the value conversion. Together,
 * they ensure a robust, modular system for handling HTTP headers in both request and response contexts.
 * </p>
 *
 * <h2>Error Handling and Robustness</h2>
 * <p>
 * The design of the {@code HttpHeaderParser} interface enforces strict error handling. When the header value string does not
 * conform to the expected format or contains invalid data, implementations must throw a {@link HeaderParseException} to
 * signal the parsing error. This exception mechanism ensures that the calling code can react appropriately to malformed
 * header values and maintain the integrity of the HTTP communication process.
 * </p>
 *
 * @param <T> the type of the header value that this parser handles
 * @see HttpHeader
 * @see HeaderParseException
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface HttpHeaderParser<T> {

    /**
     * Parses the given string, which contains only the value of an HTTP header, and returns a structured {@link HttpHeader} instance.
     *
     * <p>
     * This method is designed to handle raw header values by converting them into a type-safe, structured form as defined by the
     * generic type {@code T}. The string provided should not include the header name; it must consist solely of the header's value.
     * If the string is malformed, or if the conversion fails due to unexpected formatting, a {@link HeaderParseException} is thrown.
     * </p>
     *
     * @param string a non-null string representing only the HTTP header's value
     * @return a non-null {@link HttpHeader} instance containing the parsed value
     * @throws HeaderParseException if the header value is invalid or cannot be parsed according to the header's specific rules
     */
    @NotNull HttpHeader<T> read(@NotNull String string) throws HeaderParseException;

    /**
     * Serializes the given {@link HttpHeader} instance and returns its raw textual representation as a string.
     *
     * <p>
     * This method converts the structured {@link HttpHeader} object back into its raw textual form,
     * focusing exclusively on the header value. The returned string represents the header value that can be
     * transmitted as part of an HTTP request or response. Implementations must ensure that the resulting
     * string adheres to the proper formatting rules for HTTP header values.
     * </p>
     *
     * @param header the {@link HttpHeader} instance to serialize; must not be null
     * @return a non-null string containing the serialized header value
     */
    @NotNull String write(@NotNull HttpHeader<T> header);
}