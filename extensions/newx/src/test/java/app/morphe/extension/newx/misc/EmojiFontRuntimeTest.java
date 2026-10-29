package app.morphe.extension.newx.misc;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.ReplacementSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Map;

import app.morphe.extension.crimera.settings.SettingsRegistry;
import app.morphe.extension.shared.settings.BooleanSetting;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {29, 34}, manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class EmojiFontRuntimeTest {
    // Reproduces fonts accepted by the picker but failing during Android loading or rendering.
    @Test public void importedAppleFontsLoadAndRenderThroughTheExtension() throws Exception {
        Class<?> utils = Class.forName("app.morphe.extension.shared.Utils");
        for (Field field : utils.getDeclaredFields()) {
            if (field.getType() == android.content.Context.class
                    && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                field.set(null, RuntimeEnvironment.getApplication());
            }
        }
        String id = "newx.content.custom_emoji_font.enabled";
        Field settings = SettingsRegistry.class.getDeclaredField("SETTINGS");
        settings.setAccessible(true);
        BooleanSetting enabled = new BooleanSetting(id, true, false);
        ((Map) settings.get(null)).put(id, enabled);
        enabled.save(true);

        String input = "Hello 😀 ❤️ 👨‍👩‍👧‍👦 🇰🇷 👍🏽";
        for (String name : new String[]{"AppleColorEmoji-Linux.ttf", "iOS-Emoji-Android.ttf"}) {
            File source = new File(System.getenv("EMOJI_FONT_DIR"), name);
            assertTrue(source.toString(), source.isFile());
            File installed = new File(RuntimeEnvironment.getApplication().getFilesDir(),
                    UpdateFont.EMOJI_FONT_FILE_NAME);
            java.nio.file.Files.copy(source.toPath(), installed.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            UpdateFont.loadFont(UpdateFont.EMOJI_FONT_FILE_NAME, true);
            CharSequence processed = UpdateFont.processComposeEmoji(input);
            assertTrue(name, processed instanceof Spannable);
            Spannable text = (Spannable) processed;
            ReplacementSpan[] spans = text.getSpans(0, text.length(), ReplacementSpan.class);
            assertEquals(name, 5, spans.length);
            Bitmap bitmap = Bitmap.createBitmap(900, 160, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setTextSize(64);
            float x = 10;
            for (ReplacementSpan span : spans) {
                int start = text.getSpanStart(span), end = text.getSpanEnd(span);
                int width = span.getSize(paint, text, start, end, null);
                assertTrue(name + ": " + text.subSequence(start, end), width > 0);
                span.draw(canvas, text, start, end, x, 0, 100, 150, paint);
                x += width + 15;
            }
            int colored = 0;
            for (int y = 0; y < bitmap.getHeight(); y++) {
                for (int px = 0; px < bitmap.getWidth(); px++) {
                    int rgb = bitmap.getPixel(px, y);
                    if (Color.red(rgb) != Color.green(rgb) || Color.green(rgb) != Color.blue(rgb)) colored++;
                }
            }
            assertTrue(name + " rendered no color pixels", colored > 100);
            File out = new File(System.getenv("EMOJI_FONT_DIR"),
                    name + "-api" + android.os.Build.VERSION.SDK_INT + ".png");
            try (FileOutputStream stream = new FileOutputStream(out)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }
            System.out.println(name + ": color pixels=" + colored + ", spans=" + spans.length);
        }
    }
}
