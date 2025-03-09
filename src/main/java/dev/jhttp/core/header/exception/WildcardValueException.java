package dev.jhttp.core.header.exception;

import org.jetbrains.annotations.Nullable;

public class WildcardValueException extends RuntimeException {
    public WildcardValueException(@Nullable Throwable cause) {
        super(cause);
    }
    public WildcardValueException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public WildcardValueException(@Nullable String message) {
        super(message);
    }
    public WildcardValueException() {
    }
}
