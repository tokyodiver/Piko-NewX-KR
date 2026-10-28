package app.morphe.extension.newx.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;

import app.morphe.extension.crimera.settings.SettingsRegistry;
import app.morphe.extension.shared.settings.Setting;
import app.morphe.extension.shared.settings.StringSetting;
import app.morphe.extension.shared.Utils;

/** A palette override applied only to existing Material You resource reads. */
public final class CustomMaterialColors {
    public static final String LIGHT_ID = "newx.theme.custom_light";
    public static final String DARK_ID = "newx.theme.custom_dark";
    public static final String ENABLED_ID = "newx.theme.custom_enabled";

    private CustomMaterialColors() { }

    public static String saved(boolean dark) {
        return SettingsRegistry.getStringOrDefault(dark ? DARK_ID : LIGHT_ID, "");
    }

    public static boolean isEnabled() {
        return SettingsRegistry.getBooleanOrDefault(ENABLED_ID, false);
    }

    public static boolean isConfigured(boolean dark) {
        return isEnabled() && normalizedHex(saved(dark)) != null;
    }

    /** Custom accents take priority without replacing the host's neutral palette. */
    public static boolean useNativePalette() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false;
        Context context = Utils.getContext();
        return context != null && isConfigured(TwitterTheme.fromContext(context).isDark());
    }

    public static Integer selectedColor(boolean dark) {
        if (!isEnabled()) return null;
        String hex = normalizedHex(saved(dark));
        return hex == null ? null : Color.parseColor(hex);
    }

    public static int onAccent(int color) { return ColorSeedTransform.foregroundOn(color); }

    public static String normalizedHex(String input) {
        if (input == null) return null;
        String hex = input.trim();
        if (!hex.startsWith("#")) hex = "#" + hex;
        if (!hex.matches("#[0-9a-fA-F]{6}")) return null;
        return hex.toUpperCase(java.util.Locale.ROOT);
    }

    public static void save(boolean dark, String value) {
        String hex = normalizedHex(value);
        if (hex == null) throw new IllegalArgumentException("Expected a six-digit RGB color");
        saveRaw(dark, hex);
    }

    public static void saveRaw(boolean dark, String value) {
        if (!SettingsRegistry.isLoaded()) SettingsRegistry.load();
        String id = dark ? DARK_ID : LIGHT_ID;
        Setting<?> setting = SettingsRegistry.settingOrNull(id);
        if (!(setting instanceof StringSetting text)) {
            throw new IllegalStateException("Missing NewX custom color setting: " + id);
        }
        text.save(value);
    }

    /** A missing seed for a particular mode retains the system colors for that mode. */
    public static int resolve(String name, int original) {
        if (!isEnabled()) return original;
        if (!name.startsWith("m3_sys_color_dynamic_")
                && !name.startsWith("material_dynamic_primary")) return original;
        boolean dark = name.startsWith("m3_sys_color_dynamic_dark_")
                || (!name.startsWith("m3_sys_color_dynamic_light_")
                        && isCurrentDark());
        String hex = normalizedHex(saved(dark));
        if (hex == null) return original;
        int chosen = Color.parseColor(hex);
        // Ordinary body text and unselected action icons keep their original neutral role.
        if (name.endsWith("_on_surface") || name.endsWith("_on_surface_variant")) return original;
        if (name.endsWith("_on_primary")) return ColorSeedTransform.foregroundOn(chosen);
        if (name.endsWith("_primary") && name.startsWith("m3_")) return chosen;
        // Composer buttons/tools use these accent ramp entries, not on-surface roles.
        switch (name) {
            case "material_dynamic_primary40":
            case "material_dynamic_primary50":
            case "material_dynamic_primary60":
            case "material_dynamic_primary70":
            case "material_dynamic_primary80":
                return (original & 0xFF000000) | (chosen & 0x00FFFFFF);
            default:
                break;
        }
        Context context = Utils.getContext();
        if (context == null) return original;
        Resources resources = context.getResources();
        String ref = dark ? "m3_sys_color_dynamic_dark_primary" :
                "m3_sys_color_dynamic_light_primary";
        int id = resources.getIdentifier(ref, "color", context.getPackageName());
        if (id == 0) return original;
        int source = resources.getColor(id, context.getTheme());
        return ColorSeedTransform.transform(original, source, chosen);
    }

    private static boolean isCurrentDark() {
        Context context = Utils.getContext();
        return context != null && TwitterTheme.fromContext(context).isDark();
    }
}
