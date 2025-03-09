package dev.jhttp.utilities;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }

    public static final class RFC822 {

        private RFC822() {
            throw new UnsupportedOperationException("this class cannot be instantiated");
        }

        /**
         * Converts the provided {@link OffsetDateTime} to a {@link String} formatted according to RFC 822.
         * <p>
         * RFC 822 defines a standard for the format of ARPA Internet text messages, which includes a date and time format
         * commonly used in email headers and HTTP headers. This method ensures that the date and time are represented in
         * the GMT time zone and formatted in a way that is compliant with the RFC 822 standard.
         * </p>
         *
         * <p>
         * Example output: {@code Wed, 12 Feb 1997 16:29:51 +0000}
         * </p>
         *
         * @param dateTime the {@link OffsetDateTime} to be converted, representing a specific moment on the timeline
         *                 (typically a timestamp from the system clock)
         * @return a {@link String} representing the date and time of the provided {@link OffsetDateTime}, formatted according to RFC 822
         * @throws NullPointerException if the {@code dateTime} is {@code null}
         * @since 0.1
         */
        public static @NotNull String convert(@NotNull OffsetDateTime dateTime) throws NullPointerException {
            @NotNull SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            return dateFormat.format(new Date(dateTime.toInstant().toEpochMilli()));
        }

        /**
         * Converts an RFC 822 date string to an {@link OffsetDateTime}.
         * <p>
         * The RFC 822 date format is commonly used in HTTP headers and email messages.
         * This method parses the date string and converts it to an {@link OffsetDateTime}.
         * </p>
         *
         * @param date the RFC 822 date string
         * @return an {@link OffsetDateTime} representing the date and time
         * @throws DateTimeParseException if the date string cannot be parsed
         * @throws IllegalArgumentException if the input is null or empty
         */
        public static @NotNull OffsetDateTime convert(@NotNull String date) throws DateTimeParseException, IllegalArgumentException {
            try {
                @NotNull SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                return Instant.ofEpochMilli(dateFormat.parse(date).getTime()).atOffset(ZoneOffset.UTC);
            } catch (ParseException e) {
                throw new DateTimeParseException(e.getMessage(), date, e.getErrorOffset());
            }
        }

    }

}
