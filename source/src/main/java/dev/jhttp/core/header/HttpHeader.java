package dev.jhttp.core.header;

import dev.jhttp.core.header.factory.HttpHeaderFactory;
import dev.jhttp.core.header.factory.HttpHeaderParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a typed HTTP header, encapsulating both the header key and its associated value.
 *
 * <p>
 * The {@code HttpHeader} interface serves as the cornerstone for representing HTTP header fields
 * in a structured and type-safe manner. It combines a header's key (which defines metadata such as its name,
 * target, and associated types) with its value. This approach enables standardized handling, comparison,
 * cloning, and modification of headers.
 * </p>
 *
 * <p>
 * The interface is designed to work seamlessly with a corresponding factory and parser infrastructure,
 * as defined by the {@link HttpHeaderFactory} and {@link HttpHeaderParser} interfaces. The factory is responsible
 * for registering and retrieving header keys and their associated parsers, ensuring that headers are correctly
 * processed according to their specific characteristics. The parser is used to translate between the raw
 * textual representation of an HTTP header and its typed, structured representation.
 * </p>
 *
 * <h2>Static Initialization</h2>
 * <p>
 * The static {@code create} method provides a convenient way to instantiate a new {@code HttpHeader} using a given key
 * and value. This method delegates the creation process to the header key's own {@link Key#create(Object)} method,
 * ensuring that the header is constructed in accordance with the key's rules.
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * Typically, an HTTP header is created and managed via a header factory that maintains a registry of valid header keys and
 * their corresponding parsers. When parsing an HTTP request or response, the raw header string is converted into a structured
 * {@code HttpHeader} object using the appropriate parser. Conversely, when generating an HTTP message, the header is
 * serialized back into its textual form by the same parser.
 * </p>
 *
 * @param <T> the type of the header value
 * @see HttpHeaderFactory
 * @see HttpHeaderParser
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public interface HttpHeader<T> extends Cloneable {

    /**
     * Static initializer to create a new {@code HttpHeader} instance using the specified key and value.
     *
     * <p>
     * This method acts as a factory method, delegating the creation of a header instance to the provided key's
     * {@link Key#create(Object)} method. This ensures that the header is constructed properly and consistently.
     * </p>
     *
     * @param <E>   the type of the header value
     * @param key   the header key defining the metadata and characteristics of the header
     * @param value the header value; may be {@code null} depending on context
     * @return a new instance of {@code HttpHeader} containing the specified key and value
     */
    static <E> @NotNull HttpHeader<E> create(final @NotNull Key<E> key, final @UnknownNullability E value) {
        return key.create(value);
    }

    // Getters

    /**
     * Retrieves the header key associated with this HTTP header.
     *
     * <p>
     * The header key encapsulates metadata such as the header name, target (indicating whether the header is applicable
     * for requests, responses, or both), and any associated types that further define its behavior.
     * </p>
     *
     * @return the non-null {@link Key} representing the header's metadata
     */
    @NotNull Key<T> getKey();

    /**
     * Retrieves the name of the HTTP header.
     *
     * <p>
     * This method is a convenience that returns the header's name as defined by its key.
     * </p>
     *
     * @return the non-null header name
     */
    default @NotNull String getName() {
        return getKey().getName();
    }

    /**
     * Retrieves the value associated with this HTTP header.
     *
     * <p>
     * The header value is of a generic type {@code T}, ensuring type safety when processing header data.
     * The value can represent various data types, such as {@code String}, {@code Integer}, or even custom objects,
     * depending on the header's definition.
     * </p>
     *
     * @return the header value, which may be {@code null} if no value is set
     */
    @UnknownNullability T getValue();

    /**
     * Sets a new value for this HTTP header.
     *
     * <p>
     * This method allows the header's value to be updated. Implementations should handle any necessary validation or
     * conversion when assigning a new value.
     * </p>
     *
     * @param value the new value to assign to the header, which may be {@code null}
     */
    void setValue(@Nullable T value);

    // Implementations

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>
     * Two {@code HttpHeader} instances are considered equal if they have the same header key (ignoring case) and value.
     * </p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this header is equal to the specified object; {@code false} otherwise
     */
    @Override
    boolean equals(@Nullable Object o);

    /**
     * Returns a hash code value for this HTTP header.
     *
     * <p>
     * The hash code is computed based on the header's key and value, ensuring consistency with the {@link #equals(Object)} method.
     * </p>
     *
     * @return the hash code value for this header
     */
    @Override
    int hashCode();

    // Modules

    /**
     * Creates and returns a copy of this HTTP header.
     *
     * <p>
     * The {@code clone} method provides a mechanism to produce a deep copy of the header instance, ensuring that the key
     * and value are preserved in the new copy.
     * </p>
     *
     * @return a clone of this HTTP header instance
     */
    @NotNull HttpHeader<T> clone();

    // Classes

    /**
     * Represents a key for an HTTP header.
     *
     * <p>
     * The {@code Key} class encapsulates the metadata for an HTTP header, including:
     * </p>
     * <ul>
     *   <li>
     *     <strong>Name:</strong> The unique identifier for the header, used for lookup and matching.
     *   </li>
     *   <li>
     *     <strong>Target:</strong> Indicates whether the header is intended for HTTP requests, responses, or both.
     *   </li>
     *   <li>
     *     <strong>Types:</strong> A set of {@link Type} enums that categorize the header (e.g., client hints, conditional,
     *     hop-by-hop).
     *   </li>
     * </ul>
     *
     * <h2>Construction and Initialization</h2>
     * <p>
     * Two constructors are provided:
     * </p>
     * <ul>
     *   <li>
     *     {@code Key(String, Target)}: Constructs a key using the provided name and target. In this constructor,
     *     the set of header types is automatically determined by filtering through all available {@link Type} values,
     *     including only those that match the current key.
     *   </li>
     *   <li>
     *     {@code Key(String, Target, Type...)}: Constructs a key using the provided name, target, and an explicit set
     *     of header types. This allows for precise control over the header's categorization.
     *   </li>
     * </ul>
     *
     * <h2>Key Characteristics</h2>
     * <p>
     * The key is designed to be immutable. Its name, target, and types are established at construction and remain constant.
     * The key is used extensively in header parsing and serialization, as it provides the necessary context to interpret
     * the header's value.
     * </p>
     *
     * <h2>Type Information and Utilities</h2>
     * <p>
     * The types associated with a key are used to determine specific behaviors:
     * </p>
     * <ul>
     *   <li>{@code isHopByHop()} returns {@code true} if the header is a hop-by-hop header, meaning it is processed by
     *       each intermediary (e.g., proxies) and not forwarded to the final recipient.</li>
     *   <li>{@code isEndToEnd()} returns {@code true} if the header is an end-to-end header, implying that it is passed
     *       unchanged from the origin server to the client.</li>
     *   <li>{@code isClientHint()} returns {@code true} if the header is intended as a client hint, providing device or
     *       network information to optimize responses.</li>
     *   <li>{@code isConditional()} returns {@code true} if the header is used in conditional requests, typically for
     *       caching or resource validation purposes.</li>
     * </ul>
     *
     * <h2>Header Creation</h2>
     * <p>
     * The {@link #create(Object)} method provides a convenient way to instantiate a new {@link HttpHeader} for this key.
     * This method typically delegates header creation to a concrete implementation (such as {@code HttpHeaderImpl}),
     * ensuring that the header is constructed in accordance with the key's specifications.
     * </p>
     *
     * <h2>Validation and Equality</h2>
     * <p>
     * The {@code Key} class overrides {@link #equals(Object)} and {@link #hashCode()} to ensure that keys are compared
     * solely on the basis of their name (in a case-insensitive manner). This ensures that two keys with the same name are
     * considered equal regardless of other attributes.
     * </p>
     *
     * <h2>Internal Validation</h2>
     * <p>
     * A private utility method {@code verifyName} is provided to validate header names during key construction.
     * It checks that the header name is non-empty, starts with a letter, and contains only valid characters (letters,
     * digits, or hyphens). If any condition is violated, an exception is thrown to prevent the creation of an invalid key.
     * </p>
     *
     * @param <T> the type of the header value associated with the key
     */
    final class Key<T> {

        // Object

        private final @NotNull String name;
        private final @NotNull Target target;
        private final @NotNull Set<Type> types;

        /**
         * Constructs a new {@code Key} with the specified name and target.
         *
         * <p>
         * This constructor initializes the key's name and target, and automatically determines the header types
         * by filtering all available {@link Type} values for those that match this key.
         * </p>
         *
         * @param name   the name of the HTTP header; must not be empty and must begin with a letter
         * @param target the target indicating whether the header is intended for requests, responses, or both
         */
        public Key(@NotNull String name, @NotNull Target target) {
            this.name = name;
            this.target = target;
            this.types = Arrays.stream(Type.values()).filter(type -> type.matches(this)).collect(Collectors.toSet());
            
            verifyName(name);
        }

        /**
         * Constructs a new {@code Key} with the specified name, target, and explicit header types.
         *
         * <p>
         * This constructor allows for specifying a custom set of header types, providing fine-grained control over the
         * header's categorization.
         * </p>
         *
         * @param name   the name of the HTTP header; must not be empty and must begin with a letter
         * @param target the target indicating whether the header is intended for requests, responses, or both
         * @param types  an array of {@link Type} enums that explicitly define the header's categorization
         */
        public Key(@NotNull String name, @NotNull Target target, @NotNull Type @NotNull ... types) {
            this.name = name;
            this.target = target;
            this.types = new HashSet<>(Arrays.asList(types));

            verifyName(name);
        }

        /**
         * Retrieves the name of the header.
         *
         * <p>
         * The name uniquely identifies the header and is used for lookup and matching during both parsing
         * and serialization. Header names are case-insensitive.
         * </p>
         *
         * @return the non-null header name
         */
        @Contract(pure = true)
        public @NotNull String getName() {
            return this.name;
        }

        /**
         * Retrieves the target of the header.
         *
         * <p>
         * The target indicates whether the header is meant for HTTP requests, responses, or both.
         * </p>
         *
         * @return the non-null {@link Target} associated with this header
         */
        @Contract(pure = true)
        public @NotNull Target getTarget() {
            return this.target;
        }

        // Types

        /**
         * Retrieves the header types associated with this key.
         *
         * <p>
         * Header types further categorize the header, providing details about its behavior, such as whether it is a
         * client hint, conditional header, or hop-by-hop header.
         * </p>
         *
         * @return an array of {@link Type} representing the header's types
         */
        @Contract(pure = true)
        public @NotNull Type[] getTypes() {
            return types.toArray(new Type[0]);
        }

        /**
         * Checks if the header key contains the specified type.
         *
         * <p>
         * This method determines whether the given {@link Type} is included in the key's set of types.
         * </p>
         *
         * @param type the {@link Type} to check for
         * @return {@code true} if the key contains the specified type; {@code false} otherwise
         */
        public boolean hasType(@NotNull Type type) {
            return types.contains(type);
        }

        /**
         * Determines if the header is classified as a hop-by-hop header.
         *
         * <p>
         * Hop-by-hop headers are processed by each intermediary (such as proxies) and are not forwarded to the final recipient.
         * </p>
         *
         * @return {@code true} if the header is a hop-by-hop header; {@code false} otherwise
         */
        @Contract(pure = true)
        public boolean isHopByHop() {
            return hasType(Type.HOP_BY_HOP);
        }

        /**
         * Determines if the header is classified as an end-to-end header.
         *
         * <p>
         * End-to-end headers are passed unchanged from the origin server to the client. This method returns {@code true}
         * if the header is not marked as hop-by-hop.
         * </p>
         *
         * @return {@code true} if the header is an end-to-end header; {@code false} otherwise
         */
        @Contract(pure = true)
        public boolean isEndToEnd() {
            return !isHopByHop();
        }

        /**
         * Determines if the header is classified as a client hint.
         *
         * <p>
         * Client hint headers provide information about the client's device or network conditions,
         * allowing the server to tailor its responses.
         * </p>
         *
         * @return {@code true} if the header is a client hint; {@code false} otherwise
         */
        @Contract(pure = true)
        public boolean isClientHint() {
            return hasType(Type.CLIENT_HINT);
        }

        /**
         * Determines if the header is classified as a conditional header.
         *
         * <p>
         * Conditional headers are used to make HTTP requests conditional (often for caching or resource validation).
         * </p>
         *
         * @return {@code true} if the header is conditional; {@code false} otherwise
         */
        @Contract(pure = true)
        public boolean isConditional() {
            return hasType(Type.CONDITIONAL);
        }

        // Modules

        /**
         * Creates a new {@code HttpHeader} instance with the specified value.
         *
         * <p>
         * This method delegates header creation to a concrete implementation (for example, {@code HttpHeaderImpl})
         * that constructs a properly configured header instance using this key.
         * </p>
         *
         * @param value the value to be associated with the header
         * @return a new {@code HttpHeader} instance containing this key and the specified value
         */
        public @NotNull HttpHeader<T> create(@UnknownNullability T value) {
            return new HttpHeaderImpl<>(this, value);
        }

        // Implementations

        /**
         * Indicates whether this key is equal to another object.
         *
         * <p>
         * Two {@code Key} instances are considered equal if their names match, ignoring case differences.
         * </p>
         *
         * @param object the object to compare with
         * @return {@code true} if the object is a {@code Key} with the same name; {@code false} otherwise
         */
        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (!(object instanceof Key)) return false;
            @NotNull Key<?> that = (Key<?>) object;
            return getName().equalsIgnoreCase(that.getName());
        }

        /**
         * Returns a hash code value for this key.
         *
         * <p>
         * The hash code is computed based on the lowercase form of the header name, ensuring case-insensitive equality.
         * </p>
         *
         * @return the hash code value for this key
         */
        @Override
        public int hashCode() {
            return Objects.hash(getName().toLowerCase());
        }

        /**
         * Returns a string representation of the header key.
         *
         * <p>
         * This method returns the header name, which uniquely identifies the key.
         * </p>
         *
         * @return a string representation of the key
         */
        @Override
        public @NotNull String toString() {
            return getName();
        }

        // Utilities

        /**
         * Validates the provided header name.
         *
         * <p>
         * This utility method checks that the header name is not empty, starts with a letter, and contains only valid
         * characters (letters, digits, or hyphens) or if it's length is higher than 1024. If any of these conditions are not met, an exception is thrown to prevent
         * the creation of an invalid key.
         * </p>
         *
         * @param name the header name to validate
         * @throws IllegalArgumentException if the name is empty or does not start with a letter
         * @throws IllegalStateException    if the name contains illegal characters
         */
        private static void verifyName(@NotNull String name) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("header name cannot be null");
            } else if (name.length() > 1024) {
                throw new IllegalArgumentException("header name too long");
            } else if (!Character.isLetter(name.charAt(0))) {
                throw new IllegalArgumentException("the header key name must start with a letter");
            } else {
                char[] chars = name.toCharArray();
                for (int row = 0; row < chars.length; row++) {
                    char c = chars[row];
                    if (!Character.isLetterOrDigit(c) && c != '-') {
                        throw new IllegalStateException("the header key name contains an illegal character at index "
                                + row + ": '" + c + "'");
                    }
                }
            }
        }
    }

    /**
     * The Type enum categorizes various HTTP headers based on their functionality.
     * Each enum constant represents a category of headers used in HTTP requests and responses.
     * These categories help in organizing and understanding the purpose of each header type.
     *
     * @see <a href="https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Headers">MDN Web Docs - HTTP Headers</a>
     * @author Daniel Meinicke
     * @since 0.1
     */
    enum Type {
        /**
         * Headers related to authentication and authorization.
         * These headers are used to identify the user or client making the request and to provide credentials for access control.
         */
        AUTHENTICATION("WWW-Authenticate", "Authorization", "Proxy-Authenticate", "Proxy-Authorization"),

        /**
         * Headers used for caching mechanisms.
         * These headers control the storage and retrieval of cached content, affecting performance and freshness of resources.
         */
        CACHING("Age", "Cache-Control", "Expires", "Pragma", "Warning"),

        /**
         * Headers for client hints.
         * <p>
         * Client hints are HTTP headers used by servers to gather information about the client's device capabilities and preferences.
         * These headers enable servers to optimize resource delivery based on the specific requirements and limitations of the client's device,
         * resulting in improved performance and user experience.
         * <p>
         * By using client hints, servers can dynamically adapt the content they send to users, providing more relevant and efficient responses.
         * This can help in reducing bandwidth usage, improving load times, and ensuring that the content is appropriately tailored to the user's context.
         * <p>
         * The following headers are classified as client hints:
         * <ul>
         *     <li><b>Accept-CH</b>: Instructs the client to send specific client hint headers in subsequent requests.</li>
         *     <li><b>Accept-CH-Lifetime</b>: Indicates the duration (in seconds) that the client should remember and send the specified client hints.</li>
         *     <li><b>Content-DPR</b>: Specifies the device pixel ratio of the client device that should be used when fetching resources.</li>
         *     <li><b>DPR</b>: Device Pixel Ratio, indicates the pixel density of the client device.</li>
         *     <li><b>Device-Memory</b>: Indicates the amount of device memory the client has, in gigabytes.</li>
         *     <li><b>Save-Data</b>: A boolean value that indicates whether the user has enabled a reduced data usage mode in their browser.</li>
         *     <li><b>Viewport-Width</b>: Indicates the width of the client's viewport in CSS pixels.</li>
         *     <li><b>Width</b>: Specifies the desired width of the resource being requested, in physical pixels.</li>
         * </ul>
         * <p>
         * Example usage of client hints:
         * <pre>
         * {@code
         * HTTP/1.1 200 OK
         * Accept-CH: DPR, Width, Viewport-Width
         * Accept-CH-Lifetime: 86400
         * Content-DPR: 2.0
         * }
         * </pre>
         * In this example, the server instructs the client to send the DPR, Width, and Viewport-Width headers in subsequent requests.
         * The client should remember these hints for 86400 seconds (24 hours). The Content-DPR header specifies that the resource
         * being returned is optimized for a device pixel ratio of 2.0.
         */
        CLIENT_HINT("Accept-CH", "Accept-CH-Lifetime", "Content-DPR", "DPR", "Device-Memory", "Save-Data", "Viewport-Width", "Width", "Downlink", "ECT", "RTT", "Sec-CH-UA", "Sec-CH-UA-Arch", "Sec-CH-UA-Bitness", "Sec-CH-UA-Full-Version-List", "Sec-CH-UA-Full-Version", "Sec-CH-UA-Mobile", "Sec-CH-UA-Model", "Sec-CH-UA-Platform", "Sec-CH-UA-Platform-Version", "Sec-CH-Prefers-Color-Scheme", "Sec-CH-Prefers-Reduced-Motion"),

        /**
         * Headers for conditional requests.
         * These headers make the request conditional, usually based on the state of the resource or the client's cached version of the resource.
         */
        CONDITIONAL("Last-Modified", "ETag", "If-Match", "If-None-Match", "If-Modified-Since", "If-Unmodified-Since"),

        /**
         * Headers that manage connection control.
         * These headers control the connection's persistence and the management of multiple requests over a single connection.
         */
        CONNECTION("Connection", "Keep-Alive"),

        /**
         * Headers used for content negotiation.
         * These headers allow the client to specify its preferences for the type, charset, encoding, and language of the response content.
         */
        CONTENT_NEGOTIATION("Accept", "Accept-Charset", "Accept-Encoding", "Accept-Language"),

        /**
         * Headers that control the behavior of HTTP requests.
         * These headers define expectations and forwarding limits for requests.
         */
        CONTROL("Expect", "Max-Forwards"),

        /**
         * Headers for managing cookies.
         * These headers handle the storage and transmission of cookies, which are used for stateful interactions between the client and server.
         */
        COOKIE("Cookie", "Set-Cookie", "Cookie2", "Set-Cookie2"),

        /**
         * Headers for Cross-Origin Resource Sharing (CORS).
         * These headers manage resource sharing across different origins, controlling access and permissions for cross-origin requests.
         */
        CROSS_ORIGIN("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Access-Control-Allow-Headers", "Access-Control-Allow-Methods", "Access-Control-Expose-Headers", "Access-Control-Max-Age", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Origin", "Timing-Allow-Origin"),

        /**
         * Headers for tracking user preferences.
         * These headers are used to indicate the user's tracking preferences, such as enabling or disabling tracking.
         */
        DO_NOT_TRACK("DNT", "Tk"),

        /**
         * Headers related to downloads.
         * These headers provide information about how content should be displayed or handled when downloaded.
         */
        DOWNLOADS("Content-Disposition"),

        /**
         * Headers related to content properties.
         * These headers describe the size, type, encoding, language, and location of the content in the HTTP message.
         */
        CONTENT("Content-Length", "Content-Type", "Content-Encoding", "Content-Language", "Content-Location"),

        /**
         * Headers for routing requests through proxies.
         * These headers contain information about proxies that have forwarded the request, including client and server addresses.
         */
        ROUTING("Forwarded", "X-Forwarded-For", "X-Forwarded-Host", "X-Forwarded-Proto", "Via", "Host"),

        /**
         * Headers for redirection.
         * These headers provide information about the new location of a resource when a client needs to be redirected.
         */
        REDIRECT("Location"),

        /**
         * Headers that provide context about the request.
         * These headers include information about the client, such as the user agent, host, and referrer.
         */
        REQUEST_CONTEXT("From", "Host", "Referer", "Referrer-Policy", "User-Agent"),

        /**
         * Headers that provide context about the response.
         * These headers include information about the server and the capabilities it supports.
         */
        RESPONSE_CONTEXT("Allow", "Server"),

        /**
         * Headers used for specifying and handling ranges of data.
         * These headers are used for partial requests and responses, such as requesting or sending parts of a file.
         */
        RANGE("Accept-Ranges", "Range", "If-Range", "Content-Range"),

        /**
         * Headers related to security policies and enforcement.
         * These headers help protect resources from attacks and enforce security policies like content security and transport security.
         */
        SECURITY("Content-Security-Policy", "Content-Security-Policy-Report-Only", "Public-Key-Pins", "Public-Key-Pins-Report-Only", "Strict-Transport-Security", "Upgrade-Insecure-Requests", "X-Content-Type-Options", "X-Frame-Options", "X-XSS-Protection"),

        /**
         * Headers used for server-sent events. (Deprecated)
         * These headers were used for managing server-to-client notifications, now largely replaced by WebSockets and other technologies.
         */
        @Deprecated
        SERVER_EVENTS("Ping-From", "Ping-To", "Last-Event-ID"),

        /**
         * Headers related to transfer coding.
         * These headers are used to specify and manage different encoding methods for the message body, such as chunked transfer encoding.
         */
        TRANSFER_CODING("Transfer-Encoding", "TE", "Trailer"),

        /**
         * Headers used for WebSocket connections.
         * These headers manage the establishment and control of WebSocket connections, including keys and protocols.
         */
        WEBSOCKET("Sec-WebSocket-Key", "Sec-WebSocket-Extensions", "Sec-WebSocket-Accept", "Sec-WebSocket-Protocol", "Sec-WebSocket-Version"),

        /**
         * This enum value represents headers in HTTP that are hop-by-hop, meaning they are consumed and processed
         * by each intermediary node (e.g., proxies or gateways) in the communication path.
         * These headers are not passed
         * to the next node in the transmission chain
         * and have a specific behavior defined by the HTTP protocol.
         * <p>
         * Hop-by-hop headers are distinct from end-to-end headers,
         * which are transmitted unchanged from the origin to the final destination.
         * End-to-end headers contain information relevant to the entire request-response cycle and are
         * preserved by each intermediary node.
         * <p>
         * The headers listed in this enum are predefined hop-by-hop headers according to the HTTP RFC standards
         * (RFC 7230 and related RFCs).
         * These headers have specific semantics and are processed differently from other headers in the HTTP message.
         * <p>
         * Hop-by-hop headers are critical for managing the behavior of intermediary nodes, controlling aspects such as
         * connection persistence, authentication, content encoding, and protocol upgrades.
         * They play a crucial role in
         * ensuring the integrity and efficiency of HTTP communication across distributed networks.
         */
        HOP_BY_HOP("Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailers", "Transfer-Encoding", "Upgrade"),

        /**
         * Enum representing headers in HTTP that are forbidden to be set in certain contexts.
         * <p>
         * Forbidden header names are restricted from being set in HTTP requests made by JavaScript
         * using the XMLHttpRequest or fetch() API in the browser to prevent security vulnerabilities
         * such as Cross-Site Scripting (XSS).
         * <p>
         * The headers listed in this enum are commonly recognized as forbidden header names according to
         * browser security policies. Attempting to set these headers using JavaScript may result in the
         * browser ignoring or replacing the provided values to ensure user security and the integrity
         * of the HTTP protocol.
         * <p>
         * These headers are restricted to prevent manipulation of critical HTTP request attributes
         * that could otherwise be exploited to perform attacks or circumvent security measures.
         * <p>
         * The following headers are classified as forbidden names:
         * <ul>
         *     <li><b>Accept-Charset</b>: Specifies the character encodings that the client is willing to accept.</li>
         *     <li><b>Accept-Encoding</b>: Specifies the content encodings that the client is willing to accept.</li>
         *     <li><b>Access-Control-Request-Headers</b>: Indicates which HTTP headers can be used when making the actual request.</li>
         *     <li><b>Access-Control-Request-Method</b>: Indicates which HTTP method can be used when making the actual request.</li>
         *     <li><b>Connection</b>: Controls whether the network connection stays open after the current transaction finishes.</li>
         *     <li><b>Content-Length</b>: Specifies the size of the request or response body in bytes.</li>
         *     <li><b>Cookie</b>: Contains stored HTTP cookies previously sent by the server with Set-Cookie headers.</li>
         *     <li><b>Cookie2</b>: Similar to Cookie but used in an obsolete version of the HTTP protocol.</li>
         *     <li><b>Date</b>: Contains the date and time at which the message was sent.</li>
         *     <li><b>DNT</b>: Do Not Track, a request for the server to disable tracking of the user.</li>
         *     <li><b>Expect</b>: Indicates that particular server behaviors are required by the client.</li>
         *     <li><b>Host</b>: Specifies the domain name of the server and (optionally) the TCP port number on which the server is listening.</li>
         *     <li><b>Keep-Alive</b>: Controls the persistence of the network connection.</li>
         *     <li><b>Origin</b>: Indicates the origin of the cross-origin request.</li>
         *     <li><b>Referer</b>: Contains the address of the previous web page from which a link to the current request URL was followed.</li>
         *     <li><b>TE</b>: Indicates the transfer encodings the user agent is willing to accept.</li>
         *     <li><b>Trailer</b>: Indicates that the message body is terminated by a set of header fields.</li>
         *     <li><b>Transfer-Encoding</b>: Specifies the form of encoding used to safely transfer the payload body to the user.</li>
         *     <li><b>Upgrade</b>: Used to switch protocols.</li>
         *     <li><b>Via</b>: Added by proxies, both forward and reverse proxies, and can appear in the request headers and the response headers.</li>
         * </ul>
         * Additionally, headers that start with "Proxy-" or "Pec-" (case-insensitive) are considered forbidden:
         * <ul>
         *     <li><b>Proxy-</b>: Headers starting with "Proxy-" are reserved for proxy-related operations.</li>
         *     <li><b>Sec-</b>: Headers starting with "Sec-" are reserved for security-related operations.</li>
         * </ul>
         */
        FORBIDDEN_NAME("Accept-Charset", "Accept-Encoding", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Connection", "Content-Length", "Cookie", "Cookie2", "Date", "DNT", "Expect", "Host", "Keep-Alive", "Origin", "Referer", "TE", "Trailer", "Transfer-Encoding", "Upgrade", "Via") {
            @Override
            public boolean matches(@NotNull Key<?> key) {
                return super.matches(key) || key.getName().toLowerCase().startsWith("proxy-") || key.getName().toLowerCase().startsWith("sec-");
            }
        },

        /**
         * Enum representing CORS-satellited request headers, also known as "simple headers".
         * These headers can be sent in a CORS request without triggering a preflight OPTIONS request.
         * <p>
         * CORS (Cross-Origin Resource Sharing) is a mechanism that allows web applications to request resources
         * from a different domain than the one that served the web page.
         * This is commonly used for APIs.
         * <p>
         * Simple headers are considered safe because their values have limited impact on the request's behavior
         * and do not pose significant security risks.
         * They are restricted to a specific set of headers that are
         * commonly used and deemed secure for inclusion in CORS requests.
         * <p>
         * The following headers are classified as simple headers:
         *
         * <ul>
         *     <li><b>Accept</b>: Indicates the media types that the client can understand.</li>
         *     <li><b>Accept-Language</b>: Indicates the natural languages that the client prefers.</li>
         *     <li><b>Content-Language</b>: Describes the natural language(s) of the intended audience for the enclosed content.</li>
         *     <li><b>Content-Type</b>: Indicates the media type of the resource or the data being sent in the request.</li>
         *     <li><b>Range</b>: Requests only part of an entity. Used for partial downloads and presumable downloads.</li>
         * </ul>
         */
        SIMPLE_HEADER("Accept", "Accept-Language", "Content-Language", "Content-Type", "Range"),

        ;

        private final @NotNull String[] headers;

        Type(@NotNull String @NotNull ... headers) {
            this.headers = headers;
        }

        /**
         * Checks if the specified header key matches any header in the current type.
         *
         * @param key the HeaderKey to be checked
         * @return {@code true} if the key matches any header in the current type, {@code false} otherwise
         */
        public boolean matches(@NotNull Key<?> key) {
            return Arrays.stream(headers).anyMatch(name -> key.getName().equalsIgnoreCase(name));
        }

    }

}
