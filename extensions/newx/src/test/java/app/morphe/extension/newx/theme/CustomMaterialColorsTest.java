package app.morphe.extension.newx.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class CustomMaterialColorsTest {
    @Test public void normalizesHexInput() {
        assertEquals("#D88CB5", CustomMaterialColors.normalizedHex(" d88cb5 "));
        assertEquals("#B48ADE", CustomMaterialColors.normalizedHex("#b48ade"));
    }

    @Test public void rejectsNonRgbHexAndMalformedInput() {
        assertNull(CustomMaterialColors.normalizedHex("#ABC"));
        assertNull(CustomMaterialColors.normalizedHex("#12345678"));
        assertNull(CustomMaterialColors.normalizedHex("not a color"));
        assertNull(CustomMaterialColors.normalizedHex(null));
    }
}
