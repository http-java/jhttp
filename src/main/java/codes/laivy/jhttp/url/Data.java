package codes.laivy.jhttp.url;

import codes.laivy.jhttp.content.MediaType;
import codes.laivy.jhttp.url.csp.CSPSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Data implements CSPSource {

    // Static initializers

    public static final @NotNull Pattern DATA_URL_PATTERN = Pattern.compile("^data:(?:(.*?)(;base64)?)?,(.*)$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(@NotNull String string) {
        if (!string.startsWith("data:")) {
            return false;
        }

        return DATA_URL_PATTERN.matcher(string).matches();
    }

    public static @NotNull Data parse(@NotNull String string) throws ParseException {
        @NotNull Matcher matcher = DATA_URL_PATTERN.matcher(string);

        if (validate(string)) {
            @Nullable MediaType type = matcher.group(1) != null ? MediaType.parse(matcher.group(1)) : null;
            boolean base64 = matcher.group(2) != null;

            byte[] data = matcher.group(3).getBytes();
            byte[] decoded = base64 ? Base64.getDecoder().decode(data) : null;

            return new Data(type, base64, decoded, data);
        } else {
            throw new ParseException("cannot parse '" + string + "' as a valid CSP data url", 0);
        }
    }

    public static @NotNull Data create(@Nullable MediaType type, byte @NotNull [] raw) {
        return new Data(type, false, null, raw);
    }
    public static @NotNull Data createBase64(@Nullable MediaType type, byte @NotNull [] raw, byte @NotNull [] decoded) {
        return new Data(type, true, decoded, raw);
    }

    // Object

    private final @Nullable MediaType mediaType;
    private final boolean base64;

    private final byte @Nullable [] decoded;
    private final byte @NotNull [] raw;

    private Data(@Nullable MediaType mediaType, boolean base64, byte @Nullable [] decoded, byte @NotNull [] raw) {
        this.mediaType = mediaType;
        this.base64 = base64;
        this.decoded = decoded;
        this.raw = raw;
    }

    // Getters

    @Override
    public @NotNull Type getType() {
        return Type.DATA;
    }

    public @Nullable MediaType getMediaType() {
        return mediaType;
    }

    public boolean isBase64() {
        return base64;
    }

    /**
     * Retrieves the decoded data. If the data is base64 encoded, it will return the decoded bytes.
     * Otherwise, it returns the raw bytes.
     *
     * @return the decoded data as a byte array.
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    public byte @NotNull [] getData() {
        return isBase64() && decoded != null ? decoded : getRawData();
    }

    /**
     * Retrieves the raw data part of the data URL without decoding.
     * If the data is base64 encoded, this method will still return the encoded data as raw bytes.
     *
     * @return the raw data as a byte array.
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    public byte @NotNull [] getRawData() {
        return raw;
    }

    // Implementations

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        @NotNull Data data = (Data) object;
        return base64 == data.base64 && Objects.equals(mediaType, data.mediaType) && Objects.deepEquals(raw, data.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mediaType, base64, Arrays.hashCode(raw));
    }

    @Override
    public @NotNull String toString() {
        @NotNull StringBuilder builder = new StringBuilder("data:");

        if (getMediaType() != null) {
            builder.append(getMediaType().toString().replaceAll("\\s*;\\s*", ";"));
        }
        if (isBase64()) {
            builder.append(";base64");
        }

        builder.append(",").append(new String(getRawData()));

        return builder.toString();
    }
}