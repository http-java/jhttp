package dev.jhttp.core.protocol;

import org.jetbrains.annotations.NotNull;

/**
 * The {@code HttpProtocol} enum defines the fundamental versions of the Hypertext Transfer Protocol (HTTP),
 * specifying key attributes such as scheme (URI prefix), default port, and security level.
 * <p>
 * HTTP is the foundation of data communication on the World Wide Web, and understanding its different
 * versions is crucial for ensuring proper client-server communication, security, and performance.
 * This enumeration provides a standardized way to reference HTTP and HTTPS protocols within applications,
 * ensuring consistency and reducing the risk of misconfiguration.
 * </p>
 *
 * <h2>Usage</h2>
 * This enum is particularly useful for:
 * <ul>
 *     <li>Defining network configurations that require specifying a protocol.</li>
 *     <li>Enforcing security policies by distinguishing between secure and non-secure connections.</li>
 *     <li>Generating URLs dynamically based on protocol requirements.</li>
 *     <li>Ensuring correct default port selection for HTTP-based services.</li>
 * </ul>
 *
 * <h2>Protocol Details</h2>
 * <p>
 *     HTTP (Hypertext Transfer Protocol) is the primary protocol used for web communication.
 *     It exists in two main forms:
 * </p>
 * <ul>
 *     <li><b>HTTP:</b> Operates over port 80, transmits data in plaintext, and is vulnerable to interception.</li>
 *     <li><b>HTTPS:</b> Operates over port 443, encrypts data using TLS/SSL, ensuring confidentiality and integrity.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>
 *     HttpProtocol protocol = HttpProtocol.HTTPS;
 *     System.out.println("Scheme: " + protocol.getName()); // Outputs: "https://"
 *     System.out.println("Port: " + protocol.getPort());   // Outputs: 443
 *     System.out.println("Secure: " + protocol.isSecure()); // Outputs: true
 * </pre>
 *
 * @author Daniel Meinicke
 * @since 0.1
 */
public enum HttpProtocol {

    /**
     * The standard HTTP protocol.
     * <p>
     * This is the original version of the protocol and is widely used for non-secure communication.
     * It uses the "http://" scheme and by default operates on port 80. Because HTTP does not encrypt
     * data, it is susceptible to eavesdrop, man-in-the-middle attacks, and data modification.
     * </p>
     */
    HTTP("http://", 80, false),

    /**
     * The secure variant of the HTTP protocol, known as HTTPS (Hypertext Transfer Protocol Secure).
     * <p>
     * HTTPS is an extension of HTTP that adds a layer of security through TLS (Transport Layer Security)
     * or SSL (Secure Sockets Layer). It ensures encrypted communication between client and server, making
     * it the preferred choice for sensitive transactions such as online banking, authentication, and secure
     * data exchange. HTTPS uses the "https://" scheme and operates on port 443 by default.
     * </p>
     */
    HTTPS("https://", 443, true);

    private final @NotNull String name;
    private final int port;
    private final boolean secure;

    /**
     * Constructs an {@code HttpProtocol} enum constant with the specified attributes.
     *
     * @param name   The URI scheme associated with the protocol, such as "http://" or "https://".
     * @param port   The default port number used by the protocol (e.g., 80 for HTTP, 443 for HTTPS).
     * @param secure A boolean flag indicating whether the protocol includes security features.
     */
    HttpProtocol(@NotNull String name, int port, boolean secure) {
        this.name = name;
        this.port = port;
        this.secure = secure;
    }

    /**
     * Retrieves the URI scheme (protocol name) for this HTTP protocol.
     * <p>
     * The returned value will be either "http://" or "https://" depending on the protocol.
     * This value is useful when constructing URLs dynamically.
     * </p>
     *
     * @return The protocol scheme as a string (e.g., "http://" or "https://").
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Retrieves the default port number associated with this HTTP protocol.
     * <p>
     * The default port is typically 80 for HTTP and 443 for HTTPS, but some applications may use custom ports.
     * This method ensures that the standard port is accessible for reference.
     * </p>
     *
     * @return The default port number for the protocol.
     */
    public int getPort() {
        return port;
    }

    /**
     * Determines whether this protocol is secure.
     * <p>
     * A secure protocol (HTTPS) ensures that data is encrypted and cannot be intercepted during transmission.
     * If this method returns {@code true}, the protocol uses encryption (TLS/SSL); otherwise, it transmits
     * data in plaintext.
     * </p>
     *
     * @return {@code true} if the protocol is secure (HTTPS), {@code false} if it is not (HTTP).
     */
    public boolean isSecure() {
        return secure;
    }
}