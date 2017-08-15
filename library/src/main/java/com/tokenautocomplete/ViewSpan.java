package com.tokenautocomplete;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.view.ViewGroup;

/**
 * Span that holds a view it draws when rendering
 *
 * Created on 2/3/15.
 * @author mgod
 *
 * Updated on 15/04/2017
 * Aleksandr Borisenko
 */
public class ViewSpan extends ReplacementSpan {

    private static final String TAG = ViewSpan.class.getSimpleName();

    private static final char SENTINEL = ',';

    protected View view;
    private int maxWidth;
    private boolean prepared;

    public ViewSpan(View view, int maxWidth) {
        super();
        this.maxWidth = maxWidth;
        this.view = view;
        this.view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        prepared = false;
    }

    private void prepView() {
        if (!prepared) {
            int widthSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

            view.measure(widthSpec, heightSpec);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            prepared = true;
        }
    }

    public int getHeight() {
        prepView();
        return view.getHeight();
    }

    @Override
    public void draw(
            @NonNull Canvas canvas, CharSequence text,
            @IntRange(from = 0) int start, @IntRange(from = 0) int end,
            float x, int top, int y, int bottom, @NonNull Paint paint) {
        prepView();
        canvas.save();
        canvas.translate(x, top);
        view.draw(canvas);
        canvas.restore();
    }

    @Override
    public int getSize(
            @NonNull Paint paint, CharSequence text,
            @IntRange(from = 0) int start, @IntRange(from = 0) int end,
            @Nullable Paint.FontMetricsInt fm) {
        prepView();
        // NOTE: only the first tag (measure) has ~2dp "padding"
        // NOTE: a string with the single tag can be trimmed up to span height when the layout is inflated
        String str = text.toString();
        str = str.substring(0, str.lastIndexOf(SENTINEL) + 1);
        if (start == 0 && str.length() > end) {
            // WORKAROUND: first measure is ignored if there are other ones
            return view.getRight();
        }
        if (fm != null) {
            final int height = view.getMeasuredHeight();
            final int top_need = height - (fm.bottom - fm.top);
            if (top_need != 0) {
                int top_patch = top_need / 2;
                fm.ascent = (fm.top -= top_patch);
                fm.descent = (fm.bottom += top_need - top_patch);
            }
        }
        return view.getRight();
    }
}
