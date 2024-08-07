package codes.laivy.jhttp.media;

import codes.laivy.jhttp.body.HttpBody;
import codes.laivy.jhttp.deferred.Deferred;
import codes.laivy.jhttp.element.FormData;
import codes.laivy.jhttp.exception.media.MediaParserException;
import codes.laivy.jhttp.media.form.FormUrlEncodedMediaType;
import codes.laivy.jhttp.media.form.MultipartFormDataMediaType;
import codes.laivy.jhttp.media.html.HTMLMediaType;
import codes.laivy.jhttp.media.jar.JarMediaType;
import codes.laivy.jhttp.media.json.JsonMediaType;
import codes.laivy.jhttp.media.text.TextMediaType;
import codes.laivy.jhttp.protocol.HttpVersion;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Represents a media type and its associated parser. Each media type is defined by a type, optional parameters,
 * and a parser that can handle the content of that type.
 *
 * @param <T> the type of the content that this media type handles
 *
 * @author Daniel Richard (Laivy)
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("unchecked")
public class MediaType<T> {

    // Static initializers

    public static @NotNull MediaType<@NotNull JsonElement> APPLICATION_JSON() { return JsonMediaType.getInstance(); }
    public static @NotNull MediaType<@NotNull JarFile> APPLICATION_JAR() { return JarMediaType.getInstance(); }
    public static @NotNull MediaType<@NotNull String> TEXT_PLAIN() { return TextMediaType.getInstance(); }
    public static @NotNull MediaType<@NotNull FormData @NotNull []> X_WWW_FORM_URLENCODED() { return FormUrlEncodedMediaType.getInstance(); }
    public static @NotNull MediaType<@NotNull FormData @NotNull []> MULTIPART_FORMDATA() { return MultipartFormDataMediaType.getInstance(); }

    // todo 16/07/2024: create jhtml library
    @ApiStatus.Experimental
    public static @NotNull MediaType<@NotNull Element> TEXT_HTML() { return HTMLMediaType.getInstance(); }

    @ApiStatus.Experimental
    public static @NotNull MediaType<byte[]> custom(@NotNull Type type, @NotNull Parameter @NotNull ... parameters) {
        @NotNull MediaParser<byte[]> parser = new MediaParser<byte[]>() {
            @Override
            public byte @NotNull [] deserialize(@NotNull HttpVersion version, @NotNull InputStream stream, @NotNull Parameter @NotNull ... parameters) throws MediaParserException, IOException {
                byte[] bytes = new byte[stream.available()];

                try (@NotNull InputStreamReader reader = new InputStreamReader(stream)) {
                    int row = 0;
                    while (reader.ready()) {
                        bytes[row] = (byte) reader.read();
                        row++;
                    }
                }

                return bytes;
            }
            @Override
            public @NotNull InputStream serialize(@NotNull HttpVersion version, byte @NotNull [] content, @NotNull Parameter @NotNull ... parameters) throws IOException, MediaParserException {
                return new ByteArrayInputStream(content);
            }
        };

        return new MediaType<>(type, parser, parameters);
    }

    // Media type content

    private static final @NotNull Set<MediaType<?>> collection = ConcurrentHashMap.newKeySet();

