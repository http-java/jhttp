package dev.jhttp.core.header.module;

import com.google.gson.*;
import dev.jhttp.core.header.HttpHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public interface NetworkErrorLogging {

    // Static initializers

    static @NotNull NetworkErrorLogging create(
            @NotNull String group,
            @NotNull Duration age,

            @NotNull HttpHeader.Key<?>[] requests,
            @NotNull HttpHeader.Key<?>[] responses,

            boolean hasSubdomains,

            @Nullable Double successFraction,
            @Nullable Double failureFraction
    ) {
        return new NetworkErrorLogging() {
            @Override
            public @NotNull String getGroup() {
                return group;
            }
            @Override
            public @NotNull Duration getAge() {
                return age;
            }
            @Override
            public @NotNull HttpHeader.Key<?> @NotNull [] getRequests() {
                return requests;
            }
            @Override
            public @NotNull HttpHeader.Key<?> @NotNull [] getResponses() {
                return responses;
            }
            @Override
            public boolean hasSubdomains() {
                return hasSubdomains;
            }

            @Override
            public @Nullable Double getSuccessFraction() {
                return successFraction;
            }
            @Override
            public @Nullable Double getFailureFraction() {
                return failureFraction;
            }

            // Implementations

            @Override
            public boolean equals(@Nullable Object object) {
                if (this == object) return true;
                if (!(object instanceof NetworkErrorLogging)) return false;
                @NotNull NetworkErrorLogging that = (NetworkErrorLogging) object;
                return Objects.equals(getGroup(), that.getGroup()) && Objects.equals(getAge(), that.getAge()) && Arrays.equals(getRequests(), that.getRequests()) && Arrays.equals(getResponses(), that.getResponses()) && hasSubdomains() == that.hasSubdomains() && Objects.equals(getSuccessFraction(), that.getSuccessFraction()) && Objects.equals(getFailureFraction(), that.getFailureFraction());
            }
            @Override
            public int hashCode() {
                return Objects.hash(getGroup(), getAge(), Arrays.hashCode(getRequests()), Arrays.hashCode(getResponses()), hasSubdomains(), getSuccessFraction(), getFailureFraction());
            }

        };
    }

    // Object

    @NotNull String getGroup();
    @NotNull Duration getAge();

    @NotNull HttpHeader.Key<?> @NotNull [] getRequests();
    @NotNull HttpHeader.Key<?> @NotNull [] getResponses();

    boolean hasSubdomains();

    @Nullable Double getSuccessFraction();
    @Nullable Double getFailureFraction();

    // Classes

    final class Parser {
        private Parser() {
            throw new UnsupportedOperationException();
        }

        public static @NotNull String serialize(@NotNull NetworkErrorLogging nel) {
            @NotNull JsonObject object = new JsonObject();

            object.addProperty("report_to", nel.getGroup());
            object.addProperty("max_age", nel.getAge().getSeconds());

            if (nel.hasSubdomains()) {
                object.addProperty("include_subdomains", nel.hasSubdomains());
            }

            if (nel.getSuccessFraction() != null) {
                object.addProperty("success_fraction", Math.min(1, Math.max(0, nel.getSuccessFraction())));
            } if (nel.getFailureFraction() != null) {
                object.addProperty("failure_fraction", Math.min(1, Math.max(0, nel.getFailureFraction())));
            }

            {
                @NotNull JsonArray array = new JsonArray();

                for (@NotNull HttpHeader.Key<?> header : nel.getRequests()) {
                    if (!header.getTarget().isRequests()) continue;
                    array.add(header.getName());
                }

                object.add("request_headers", array);
            }

            {
                @NotNull JsonArray array = new JsonArray();

                for (@NotNull HttpHeader.Key<?> header : nel.getResponses()) {
                    if (!header.getTarget().isResponses()) continue;
                    array.add(header.getName());
                }

                object.add("response_headers", array);
            }

            return object.toString();
        }
        public static @NotNull NetworkErrorLogging parse(@NotNull String string) throws ParseException {
            if (validate(string)) {
                @NotNull JsonObject object = JsonParser.parseString(string).getAsJsonObject();
                @NotNull String group = object.get("report_to").getAsString();
                @NotNull Duration age = Duration.ofSeconds(object.get("max_age").getAsInt());
                boolean subdomains = object.has("include_subdomains") && object.get("include_subdomains").getAsBoolean();

                @Nullable Double successFraction = object.has("success_fraction") ? object.get("success_fraction").getAsDouble() : null;
                @Nullable Double failureFraction = object.has("failure_fraction") ? object.get("failure_fraction").getAsDouble() : null;

                @NotNull List<HttpHeader.Key<?>> requests = new LinkedList<>();
                @NotNull List<HttpHeader.Key<?>> responses = new LinkedList<>();

                if (object.has("request_headers")) {
                    for (@NotNull JsonElement element : object.getAsJsonArray("request_headers")) {
                        requests.add(HttpHeader.Key.retrieve(element.getAsString()));
                    }
                }
                if (object.has("response_headers")) {
                    for (@NotNull JsonElement element : object.getAsJsonArray("response_headers")) {
                        responses.add(HttpHeader.Key.retrieve(element.getAsString()));
                    }
                }

                return create(group, age, requests.toArray(new HttpHeader.Key[0]), responses.toArray(new HttpHeader.Key[0]), subdomains, successFraction, failureFraction);
            } else {
                throw new ParseException("cannot parse '" + string + "' as a valid network error logging object", 0);
            }
        }

        public static boolean validate(@NotNull String string) {
            try {
                @NotNull JsonObject object = JsonParser.parseString(string).getAsJsonObject();

                if (!object.has("report_to") || !object.get("report_to").isJsonPrimitive()) {
                    return false;
                } else if (!object.has("max_age") || !object.get("max_age").isJsonPrimitive()) {
                    return false;
                } else if (object.has("include_subdomains") && !object.get("include_subdomains").isJsonPrimitive()) {
                    return false;
                } else if (object.has("success_fraction") && !object.get("success_fraction").isJsonPrimitive()) {
                    return false;
                } else if (object.has("failure_fraction") && !object.get("failure_fraction").isJsonPrimitive()) {
                    return false;
                } else if (object.has("request_headers") && !object.get("request_headers").isJsonArray()) {
                    return false;
                }

                return !object.has("response_headers") || object.get("response_headers").isJsonArray();
            } catch (@NotNull IllegalStateException | @NotNull JsonParseException | @NotNull UnsupportedOperationException e) {
                return false;
            }
        }

    }

}
