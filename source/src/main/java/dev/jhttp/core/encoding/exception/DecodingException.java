package dev.jhttp.core.encoding.exception;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class DecodingException extends IOException {
    public DecodingException(@Nullable Throwable cause) {
        super(cause);
    }
    public DecodingException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public DecodingException(@Nullable String message) {
        super(message);
    }
    public DecodingException() {
    }
}
