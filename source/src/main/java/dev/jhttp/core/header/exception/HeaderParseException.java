package dev.jhttp.core.header.exception;

import org.jetbrains.annotations.Nullable;

public class HeaderParseException extends HeaderException {
    public HeaderParseException(@Nullable String message) {
        super(message);
    }
    public HeaderParseException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public HeaderParseException(@Nullable Throwable cause) {
        super(cause);
    }
    public HeaderParseException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
