package app.morphe.extension.newx.theme;

/**
 * Shifts an existing Material You color role to a user seed, preserving its source
 * luminance and alpha. This intentionally preserves the host palette's role structure;
 * it is not a substitute for Material Color Utilities HCT tonal palette generation.
 * Package-private and Android-free to allow tests with plain javac.
 */
final class ColorSeedTransform {
    private ColorSeedTransform() { }

    static int transform(int original, int sourcePrimary, int targetPrimary) {
        float[] source = hsl(sourcePrimary);
        float[] target = hsl(targetPrimary);
        float[] role = hsl(original);
        // Rotate the source Material You hue to the chosen seed hue. Very-low-chroma
        // neutral roles remain nearly neutral and keep their contrast-defining tone.
        float shift = ((target[0] - source[0] + 540f) % 360f) - 180f;
        float hue = (role[0] + shift + 360f) % 360f;
        float satRatio = source[1] < 0.02f ? 1f : target[1] / source[1];
        float sat = Math.max(0f, Math.min(1f, role[1] * satRatio));
        return ((original >>> 24) << 24) | (rgb(hue, sat, role[2]) & 0x00FFFFFF);
    }

    static int foregroundOn(int bg) {
        return contrastRatio(bg, 0xFF000000) >= contrastRatio(bg, 0xFFFFFFFF)
                ? 0xFF000000 : 0xFFFFFFFF;
    }

    static float contrastRatio(int left, int right) {
        double a = luminance(left), b = luminance(right);
        return (float) ((Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05));
    }

    private static double luminance(int argb) {
        double r = linear((argb >>> 16) & 255), g = linear((argb >>> 8) & 255);
        double b = linear(argb & 255);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double linear(int value) {
        double c = value / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static float[] hsl(int c) {
        float r = ((c >>> 16) & 255) / 255f, g = ((c >>> 8) & 255) / 255f;
        float b = (c & 255) / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float d = max - min, l = (max + min) / 2f;
        if (d == 0) return new float[]{0f, 0f, l};
        float s = d / (1f - Math.abs(2f * l - 1f));
        float h = max == r ? ((g - b) / d) % 6f :
                max == g ? (b - r) / d + 2f : (r - g) / d + 4f;
        h = ((h * 60f) % 360f + 360f) % 360f;
        return new float[]{h, s, l};
    }

    private static int rgb(float hue, float sat, float light) {
        float c = (1f - Math.abs(2f * light - 1f)) * sat;
        float x = c * (1f - Math.abs((hue / 60f) % 2f - 1f));
        float m = light - c / 2f;
        float r, g, b;
        if (hue < 60f) { r = c; g = x; b = 0; }
        else if (hue < 120f) { r = x; g = c; b = 0; }
        else if (hue < 180f) { r = 0; g = c; b = x; }
        else if (hue < 240f) { r = 0; g = x; b = c; }
        else if (hue < 300f) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        int ri = Math.max(0, Math.min(255, Math.round((r + m) * 255f)));
        int gi = Math.max(0, Math.min(255, Math.round((g + m) * 255f)));
        int bi = Math.max(0, Math.min(255, Math.round((b + m) * 255f)));
        return (ri << 16) | (gi << 8) | bi;
    }
}
