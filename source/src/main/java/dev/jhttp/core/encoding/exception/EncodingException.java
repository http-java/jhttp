package dev.jhttp.core.encoding.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class EncodingException extends IOException {
    public EncodingException(@Nullable Throwable cause) {
        super(cause);
    }
    public EncodingException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public EncodingException(@Nullable String message) {
        super(message);
    }
    public EncodingException() {
    }
}
