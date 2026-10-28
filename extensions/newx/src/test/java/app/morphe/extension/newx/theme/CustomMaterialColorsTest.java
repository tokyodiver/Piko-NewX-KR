package app.morphe.extension.newx.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
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

    @Test public void recentColorsKeepOnlyFiveDistinctValidValues() {
        assertArrayEquals(new String[]{"#AABBCC", "#112233", "#223344", "#334455", "#445566"},
                CustomMaterialColors.recentColors("bad,aabbcc,#AABBCC,#112233,#223344,#334455,#445566,#556677"));
        assertArrayEquals(new String[0], CustomMaterialColors.recentColors(""));
    }

    @Test public void confirmingAColorMovesItToFrontWithoutDuplicates() {
        String history = "#111111,#222222,#333333,#444444,#555555";
        assertEquals("#333333,#111111,#222222,#444444,#555555",
                CustomMaterialColors.rememberColor(history, "333333"));
        assertEquals("#ABCDEF,#111111,#222222,#333333,#444444",
                CustomMaterialColors.rememberColor(history, "abcdef"));
        assertEquals("#123456", CustomMaterialColors.rememberColor("", "123456"));
        assertThrows(IllegalArgumentException.class,
                () -> CustomMaterialColors.rememberColor(history, "invalid"));
    }
}
