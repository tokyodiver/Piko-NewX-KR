package app.morphe.extension.newx.theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import app.morphe.extension.newx.ui.Theme;

/** Touch a color plane, then use the rainbow strip to choose its hue. */
final class ColorPickerView extends View {
    interface Listener { void onColor(int color); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] hsv = new float[3];
    private final float inset;
    private Listener listener;
    private boolean draggingHue;

    ColorPickerView(Context context, int initial) {
        super(context);
        inset = Theme.dpToPx(context, 10f);
        setColor(initial);
    }

    void setListener(Listener listener) { this.listener = listener; }

    void setColor(int color) {
        Color.colorToHSV(color, hsv);
        setContentDescription(String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF));
        invalidate();
    }

    private float planeBottom() { return getHeight() - inset * 6; }
    private float hueTop() { return getHeight() - inset * 3; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = inset, right = getWidth() - inset;
        if (right <= left || planeBottom() <= inset) return;
        int pureHue = Color.HSVToColor(new float[]{hsv[0], 1f, 1f});
        paint.setShader(new LinearGradient(left, 0, right, 0,
                Color.WHITE, pureHue, Shader.TileMode.CLAMP));
        canvas.drawRect(left, inset, right, planeBottom(), paint);
        paint.setShader(new LinearGradient(0, inset, 0, planeBottom(),
                Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP));
        canvas.drawRect(left, inset, right, planeBottom(), paint);
        int[] rainbow = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
                Color.BLUE, Color.MAGENTA, Color.RED};
        paint.setShader(new LinearGradient(left, 0, right, 0,
                rainbow, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(left, hueTop(), right, getHeight() - inset,
                inset, inset, paint);
        paint.setShader(null);
        marker(canvas, left + hsv[1] * (right - left),
                inset + (1f - hsv[2]) * (planeBottom() - inset));
        marker(canvas, left + hsv[0] / 360f * (right - left), getHeight() - inset * 2);
    }

    private void marker(Canvas canvas, float x, float y) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Theme.dpToPx(getContext(), 4f));
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x, y, inset * 0.7f, paint);
        paint.setStrokeWidth(Theme.dpToPx(getContext(), 2f));
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, inset * 0.7f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= inset * 2 || planeBottom() <= inset) return false;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (event.getY() > planeBottom() && event.getY() < hueTop()) return false;
            draggingHue = event.getY() >= hueTop();
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            getParent().requestDisallowInterceptTouchEvent(false);
            return true;
        } else if (action != MotionEvent.ACTION_MOVE && action != MotionEvent.ACTION_UP) {
            return false;
        }
        float x = clamp((event.getX() - inset) / (getWidth() - inset * 2));
        if (draggingHue) hsv[0] = x * 360f;
        else {
            hsv[1] = x;
            hsv[2] = 1f - clamp((event.getY() - inset) / (planeBottom() - inset));
        }
        int color = Color.HSVToColor(hsv);
        setContentDescription(String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF));
        invalidate();
        if (listener != null) listener.onColor(color);
        if (action == MotionEvent.ACTION_UP) {
            getParent().requestDisallowInterceptTouchEvent(false);
            performClick();
        }
        return true;
    }

    @Override public boolean performClick() { super.performClick(); return true; }
    private static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
}
