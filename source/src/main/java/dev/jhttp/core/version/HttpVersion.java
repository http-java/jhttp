package dev.jhttp.core.version;

import dev.jhttp.core.encoding.collection.HttpEncodings;
import dev.jhttp.core.header.factory.HttpHeaderFactory;
import org.jetbrains.annotations.NotNull;

public interface HttpVersion {

    /**
     * Retrieves the byte identification sequence used in the Application-Layer Protocol Negotiation (ALPN).
     * <p>
     * The byte sequence returned by this method corresponds to the ALPN protocol ID sequence, which is used to
     * identify the protocol to be used for communication over a given connection. This method plays a crucial
     * role in ensuring that the correct protocol is negotiated between a client and a server during the
     * establishment of a secure connection.
     * </p>
     * <p>
     * For more details, refer to the <a href="https://developer.mozilla.org/en-US/docs/Glossary/ALPN">Mozilla ALPN Protocol ID Sequence</a> documentation.
     * </p>
     *
     * @return a byte array representing the ALPN protocol identification sequence.
     * @author Daniel Meinicke
     * @since 0.1
     */
    byte[] getId();

    // Getters

    @NotNull HttpHeaderFactory getHeaderFactory();

    @NotNull HttpEncodings getEncodings();

    int getMajor();
    int getMinor();

}
