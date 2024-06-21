package codes.laivy.jhttp.tests.content;

import codes.laivy.jhttp.content.ContentRange;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.text.ParseException;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public final class ContentRangeTests {

    private ContentRangeTests() {
    }

    private static final @NotNull String[] VALIDS = new String[] {
            "bytes */*",
            "bytes 0-50/*",
            "bytes 0-50/100"
    };

    @Test
    @Order(value = 0)
    void validate() throws ParseException {
        for (@NotNull String valid : VALIDS) {
            Assertions.assertTrue(ContentRange.Parser.validate(valid), "cannot parse '" + valid + "' as a valid content range");
            Assertions.assertEquals(ContentRange.Parser.deserialize(valid), ContentRange.Parser.deserialize(ContentRange.Parser.deserialize(valid).toString()));
        }
    }
    @Test
    @Order(value = 1)
    void assertions() throws ParseException {
        @NotNull ContentRange range = ContentRange.Parser.deserialize("bytes 0-50/100");

        Assertions.assertFalse(range.getSize().isWildcard());
        Assertions.assertEquals(range.getUnit(), "bytes");
        Assertions.assertEquals(range.getMinimum(), 0L);
        Assertions.assertEquals(range.getMaximum(), 50L);
        Assertions.assertEquals(range.getSize().getValue(), 100L);
    }

}
