package codes.laivy.jhttp.protocol.v1_0;

import codes.laivy.jhttp.body.HttpBody;
import codes.laivy.jhttp.client.HttpClient;
import codes.laivy.jhttp.element.HttpProtocol;
import codes.laivy.jhttp.element.Method;
import codes.laivy.jhttp.element.Target;
import codes.laivy.jhttp.element.request.HttpRequest;
import codes.laivy.jhttp.element.request.HttpRequest.Future;
import codes.laivy.jhttp.exception.encoding.EncodingException;
import codes.laivy.jhttp.exception.parser.HeaderFormatException;
import codes.laivy.jhttp.exception.parser.element.HttpBodyParseException;
import codes.laivy.jhttp.exception.parser.element.HttpRequestParseException;
import codes.laivy.jhttp.headers.HttpHeader;
import codes.laivy.jhttp.headers.HttpHeaders;
import codes.laivy.jhttp.protocol.HttpVersion;
import codes.laivy.jhttp.protocol.factory.HttpRequestFactory;
import codes.laivy.jhttp.url.URIAuthority;
import codes.laivy.jhttp.utilities.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import static codes.laivy.jhttp.Main.CRLF;
import static codes.laivy.jhttp.headers.HttpHeaderKey.CONTENT_LENGTH;

final class HttpRequestFactory1_0 implements HttpRequestFactory {

    // Static initializers

    private static @NotNull URI uri(@NotNull String string) throws URISyntaxException {
        // Remove protocol
        for (@NotNull HttpProtocol protocol : HttpProtocol.values()) {
            if (string.startsWith(protocol.getName())) {
                string = string.replaceFirst(protocol.getName(), "");
            }
        }

        // Parse
        @NotNull String[] split = string.split("/", 2);

        if (split.length == 1) {
            return new URI("/");
        } else {
            return new URI("/" + split[1]);
        }
    }

    // Object

    private final @NotNull HttpVersion version;
    private final @NotNull Map<HttpClient, FutureImpl> futures;

    HttpRequestFactory1_0(@NotNull HttpVersion1_0 version) {
        this.version = version;
        this.futures = new HashMap<>();
    }

    // Getters

    @Override
    public @NotNull HttpVersion getVersion() {
        return version;
    }

    @Override
    public @NotNull HttpRequest create(@NotNull Method method, @Nullable URIAuthority authority, @NotNull URI uri, @NotNull HttpHeaders headers, @NotNull HttpBody body) {
        return new HttpRequestImpl(method, authority, uri, headers, body);
    }

    // Modules

    @Override
    public @NotNull String serialize(@NotNull HttpRequest request) throws EncodingException, IOException {
        if (!request.getVersion().equals(getVersion())) {
            throw new IllegalArgumentException("cannot serialize a '" + request.getVersion() + "' http request using a '" + getVersion() + "' http request factory");
        }

        @NotNull StringBuilder builder = new StringBuilder();

        // Write request line
        @NotNull String authority = request.getAuthority() != null ? request.getAuthority().toString() : "";

        @NotNull String uri = request.getUri().toString();
        if (!StringUtils.isBlank(uri)) {
            if (!uri.startsWith("/")) authority += "/";
            authority += request.getUri().toString();
        }

        builder.append(request.getMethod().name()).append(" ").append(authority).append(" ").append(getVersion());

        // Write headers
        for (@NotNull HttpHeader<?> header : request.getHeaders()) {
            if (!header.getKey().getTarget().isRequests()) continue;
            builder.append(CRLF).append(getVersion().getHeaderFactory().serialize(header));
        }

        // End request configurations
        builder.append(CRLF).append(CRLF);

        // Write a message if exists
        builder.append(getVersion().getBodyFactory().serialize(request.getHeaders(), request.getBody()));

        // Finish
        return builder.toString();
    }

