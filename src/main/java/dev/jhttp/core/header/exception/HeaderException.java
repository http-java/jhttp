package dev.jhttp.core.header.exception;

import org.jetbrains.annotations.Nullable;

public class HeaderException extends Exception {
    public HeaderException(@Nullable String message) {
        super(message);
    }
    public HeaderException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public HeaderException(@Nullable Throwable cause) {
        super(cause);
    }
    public HeaderException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
