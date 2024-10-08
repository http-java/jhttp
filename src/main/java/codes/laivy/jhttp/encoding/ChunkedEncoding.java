package codes.laivy.jhttp.encoding;

import codes.laivy.jhttp.encoding.ChunkedEncoding.Chunk.Extension;
import codes.laivy.jhttp.encoding.ChunkedEncoding.Chunk.Length;
import codes.laivy.jhttp.exception.encoding.EncodingException;
import codes.laivy.jhttp.utilities.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static codes.laivy.jhttp.Main.CRLF;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class ChunkedEncoding extends Encoding {

    // Static initializers

    public static @NotNull Builder builder() {
        return new Builder();
    }

    // Object

    @Range(from = 1, to = Integer.MAX_VALUE)
    private final int blockSize;

    protected ChunkedEncoding(
            @Range(from = 1, to = Integer.MAX_VALUE)
            int blockSize
    ) {
        super("chunked");
        this.blockSize = blockSize;
    }

    // Getters

    @Range(from = 1, to = Integer.MAX_VALUE)
    public int getBlockSize() {
        return blockSize;
    }

    // Modules

    protected @NotNull Extension @NotNull [] extensions(@NotNull String bytes) {
        return new Extension[0];
    }

    @Override
    public byte @NotNull [] compress(byte @NotNull [] bytes) throws EncodingException {
        // As you can see, the compression is pretty much simpler than the decompression
        // Lol the decompression method took Laivy almost 16 hours to be done :crying:

        @NotNull StringBuilder builder = new StringBuilder();

        for (byte[] block : StringUtils.explode(bytes, getBlockSize())) {
            // Length and extensions
            @NotNull Length length = new Length(block.length, extensions(new String(block, ISO_8859_1)));
            builder.append(length).append(CRLF);

            // Content
            builder.append(new String(block)).append(CRLF);
        }

        // Ending
        @NotNull Length length = new Length(0, extensions(new String(new byte[0], ISO_8859_1)));
        builder.append(length).append(CRLF).append(CRLF);

        // Finish
        return builder.toString().getBytes(ISO_8859_1);
    }
    @Override
    public byte @NotNull [] decompress(byte @NotNull [] bytes) throws EncodingException {
        // Parser functions
        @NotNull BiFunction<String, Length, Chunk> chunkParser = new BiFunction<String, Length, Chunk>() {
            @Override
            public @NotNull Chunk apply(@NotNull String string, @NotNull Length length) {
                return new Chunk(length, string);
            }
        };
        @NotNull Function<String, Length> lengthParser = new Function<String, Length>() {
            @Override
            public @NotNull Length apply(@NotNull String string) {
                @NotNull String[] data = string.split("\\s*;\\s*", 2);
                @NotNull Extension[] extensions = new Extension[0];

                if (data.length > 1) {
                    @NotNull Pattern pattern = Pattern.compile("\\s*(?<key>[^;=\\s]+)\\s*(?:=\\s*(?<value>[^;\\s]*))?\\s*");
                    @NotNull Matcher matcher = pattern.matcher(string);
                    extensions = new Extension[string.split(";").length];

                    int index = 0;
                    while (matcher.find()) {
                        @NotNull String key = matcher.group("key");
                        @Nullable String value = matcher.group("value");

                        extensions[index] = Extension.create(key, value);
                        index++;
                    }
                }

                // todo: 03/06/2024 according to Wikipedia, the length number may be a hexadecimal value
                //  https://en.wikipedia.org/wiki/Chunked_transfer_encoding#Example
                int length = Integer.parseInt(data[0].replace("\r", "").replace("\n", ""));

                return new Length(length, extensions);
            }
        };

        // Parse
        @NotNull List<Chunk> chunks = new LinkedList<>();
        @NotNull String string = new String(bytes, ISO_8859_1);

        @Nullable Length length = null;
        while (!string.isEmpty()) {
            if (length == null) {
                @NotNull String part = string.split(CRLF, 2)[0];
                length = lengthParser.apply(part);

                string = string.substring(part.length() + 2);
            } else {
                @NotNull String part = "";
                if (length.getAmount() > 0) {
                    if (length.getAmount() > string.length()) {
                        throw new IllegalStateException("chunk specified length not equals to chunk content length");
                    }

                    part = string.substring(0, length.getAmount());
                }

                chunks.add(chunkParser.apply(part, length));
                string = string.substring(part.length() + 2);

                if (length.getAmount() == 0) {
                    // End of chunks
                    break;
                }

                length = null;
            }
        }

        // Merge bytes
        byte[] decompressed = new byte[chunks.stream().mapToInt(chunk -> chunk.getContent().length()).sum()];
        int amount = 0;
        for (@NotNull Chunk chunk : chunks) {
            int bytesLength = chunk.getContent().length();
            System.arraycopy(chunk.getContent().getBytes(ISO_8859_1), 0, decompressed, amount, bytesLength);
            amount += bytesLength;
        }

        // Finish
        return decompressed;
    }

    // Classes

    public static final class Builder {

        @Range(from = 1, to = Integer.MAX_VALUE)
        private int blockSize = 4096;

        private Builder() {
        }

        @Range(from = 1, to = Integer.MAX_VALUE)
        public int blockSize() {
            return blockSize;
        }
        @Contract("_->this")
        public @NotNull Builder blockSize(
                @Range(from = 1, to = Integer.MAX_VALUE)
                int blockSize
        ) {
            this.blockSize = blockSize;
            return this;
        };

        public @NotNull ChunkedEncoding build() {
            return new ChunkedEncoding(blockSize);
        }

    }

    public static final class Chunk {

        private final @NotNull Length length;
        private final @NotNull String content;

        public Chunk(@NotNull Length length, @NotNull String content) {
            this.length = length;
            this.content = content.substring(0, length.getAmount());
        }

        // Getters

        @Contract(pure = true)
        public @NotNull Length getLength() {
            return length;
        }
        public @NotNull String getContent() {
            return content;
        }

        // Implementations

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            @NotNull Chunk chunk = (Chunk) object;
            return Objects.equals(length, chunk.length) && Objects.equals(content, chunk.content);
        }
        @Override
        public int hashCode() {
            return Objects.hash(length, content);
        }

        // Classes

        public static final class Extension implements Entry<@NotNull String, @Nullable String> {

            // Static initializers

            public static @NotNull Extension create(@NotNull String key) {
                return create(key, null);
            }
            public static @NotNull Extension create(@NotNull String key, @Nullable String value) {
                return new Extension(key, value);
            }

            // Object

            private final @NotNull String key;
            private @Nullable String value;

            private Extension(@NotNull String key, @Nullable String value) {
                this.key = key;
                this.value = value;
            }

            // Implementations

            @Override
            public @NotNull String getKey() {
                return key;
            }

            @Override
            public @Nullable String getValue() {
                return value;
            }
            @Override
            public @Nullable String setValue(@Nullable String value) {
                @Nullable String old = getValue();
                this.value = value;

                return old;
            }

            // Equals

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                Extension extension = (Extension) object;
                return Objects.equals(getKey(), extension.getKey()) && Objects.equals(getValue(), extension.getValue());
            }
            @Override
            public int hashCode() {
                return Objects.hash(getKey(), getValue());
            }
            @Override
            public @NotNull String toString() {
                return getKey() + (getValue() != null ? "=" + getValue() : "");
            }

        }
        public static final class Length implements Comparable<Integer> {

            // Static initializers

            public static @NotNull Length create(
                    @Range(from = 0, to = Integer.MAX_VALUE)
                    int amount
            ) {
                return new Length(amount, new Extension[0]);
            }
            public static @NotNull Length create(
                    @Range(from = 0, to = Integer.MAX_VALUE)
                    int amount,

                    @NotNull Extension @NotNull [] extensions
            ) {
                return new Length(amount, extensions);
            }

            // Object

            @Range(from = 0, to = Integer.MAX_VALUE)
            private final int amount;

            private final @NotNull Extension @NotNull [] extensions;

            private Length(
                    @Range(from = 0, to = Integer.MAX_VALUE)
                    int amount,

                    @NotNull Extension @NotNull [] extensions
            ) {
                this.amount = amount;
                this.extensions = extensions;
            }

            // Getters

            public int getAmount() {
                return amount;
            }
            public @NotNull Extension @NotNull [] getExtensions() {
                return extensions;
            }

            // Implementations

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                Length that = (Length) object;
                return getAmount() == that.getAmount() && Objects.deepEquals(getExtensions(), that.getExtensions());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getAmount(), Arrays.hashCode(getExtensions()));
            }

            @Override
            public @NotNull String toString() {
                @NotNull StringBuilder builder = new StringBuilder();
                builder.append(getAmount());

                for (@NotNull Extension extension : getExtensions()) {
                    builder.append(";").append(extension);
                }

                return builder.toString();
            }

            @Override
            public int compareTo(@NotNull Integer o) {
                return ((Integer) getAmount()).compareTo(o);
            }

        }
    }

}
