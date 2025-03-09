package dev.jhttp.core.header;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

final class HttpHeaderImpl<T> implements HttpHeader<T> {

    private final @NotNull Key<T> key;
    private @UnknownNullability T value;

    public HttpHeaderImpl(@NotNull Key<T> key, @UnknownNullability T value) {
        this.key = key;
        this.value = value;
    }

    // Getters

    @Override
    public @NotNull Key<T> getKey() {
        return key;
    }

    @Override
    public @UnknownNullability T getValue() {
        return value;
    }
    @Override
    public void setValue(@Nullable T value) {
        this.value = value;
    }

    // Cloneable

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull HttpHeader<T> clone() {
        return new HttpHeaderImpl<>(getKey(), getValue());
    }

    // Implementations

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof HttpHeaderImpl<?>)) return false;
        @NotNull HttpHeaderImpl<?> that = (HttpHeaderImpl<?>) object;
        return Objects.equals(getKey(), that.getKey()) && Objects.equals(getValue(), that.getValue());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }

    @Override
    public @NotNull String toString() {
        return "HttpHeader{" +
                "key=" + getKey() +
                ", value=" + getValue() +
                '}';
    }

}
