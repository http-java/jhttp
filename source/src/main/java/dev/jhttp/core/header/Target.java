package dev.jhttp.core.header;

/**
 * The {@code Target} enum defines the context in which an HTTP header is applicable.
 * HTTP headers may be intended for use in requests, responses, or both. This enum is used to
 * clearly specify the intended direction of a header, aiding in proper header management and validation
 * in HTTP communication frameworks.
 *
 * <p>Each enum constant encapsulates two properties that indicate whether a header should be applied
 * to HTTP requests, responses, or both. This distinction is useful when processing headers differently
 * based on their target context. For example, some headers are only valid in requests while others are only valid in responses.
 * Using the {@code Target} enum ensures that headers are correctly categorized, reducing the risk of misapplication
 * and enhancing the clarity of the HTTP handling logic.</p>
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public enum Target {

    /**
     * Indicates that the header is intended exclusively for HTTP requests.
     * This target should be used when a header is only applicable to the client-to-server communication.
     */
    REQUEST(true, false),

    /**
     * Indicates that the header is intended exclusively for HTTP responses.
     * This target should be used when a header is only applicable to the server-to-client communication.
     */
    RESPONSE(false, true),

    /**
     * Indicates that the header is applicable to both HTTP requests and responses.
     * This target is used for headers that are relevant in both the sending and receiving phases of an HTTP transaction.
     */
    BOTH(true, true);

    private final boolean requests;
    private final boolean responses;

    /**
     * Constructs a new {@code Target} instance with the specified applicability.
     *
     * @param requests  {@code true} if the header is applicable to HTTP requests, {@code false} otherwise
     * @param responses {@code true} if the header is applicable to HTTP responses, {@code false} otherwise
     */
    Target(boolean requests, boolean responses) {
        this.requests = requests;
        this.responses = responses;
    }

    /**
     * Determines whether the header is intended for use in HTTP requests.
     *
     * @return {@code true} if the header should be applied in HTTP requests; {@code false} otherwise
     */
    public boolean isRequests() {
        return requests;
    }

    /**
     * Determines whether the header is intended for use in HTTP responses.
     *
     * @return {@code true} if the header should be applied in HTTP responses; {@code false} otherwise
     */
    public boolean isResponses() {
        return responses;
    }
}