    static {
        // Load all default media types
        for (@NotNull Method method : MediaType.class.getDeclaredMethods()) {
            try {
                method.setAccessible(true);

                if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0 && method.getReturnType() == MediaType.class) {
                    @NotNull MediaType<?> media = (MediaType<?>) method.invoke(null);
                    collection.add(media);
                }
            } catch (@NotNull InvocationTargetException | @NotNull IllegalAccessException e) {
                throw new RuntimeException("cannot load media type with method #" + method.getName(), e);
            }
        }
    }

    /**
     * Retrieves all the available media types.
     *
     * @return an unmodifiable collection of all registered media types
     * @author Daniel Richard (Laivy)
     */
    public static @NotNull Collection<MediaType<?>> retrieve() {
        return Collections.unmodifiableSet(collection);
    }

    /**
     * Retrieves a media type by its name or alias.
     *
     * @param type the type of the media type to retrieve
     * @return an optional containing the media type if found, otherwise an empty optional
     * @author Daniel Richard (Laivy)
     */
    public static @NotNull Optional<MediaType<?>> retrieve(@NotNull Type type) {
        return stream().filter(media -> media.getType().equals(type)).findFirst();
    }

    /**
     * Adds a custom media type to the collection.
     *
     * @param type the media type to add
     * @return {@code true} if the media type was added successfully, {@code false} if it was already present
     * @author Daniel Richard (Laivy)
     */
    public static boolean add(@NotNull MediaType<?> type) {
        // Check if there's a media type with that name already defined
        if (collection.stream().anyMatch(media -> media.getType().equals(type.getType()))) {
            return false;
        }

        return collection.add(type);
    }

    /**
     * Removes a custom media type from the collection.
     *
     * @param media the media type to remove
     * @return {@code true} if the media type was removed successfully, {@code false} if it was not present
     * @author Daniel Richard (Laivy)
     */
    public static boolean remove(@NotNull MediaType<?> media) {
        return collection.remove(media);
    }

    /**
     * Checks if a specific media type is present in the collection.
     *
     * @param media the media type to check for
     * @return {@code true} if the media type is present, {@code false} otherwise
     * @author Daniel Richard (Laivy)
     */
    public static boolean contains(@NotNull MediaType<?> media) {
        return retrieve().contains(media);
    }

    /**
     * Checks if a specific media type is present in the collection by it's name.
     *
     * @param type the type of the media
     * @return {@code true} if the media type is present, {@code false} otherwise
     * @author Daniel Richard (Laivy)
     */
    public static boolean contains(@NotNull Type type) {
        return retrieve(type).isPresent();
    }

    /**
     * Returns the number of media types in the collection, including the default ones.
     *
     * @return the number of media types in the collection
     * @author Daniel Richard (Laivy)
     */
    public static int size() {
        return retrieve().size();
    }

    /**
     * Returns a stream of all the media types in the collection.
     *
     * @return a stream of all media types
     * @author Daniel Richard (Laivy)
     */
    public static @NotNull Stream<MediaType<?>> stream() {
        return retrieve().stream();
    }

    /**
     * Returns an iterator over the media types in the collection.
     *
     * @return an iterator over the media types
     * @author Daniel Richard (Laivy)
     */
    public static @NotNull Iterator<MediaType<?>> iterator() {
        return retrieve().iterator();
    }

    /**
     * Returns an array containing all the media types in the collection.
     *
     * @return an array of all media types
     * @author Daniel Richard (Laivy)
     */
    public static @NotNull MediaType<?>[] toArray() {
        return retrieve().toArray(new MediaType<?>[0]);
    }

    // Object

    private final @NotNull Type type;
    private final @NotNull MediaParser<T> parser;
    private final @NotNull Parameter @NotNull [] parameters;

    protected MediaType(@NotNull Type type, @NotNull MediaParser<T> parser, @NotNull Parameter @NotNull [] parameters) {
        this.type = type;
        this.parser = parser;
        this.parameters = parameters;
    }

    // Getters

    /**
     * Returns the type of this media type.
     *
     * @return the type of this media type
     */
    public final @NotNull Type getType() {
        return type;
    }

    /**
     * Returns the parser associated with this media type.
     *
     * @return the parser associated with this media type
     */
    public @NotNull MediaParser<T> getParser() {
        return parser;
    }

    /**
     * The parameters of a media type.
     * <p>
     * As an example, the media type "text/plain" can have the parameter "charset" with the value "utf-8"
     *
     * @return the parameter array of this media type
     */
    public @NotNull Parameter @NotNull [] getParameters() {
        return parameters;
    }

    /**
     * Returns the parameter with the specified key, if present.
     *
     * @param key the key of the parameter to retrieve
     * @return an optional containing the parameter if present, or an empty optional if not
     */
    public final @NotNull Optional<Parameter> getParameter(@NotNull String key) {
        return Arrays.stream(getParameters()).filter(p -> p.getKey().equalsIgnoreCase(key)).findFirst();
    }

    /**
     * Returns the charset parameter of this media type, if present.
     *
     * @return the charset parameter if present, or null if not
     */
    public @Nullable Deferred<Charset> getCharset() {
        @Nullable Parameter parameter = getParameter("charset").orElse(null);
        return parameter != null ? Deferred.charset(parameter.getValue()) : null;
    }

    /**
     * Returns the boundary parameter of this media type, if present.
     *
     * @return the boundary parameter if present, or null if not
     */
    public @Nullable Boundary getBoundary() {
        @Nullable Parameter parameter = getParameter("boundary").orElse(null);

        if (parameter != null) {
            return new Boundary(parameter.getValue());
        } else {
            return null;
        }
    }

    public @NotNull Content<T> create(@NotNull HttpVersion version, @NotNull T object) {
        return HttpBody.create(version, this, object);
    }

    public @NotNull MediaType<T> clone(@NotNull Parameter @NotNull ... parameters) {
        return new MediaType<>(getType(), getParser(), parameters);
    }

    // Implementations

    @Override
    public final boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof MediaType)) return false;
        @NotNull MediaType<?> mediaType = (MediaType<?>) object;
        return Objects.equals(getType(), mediaType.getType()) && Objects.deepEquals(getParameters(), mediaType.getParameters());
    }
    @Override
    public final int hashCode() {
        return Objects.hash(getType(), Arrays.hashCode(getParameters()));
    }

    @Override
    public final @NotNull String toString() {
        return Parser.serialize(this);
    }

    // Classes

    /**
     * Utility class for parsing and serializing media types.
     *
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    public static final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        // Serializers

        @ApiStatus.Internal
        private static final @NotNull Pattern PARSE_PATTERN = Pattern.compile("([^;\\s]+)(?:;\\s*charset=([^;\\s]+))?(?:;\\s*(.*))?");

        /**
         * Serializes the given media type to a string.
         *
         * @param media the media type to serialize
         * @return the string representation of the media type
         */
        public static @NotNull String serialize(@NotNull MediaType<?> media) {
            @NotNull StringBuilder builder = new StringBuilder();
            builder.append(media.getType());

            for (@NotNull Parameter parameter : media.getParameters()) {
                builder.append("; ").append(parameter);
            }

            return builder.toString();
        }

        /**
         * Deserializes the given string into a media type.
         *
         * @param string the string to deserialize
         * @return the deserialized media type
         * @throws ParseException if an error occurs during deserialization
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public static @NotNull MediaType<?> deserialize(@NotNull String string) throws ParseException {
            // todo: improve this (low performance)
            @NotNull Pattern pattern = Pattern.compile("([\\w-]+\\s*=\\s*[^;]+)|(^[^;]+)");
            @NotNull Matcher matcher = pattern.matcher(string);

            @Nullable Type type = null;
            @Nullable MediaParser<?> parser = null;

            @NotNull List<Parameter> parameters = new LinkedList<>();

            try {
                while (matcher.find()) {
                    if (matcher.group(2) != null) {
                        type = Type.parse(matcher.group(2));
                    } else {
                        @NotNull String[] split = matcher.group(1).split("\\s*=\\s*");
                        parameters.add(new Parameter(split[0], split[1]));
                    }
                }
            } catch (@NotNull Throwable throwable) {
                throw new ParseException("cannot parse media type '" + string + "': " + throwable.getMessage(), -1);
            }

            if (type == null) {
                throw new ParseException("cannot obtain name in media type text '" + string + "'", -1);
            }

            // Get parser
            @NotNull Optional<MediaType<?>> optional = MediaType.retrieve(type);
            if (optional.isPresent()) parser = optional.get().getParser();

            // Finish
            if (parser != null) {
                return new MediaType(type, parser, parameters.toArray(new Parameter[0])) {};
            } else {
                return new MediaType(type, MediaParser.getDefault(), parameters.toArray(new Parameter[0])) {};
            }
        }

        /**
         * Validates whether the given string is a valid media type.
         *
         * @param string the string to validate
         * @return true if the string is a valid media type, false otherwise
         */
        public static boolean validate(@NotNull String string) {
            try {
                deserialize(string);
                return true;
            } catch (@NotNull Throwable throwable) {
                return false;
            }
        }

    }

    /**
     * Represents the type of media type, consisting of a type and an optional subtype.
     *
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    public static final class Type implements CharSequence {

        // Static initializers

        /**
         * Parses a string into a media type {@link Type}.
         *
         * @param string the string to parse
         * @return the parsed media type {@link Type}
         */
        public static @NotNull Type parse(@NotNull String string) {
            @NotNull String[] split = string.split("/", 2);

            if (split.length == 2) {
                return new Type(split[0], split[1]);
            } else if (split.length == 1) {
                return new Type(split[0], null);
            } else {
                throw new IllegalArgumentException("cannot parse '" + string + "' as a valid media type");
            }
        }

        // Object

        private final @NotNull String type;
        private final @Nullable String subtype;

        public Type(@NotNull String type, @Nullable String subtype) {
            this.type = type;
            this.subtype = subtype;

            if ((type.contains(";") || type.contains(",")) || (subtype != null && (subtype.contains(";") || subtype.contains(",")))) {
                throw new IllegalArgumentException("type or subtype with illegal characters");
            }
        }

        // Getters

        /**
         * Returns the type of this media type {@link Type}.
         *
         * @return the type of this media type {@link Type}
         */
        public @NotNull String getType() {
            return type;
        }

        /**
         * Returns the subtype of this media type {@link Type}.
         *
         * @return the subtype of this media type {@link Type}, or null if none
         */
        public @Nullable String getSubType() {
            return subtype;
        }

        /**
         * Determines whether this media type {@link Type} is multipart.
         *
         * @return true if this media type {@link Type} is multipart, false otherwise
         */
        public boolean isMultipart() {
            return getType().equalsIgnoreCase("multipart");
        }

        // Implementations

        @Override
        public int length() {
            return toString().length();
        }
        @Override
        public char charAt(int index) {
            return toString().charAt(index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return toString().subSequence(start, end);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Type type = (Type) o;
            return this.type.equalsIgnoreCase(type.type) && (subtype != null && type.subtype != null && subtype.equalsIgnoreCase(type.subtype));
        }
        @Override
        public int hashCode() {
            return Objects.hash(type.toLowerCase(), (subtype != null ? subtype.toLowerCase() : null));
        }
        @Override
        public @NotNull String toString() {
            return type + (subtype != null ? "/" + subtype : "");
        }

    }

    /**
     * Represents a parameter of a media type, consisting of a key-value pair.
     *
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    public static final class Parameter {

        private final @NotNull String key;
        private final @NotNull String value;

        public Parameter(@NotNull String key, @NotNull String value) {
            this.key = key;
            this.value = value;

            if (key.contains(";") || key.contains(",") || value.contains(";") || value.contains(",")) {
                throw new IllegalArgumentException("content type parameter key or value with illegal characters");
            }
        }

        // Getters

        /**
         * Returns the key of this parameter.
         *
         * @return the key of this parameter
         */
        public @NotNull String getKey() {
            return key;
        }

        /**
         * Returns the value of this parameter.
         *
         * @return the value of this parameter
         */
        public @NotNull String getValue() {
            return value;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            @NotNull Parameter parameter = (Parameter) o;
            return Objects.equals(getKey(), parameter.getKey()) && Objects.equals(getValue(), parameter.getValue());
        }
        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getValue());
        }

        @Override
        public @NotNull String toString() {
            return getKey() + "=" + getValue();
        }

    }

    /**
     * Represents a boundary parameter for multipart media types.
     *
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    @ApiStatus.Experimental
    public static final class Boundary implements CharSequence {

        private final @NotNull String name;

        public Boundary(@NotNull String name) {
            this.name = name;
        }

        /**
         * Returns the name of this boundary.
         *
         * @return the name of this boundary
         */
        public @NotNull String getName() {
            return name;
        }

        // Implementations

        @Override
        public int length() {
            return name.length();
        }
        @Override
        public char charAt(int index) {
            return name.charAt(index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return name.subSequence(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Boundary boundary = (Boundary) o;
            return Objects.equals(name, boundary.name);
        }
        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public @NotNull String toString() {
            return name;
        }
    }

}