    @Override
    public @NotNull HttpRequest parse(@NotNull String string) throws HttpRequestParseException, HttpBodyParseException {
        // Content
        @NotNull String[] content = string.split(CRLF + CRLF, 2);
        @NotNull String[] parts = content[0].split(CRLF, 2);

        // Headers
        @NotNull HttpHeaders headers = getVersion().getHeaderFactory().createMutable(Target.REQUEST);

        if (parts.length == 2) {
            for (@NotNull String line : parts[1].split(CRLF)) {
                try {
                    @NotNull HttpHeader<?> header = getVersion().getHeaderFactory().parse(line);
                    if (!header.getKey().getTarget().isRequests()) continue;

                    headers.add(header);
                } catch (@NotNull HeaderFormatException e) {
                    throw new HttpRequestParseException("cannot parse http request header line '" + line + "'", e);
                }
            }
        }

        // Request line
        @NotNull String[] requestLine = parts[0].split(" ");
        @NotNull Method method = Method.valueOf(requestLine[0].toUpperCase());
        @Nullable URIAuthority authority = null;
        @NotNull URI uri;

        if (method == Method.OPTIONS) {
            throw new HttpRequestParseException("the OPTIONS request method isn't supported by HTTP 1.0!");
        }

        // Uri
        if (!requestLine[2].equals(getVersion().toString())) {
            throw new HttpRequestParseException("the http version '" + requestLine[2] + "' isn't compatible with the '" + getVersion() + "' http request parser");
        } else try {
            uri = uri(requestLine[1]);
        } catch (@NotNull URISyntaxException e) {
            throw new HttpRequestParseException("cannot parse uri '" + requestLine[1] + "' from http request", e);
        }

        // Authority
        if (URIAuthority.validate(requestLine[1])) try {
            authority = URIAuthority.parse(requestLine[1]);
        } catch (@NotNull URISyntaxException e) {
            throw new HttpRequestParseException("cannot parse uri authority '" + requestLine[1] + "' from http request", e);
        }

        // Message
        @NotNull HttpBody body = content.length == 2 ? getVersion().getBodyFactory().parse(headers, content[1]) : HttpBody.empty();

        // Finish
        return HttpRequest.create(getVersion(), method, authority, uri, headers, body);
    }

    @Override
    public @NotNull Future parse(@NotNull HttpClient client, @NotNull String string) throws HttpRequestParseException {
        @NotNull FutureImpl future;

        if (futures.containsKey(client)) {
            future = futures.get(client);
            future.feed(string);
        } else {
            future = new FutureImpl(client, string);
        }

        return future;
    }

    @Override
    public boolean validate(@NotNull String string) {
        try {
            @NotNull String[] content = string.split(CRLF + CRLF, 2);

            // Check request line
            @NotNull String[] requestLine = content[0].split(CRLF, 2)[0].split("[\\s*]");

            // Check version
            if (!requestLine[2].equalsIgnoreCase(getVersion().toString())) {
                return false;
            }

            // Request method
            @NotNull Method method = Method.valueOf(requestLine[0].toUpperCase());

            if (method == Method.OPTIONS) {
                return false;
            }

            // Headers
            if (content[0].split(CRLF, 2).length > 1) {
                for (@NotNull String line : content[0].split(CRLF, 2)[1].split(CRLF)) {
                    if (!getVersion().getHeaderFactory().validate(line)) {
                        return false;
                    }
                }
            }

            // Finish
            return true;
        } catch (@NotNull Throwable throwable) {
            return false;
        }
    }

    // Classes

    private final class HttpRequestImpl implements HttpRequest {

        // Object

        private final @NotNull Method method;
        private final @Nullable URIAuthority authority;
        private final @NotNull URI uri;
        private final @NotNull HttpHeaders headers;
        private final @NotNull HttpBody body;

        private HttpRequestImpl(
                @NotNull Method method,
                @Nullable URIAuthority authority,
                @NotNull URI uri,
                @NotNull HttpHeaders headers,
                @NotNull HttpBody body
        ) {
            this.method = method;
            this.authority = authority;
            this.uri = uri;
            this.headers = headers;
            this.body = body;
        }

        // Getters

        @Override
        public @NotNull Method getMethod() {
            return method;
        }
        @Override
        public @Nullable URIAuthority getAuthority() {
            return authority;
        }
        @Override
        public @NotNull URI getUri() {
            return uri;
        }

        @Override
        public @NotNull HttpVersion getVersion() {
            return version;
        }
        @Override
        public @NotNull HttpHeaders getHeaders() {
            return headers;
        }
        @Override
        public @NotNull HttpBody getBody() {
            return body;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (!(object instanceof HttpRequest)) return false;
            @NotNull HttpRequest that = (HttpRequest) object;
            return getMethod() == that.getMethod() && Objects.equals(getAuthority(), that.getAuthority()) && Objects.equals(getUri(), that.getUri()) && Objects.equals(getHeaders(), that.getHeaders()) && Objects.equals(getBody(), that.getBody());
        }
        @Override
        public int hashCode() {
            return Objects.hash(getMethod(), getAuthority(), getUri(), getHeaders(), getBody());
        }
        @Override
        public @NotNull String toString() {
            try {
                return serialize(this);
            } catch (@NotNull EncodingException | @NotNull IOException e) {
                throw new RuntimeException("cannot serialize '" + getVersion() + "' request", e);
            }
        }

    }
    private final class FutureImpl implements Future {

