package app.morphe.extension.newx.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Pure JVM tests for the light/dark palette recoloring math. */
public final class ColorSeedTransformTest {
    @Test public void accentActionTonesKeepChosenColorInsteadOfMutedSystemPalette() {
        int chosen = 0xFFFF3070;
        for (int tone : new int[]{40, 50, 60, 70, 80}) {
            assertEquals(chosen, ColorSeedTransform.accentTone(chosen, tone));
        }
        assertEquals(0xFF000000, ColorSeedTransform.accentTone(chosen, 0));
        assertEquals(0xFFFFFFFF, ColorSeedTransform.accentTone(chosen, 100));
    }

    @Test public void preservesAlpha() {
        int result = ColorSeedTransform.transform(0x7F9882AC, 0xFF6750A4, 0xFFD88CB5);
        assertEquals(0x7F, result >>> 24);
    }

    @Test public void retainsNeutralWhiteAndBlack() {
        assertEquals(0xFFFFFFFF,
                ColorSeedTransform.transform(0xFFFFFFFF, 0xFF6750A4, 0xFFD88CB5));
        assertEquals(0xFF000000,
                ColorSeedTransform.transform(0xFF000000, 0xFF6750A4, 0xFFD88CB5));
    }

    @Test public void convertsOriginalAccentToTheNewHue() {
        int source = 0xFF6750A4;
        int changed = ColorSeedTransform.transform(source, source, 0xFFD88CB5);
        assertNotEquals(source, changed);
        assertEquals(0xFF, changed >>> 24);
    }

    @Test public void selectsReadableForegroundForPrimary() {
        assertEquals(0xFF000000, ColorSeedTransform.foregroundOn(0xFFFFFFFF));
        assertEquals(0xFFFFFFFF, ColorSeedTransform.foregroundOn(0xFF000000));
        assertTrue(ColorSeedTransform.contrastRatio(0xFFD88CB5,
                ColorSeedTransform.foregroundOn(0xFFD88CB5)) >= 4.5f);
    }
}
