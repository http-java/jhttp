package dev.jhttp.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }

    /**
     * Splits a byte array into blocks of a specified size.
     *
     * <p>This method takes a byte array and divides it into smaller arrays (blocks) of a given size.
     * The last block may be smaller if the byte array length is not a perfect multiple of the block size.
     * It ensures that each block (except possibly the last one) has the specified number of bytes.
     *
     * <p>For example, if the byte array has 10 bytes and the block size is 3, the resulting array
     * will contain four blocks: the first three with 3 bytes each, and the last one with 1 byte.
     *
     * @param bytes the byte array to be split into blocks
     * @param blockSize the size of each block
     * @return a two-dimensional byte array, where each sub-array is a block of the specified size
     * @throws IllegalArgumentException if blockSize is less than 1
     *
     * @author Daniel Meinicke
     * @since 0.1
     */
    public static byte[][] explode(byte[] bytes, int blockSize) {
        int numBlocks = (int) Math.ceil((double) bytes.length / blockSize);
        return IntStream.range(0, numBlocks).mapToObj(i -> Arrays.copyOfRange(bytes, i * blockSize, Math.min((i + 1) * blockSize, bytes.length))).toArray(byte[][]::new);
    }

    /**
     * Splits the input string by the given delimiter and includes the delimiter at the end of each split segment,
     * with a limit on the number of splits.
     *
     * <p>This method splits the input string based on the provided delimiter pattern. Unlike the standard
     * {@link String#split(String, int)} method, it includes the delimiter in the same index of the resulting array
     * where the split occurs. The number of splits is controlled by the {@code limit} parameter.
     * If {@code limit} is non-positive, the pattern will be applied as many times as possible and the resulting
     * array can have any length.
     *
     * <p>For example, splitting the string "a,b,c,d" with delimiter "," and limit 3 would result in an array:
     * ["a,", "b,", "c,d"].
     *
     * @param input the input string to be split
     * @param delimiter the delimiter pattern to split by
     * @param limit the result threshold, as described above
     * @return an array of strings, split by the delimiter, including the delimiter in the same index of each segment
     * @throws IllegalArgumentException if the input or delimiter is null
     *
     * @author Daniel Meinicke
     * @since 0.1
     */
    public static @NotNull String[] splitAndKeepDelimiter(@NotNull String input, @NotNull String delimiter, int limit) {
        @NotNull List<String> result = new LinkedList<>();
        @NotNull Pattern pattern = Pattern.compile(delimiter);
        @NotNull Matcher matcher = pattern.matcher(input);

        int lastEnd = 0;
        int splits = 0;

        while (matcher.find()) {
            if (limit > 0 && splits >= limit - 1) {
                break;
            }

            result.add(input.substring(lastEnd, matcher.end()));
            lastEnd = matcher.end();
            splits++;
        }
        if (lastEnd != input.length()) {
            result.add(input.substring(lastEnd));
        }

        return result.toArray(new String[0]);
    }

    /**
     * @param input the input string to be split
     * @param delimiter the delimiter pattern to split by
     *
     * @see #splitAndKeepDelimiter(String, String, int)
     * @return an array of strings, split by the delimiter, including the delimiter in the same index of each segment
     */
    public static @NotNull String[] splitAndKeepDelimiter(@NotNull String input, @NotNull String delimiter) {
        return splitAndKeepDelimiter(input, delimiter, 0);
    }

    /**
     * Checks if a string is blank.
     * <p>A string is considered blank if it is null, empty, or contains only whitespace characters.</p>
     *
     * Note: JHTTP was made on Java 8, so it doesn't have the String#isBlank method.
     *
     * @param str the string to check, may be null
     * @return {@code true} if the string is null, empty, or contains only whitespace characters, {@code false} otherwise
     *
     * @author Daniel Meinicke
     * @since 0.1
     */
    public static boolean isBlank(@NotNull String str) {
        return str.trim().isEmpty();
    }

    public static boolean isInteger(@NotNull String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (@NotNull NumberFormatException ignore) {
            return false;
        }
    }

    public static @NotNull Map<String, String> readQueryParams(@NotNull URI uri) {
        @NotNull Map<String, String> map = new LinkedHashMap<>();
        @Nullable String query = uri.getQuery();

        if (query == null || StringUtils.isBlank(query)) {
            return map;
        }

        for (@NotNull String parameter : query.split("&")) {
            @NotNull String[] split = parameter.split("=", 2);

            if (split.length == 2) {
                map.put(split[0], split[1]);
            } else if (split.length == 1) {
                map.put(split[0], "");
            }
        }

        return map;
    }

}