        private final @NotNull CompletableFuture<HttpRequest> future = new CompletableFuture<>();
        private @UnknownNullability ScheduledFuture<?> timeout;

        private final @NotNull HttpClient client;

        private final @NotNull HttpVersion version;
        private final @NotNull Method method;
        private final @Nullable URIAuthority authority;
        private final @NotNull URI uri;
        private final @NotNull HttpHeaders headers;

        private @NotNull String body;

        public FutureImpl(
                @NotNull HttpClient client,
                @NotNull String body
        ) throws HttpRequestParseException {
            this.client = client;
            this.body = body;

            // Future checkers
            futures.put(client, this);
            future.whenComplete((done, exception) -> {
                futures.remove(client);
            });

            // Request
            try {
                @NotNull HttpRequest request = parse(body.split(CRLF + CRLF)[0]);
                this.version = request.getVersion();
                this.method = request.getMethod();
                this.authority = request.getAuthority();
                this.uri = request.getUri();
                this.headers = getVersion().getHeaderFactory().createImmutable(request.getHeaders());
            } catch (@NotNull HttpBodyParseException e) {
                throw new RuntimeException("illegal factory parser");
            }

            // Security
            check();
        }

        // Getters

        @Override
        public @NotNull HttpClient getClient() {
            return client;
        }
        @Override
        public @NotNull HttpVersion getVersion() {
            return version;
        }
        @Override
        public @NotNull Method getMethod() {
            return method;
        }
        @Override
        public @Nullable URIAuthority getAuthority() {
            return authority;
        }
        @Override
        public @NotNull URI getUri() {
            return uri;
        }
        @Override
        public @NotNull HttpHeaders getHeaders() {
            return headers;
        }

        // Modules

        private void check() {
            try {
                // Content Length
                if (getHeaders().contains(CONTENT_LENGTH)) {
                    // Check if the content length matches with the current body length and finish it true
                    // Also complete exceptionally if the current body is higher than the required
                    long required = getHeaders().get(CONTENT_LENGTH)[0].getValue().getBytes();

                    if (required <= body.length()) {
                        @NotNull String[] parts = body.split(CRLF + CRLF, 2);

                        if (parts.length == 2) {
                            parts[1] = parts[1].substring(0, (int) required);
                            body = parts[0] + CRLF + CRLF + parts[1];
                        }

                        // Completes the message with the new body
                        @NotNull HttpRequest request = parse(getAsString());
                        future.complete(request);
                    }
                } else {
                    // Completes the message; without the chunked transfer encoding or content length,
                    // there's no reason to continue with the http request future

                    @NotNull HttpRequest request = parse(getAsString());
                    future.complete(request);
                }
            } catch (@NotNull Throwable throwable) {
                // Any error report to the future
                future.completeExceptionally(throwable);
            }
        }
        public void feed(@NotNull String body) {
            if (future.isDone()) {
                throw new IllegalStateException("future already completed");
            }

            this.body += body;
            check();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }
        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public @NotNull HttpRequest get() throws InterruptedException, ExecutionException {
            return future.get();
        }
        @Override
        public @NotNull HttpRequest get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }

        // Implementations

        @Override
        public @NotNull String getAsString() {
            return body;
        }
        @Override
        public @NotNull String toString() {
            return "FutureImpl{" +
                    "future=" + future +
                    '}';
        }

        // Future

        @Override
        @Contract("_->this")
        public @NotNull Future whenComplete(@NotNull BiConsumer<? super HttpRequest, ? super Throwable> action) {
            future.whenComplete(action);
            return this;
        }
        @Override
        @Contract("_->this")
        public @NotNull Future orTimeout(@NotNull Duration duration) {
            // Schedule a timeout task
            if (timeout != null) timeout.cancel(true);
            timeout = HttpVersion1_0.FUTURE_TIMEOUT_SCHEDULED.schedule(() -> {
                cancel(true);
            }, duration.toMillis(), TimeUnit.MILLISECONDS);

            whenComplete((value, exception) -> {
                timeout.cancel(false);
            });

            // Finish
            return this;
        }

    }

}
