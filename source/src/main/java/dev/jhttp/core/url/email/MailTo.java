package dev.jhttp.core.url.email;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;

public interface MailTo {

    // Static initializers

    static @NotNull MailTo create(
            final @Nullable String subject,
            final @Nullable String body,

            final @NotNull Email @NotNull ... emails
    ) {
        return create(subject, body, null, null, emails);
    }

    static @NotNull MailTo create(
            final @Nullable String subject,
            final @Nullable String body,
            final @Nullable Email copy,
            final @Nullable Email hiddenCopy,

            final @NotNull Email @NotNull ... emails
    ) {
        return new MailTo() {
            @Override
            public @NotNull Email @NotNull [] getEmails() {
                return emails;
            }
            @Override
            public @Nullable String getSubject() {
                return subject;
            }
            @Override
            public @Nullable String getBody() {
                return body;
            }
            @Override
            public @Nullable Email getCopy() {
                return copy;
            }
            @Override
            public @Nullable Email getHiddenCopy() {
                return hiddenCopy;
            }

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                @NotNull MailTo mail = (MailTo) object;
                return Arrays.equals(getEmails(), mail.getEmails()) && Objects.equals(getSubject(), mail.getSubject()) && Objects.equals(getBody(), mail.getBody()) && Objects.equals(getCopy(), mail.getCopy()) && Objects.equals(getHiddenCopy(), mail.getHiddenCopy());
            }
            @Override
            public int hashCode() {
                return Objects.hash(Arrays.hashCode(getEmails()), getSubject(), getBody(), getCopy(), getHiddenCopy());
            }

            @Override
            public @NotNull String toString() {
                return Parser.serialize(this);
            }

        };
    }

    // Getters

    @NotNull Email @NotNull [] getEmails();
    @Nullable String getSubject();
    @Nullable String getBody();

    @Nullable Email getCopy();
    @Nullable Email getHiddenCopy();

    // Classes

    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        public static @NotNull String serialize(@NotNull MailTo to) {
            @NotNull StringBuilder builder = new StringBuilder("mailto:");

            // Emails
            for (@NotNull Email email : to.getEmails()) {
                if (builder.length() > 7) builder.append(",");
                builder.append(email);
            }

            builder.append("?");

            // Subject and body
            try {
                // Subject
                if (to.getSubject() != null) {
                    builder.append("?subject=").append(URLEncoder.encode(to.getSubject(), "UTF-8"));
                }

                // Body
                if (to.getSubject() != null) {
                    builder.append("?body=").append(URLEncoder.encode(to.getSubject(), "UTF-8"));
                }
            } catch (@NotNull UnsupportedEncodingException e) {
                throw new RuntimeException("cannot detect UTF-8 charset", e);
            }

            // Copies
            if (to.getCopy() != null) {
                builder.append("?cc=").append(to.getCopy());
            } if (to.getHiddenCopy() != null) {
                builder.append("?bcc=").append(to.getHiddenCopy());
            }

            // Finish
            return builder.toString();
        }

        public static @NotNull MailTo deserialize(@NotNull String string) throws ParseException {
            // todo: MailTo parser
            throw new UnsupportedOperationException("not supported yet by jhttp");
        }

        public static boolean validate(@NotNull String string) {
            if (!string.startsWith("mailto:")) {
                return false;
            }

            try {
                @NotNull String decoded = URLDecoder.decode(string, "UTF-8");

                if (!string.equals(decoded) || URLEncoder.encode(decoded, "UTF-8").equals(string)) {
                    return true;
                }
            } catch (@NotNull UnsupportedEncodingException | @NotNull IllegalArgumentException ignore) {
            }

            return false;
        }

    }

}
