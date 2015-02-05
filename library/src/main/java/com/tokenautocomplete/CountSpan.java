package com.tokenautocomplete;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Span that displays +[x]
 *
 * Created on 2/3/15.
 * @author mgod
 */

public class CountSpan extends ViewSpan {
    public String text = "";

    public CountSpan(int count, Context ctx, int textColor, int textSize, int maxWidth) {
        super(new TextView(ctx), maxWidth);
        TextView v = (TextView)view;
        v.setTextColor(textColor);
        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setCount(count);
    }

    public void setCount(int c) {
        text = "+" + c;
        ((TextView)view).setText(text);
    }
}
