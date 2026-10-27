package app.morphe.extension.newx.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import app.morphe.extension.crimera.settings.CustomScreenFragment;
import app.morphe.extension.crimera.settings.SettingsUi;
import app.morphe.extension.newx.settings.NewXSettingsActivity;
import app.morphe.extension.newx.ui.ListItem;
import app.morphe.extension.newx.ui.Theme;
import app.morphe.extension.shared.StringRef;

/** Edits the two registry-backed custom Material You color seeds. */
public final class CustomThemeFragment extends CustomScreenFragment {
    private ListItem lightRow;
    private ListItem darkRow;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Context context = requireContext();
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(SettingsUi.backgroundColor(context));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, Theme.dpToPx(context, 16f), 0, Theme.dpToPx(context, 32f));
        scroll.addView(content);

        TextView description = SettingsUi.summaryText(context);
        description.setText(str("piko_newx_custom_theme_explain"));
        description.setPadding(Theme.dpToPx(context, 20f), Theme.dpToPx(context, 8f),
                Theme.dpToPx(context, 20f), Theme.dpToPx(context, 16f));
        content.addView(description);

        lightRow = new ListItem(context);
        lightRow.setTitle(str("piko_newx_custom_theme_pick_light"));
        lightRow.setOnClickListener(view -> showPicker(false));
        content.addView(lightRow, new LinearLayout.LayoutParams(-1, -2));

        darkRow = new ListItem(context);
        darkRow.setTitle(str("piko_newx_custom_theme_pick_dark"));
        darkRow.setOnClickListener(view -> showPicker(true));
        content.addView(darkRow, new LinearLayout.LayoutParams(-1, -2));

        TextView reset = SettingsUi.summaryText(context);
        reset.setText(str("piko_newx_custom_theme_reset"));
        reset.setPadding(Theme.dpToPx(context, 20f), Theme.dpToPx(context, 24f),
                Theme.dpToPx(context, 20f), Theme.dpToPx(context, 24f));
        reset.setClickable(true);
        reset.setOnClickListener(view -> confirmReset());
        content.addView(reset);
        refresh();
        return scroll;
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof NewXSettingsActivity settings) {
            settings.setPageTitle(str("piko_newx_custom_theme_colors_title"));
        }
        refresh();
    }

    private void refresh() {
        if (lightRow != null) lightRow.setSubtitle(subtitle(false));
        if (darkRow != null) darkRow.setSubtitle(subtitle(true));
    }

    private String subtitle(boolean dark) {
        String hex = CustomMaterialColors.normalizedHex(CustomMaterialColors.saved(dark));
        return hex == null ? str("piko_newx_custom_theme_system") : hex;
    }

    private void confirmReset() {
        Activity activity = getActivity();
        if (activity == null) return;
        new AlertDialog.Builder(activity)
                .setTitle(str("piko_newx_custom_theme_reset"))
                .setMessage(str("piko_newx_custom_theme_reset_confirm"))
                .setNegativeButton(str("piko_newx_settings_cancel"), null)
                .setPositiveButton(str("piko_newx_settings_ok"), (dialog, which) -> {
                    CustomMaterialColors.saveRaw(false, "");
                    CustomMaterialColors.saveRaw(true, "");
                    refresh();
                    toast("piko_newx_custom_theme_reset_done");
                })
                .show();
    }

    private void showPicker(boolean dark) {
        Activity activity = getActivity();
        if (activity == null) return;
        Context context = activity;
        String previous = CustomMaterialColors.normalizedHex(CustomMaterialColors.saved(dark));
        if (previous == null) previous = defaultSeed(context, dark);
        int initial = Color.parseColor(previous);

        int pad = Theme.dpToPx(context, 20f);
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(pad, pad, pad, Theme.dpToPx(context, 8f));

        TextView swatch = new TextView(context);
        swatch.setGravity(Gravity.CENTER);
        swatch.setText(str("piko_newx_custom_theme_preview"));
        GradientDrawable swatchBackground = new GradientDrawable();
        swatchBackground.setCornerRadius(Theme.dpToPx(context, 12f));
        swatchBackground.setColor(initial);
        swatch.setBackground(swatchBackground);
        swatch.setTextColor(ColorSeedTransform.foregroundOn(initial));
        form.addView(swatch, new LinearLayout.LayoutParams(-1, Theme.dpToPx(context, 56f)));

        TextView presetsTitle = SettingsUi.summaryText(context);
        presetsTitle.setText(str("piko_newx_custom_theme_presets"));
        presetsTitle.setPadding(0, pad / 2, 0, 0);
        form.addView(presetsTitle);

        EditText hex = new EditText(context);
        hex.setSingleLine(true);
        hex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        hex.setHint(str("piko_newx_custom_theme_hex_hint"));
        hex.setText(previous);
        hex.setVisibility(View.GONE);
        boolean[] updating = {false};
        ColorPickerView picker = new ColorPickerView(context, initial);
        java.util.function.IntConsumer selectColor = rgb -> {
            if (updating[0]) return;
            updating[0] = true;
            picker.setColor(rgb);
            swatchBackground.setColor(rgb);
            swatch.setTextColor(ColorSeedTransform.foregroundOn(rgb));
            hex.setText(String.format(java.util.Locale.ROOT, "#%06X", rgb & 0xFFFFFF));
            updating[0] = false;
        };
        // A finger drag already owns the HSV state: do not round-trip it through RGB.
        picker.setListener(rgb -> {
            if (updating[0]) return;
            updating[0] = true;
            swatchBackground.setColor(rgb);
            swatch.setTextColor(ColorSeedTransform.foregroundOn(rgb));
            hex.setText(String.format(java.util.Locale.ROOT, "#%06X", rgb & 0xFFFFFF));
            updating[0] = false;
        });
        int[] presets = {0xFF1D9BF0, 0xFF536DFE, 0xFF8B5CF6, 0xFFE879F9, 0xFFF43F5E,
                0xFFFF7A00, 0xFFF5C542, 0xFF22C55E, 0xFF14B8A6, 0xFF94A3B8};
        for (int row = 0; row < 2; row++) {
            LinearLayout palette = new LinearLayout(context);
            for (int column = 0; column < 5; column++) {
                int rgb = presets[row * 5 + column];
                FrameLayout target = new FrameLayout(context);
                target.setContentDescription(String.format(java.util.Locale.ROOT, "#%06X", rgb & 0xFFFFFF));
                target.setFocusable(true);
                target.setOnClickListener(view -> selectColor.accept(rgb));
                View dot = new View(context);
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(rgb);
                circle.setStroke(Theme.dpToPx(context, 1f), 0x55808080);
                dot.setBackground(circle);
                int size = Theme.dpToPx(context, 30f);
                target.addView(dot, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
                palette.addView(target, new LinearLayout.LayoutParams(0, Theme.dpToPx(context, 48f), 1f));
            }
            form.addView(palette);
        }
        TextView help = SettingsUi.summaryText(context);
        help.setText(str("piko_newx_custom_theme_picker_help"));
        form.addView(help);
        form.addView(picker, new LinearLayout.LayoutParams(-1, Theme.dpToPx(context, 260f)));

        TextView advanced = SettingsUi.summaryText(context);
        advanced.setText(str("piko_newx_custom_theme_hex_toggle"));
        advanced.setPadding(0, pad / 2, 0, pad / 2);
        advanced.setFocusable(true);
        advanced.setOnClickListener(view -> hex.setVisibility(
                hex.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        form.addView(advanced);
        form.addView(hex, new LinearLayout.LayoutParams(-1, -2));
        hex.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (updating[0]) return;
                String valid = CustomMaterialColors.normalizedHex(s.toString());
                if (valid == null) return;
                int rgb = Color.parseColor(valid);
                picker.setColor(rgb);
                swatchBackground.setColor(rgb);
                swatch.setTextColor(ColorSeedTransform.foregroundOn(rgb));
            }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.addView(form);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(str(dark ? "piko_newx_custom_theme_pick_dark" :
                        "piko_newx_custom_theme_pick_light"))
                .setView(scroll)
                .setNegativeButton(str("piko_newx_settings_cancel"), null)
                .setPositiveButton(str("piko_newx_settings_ok"), null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String selected = CustomMaterialColors.normalizedHex(hex.getText().toString());
                    if (selected == null) {
                        hex.setVisibility(View.VISIBLE);
                        hex.setError(str("piko_newx_custom_theme_invalid"));
                        return;
                    }
                    CustomMaterialColors.save(dark, selected);
                    refresh();
                    toast("piko_newx_custom_theme_saved");
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private static String defaultSeed(Context context, boolean dark) {
        String name = dark ? "m3_sys_color_dynamic_dark_primary" :
                "m3_sys_color_dynamic_light_primary";
        int id = context.getResources().getIdentifier(name, "color", context.getPackageName());
        int rgb = id == 0 ? (dark ? 0xFFD0BCFF : 0xFF6750A4) :
                context.getResources().getColor(id, context.getTheme());
        return String.format(java.util.Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    private static String str(String name) { return StringRef.str(name); }

    private Context requireContext() {
        Context context = getActivity();
        if (context == null) throw new IllegalStateException("Missing settings activity");
        return context;
    }

    private void toast(String id) {
        Context context = getActivity();
        if (context != null) Toast.makeText(context, str(id), Toast.LENGTH_LONG).show();
    }
}
