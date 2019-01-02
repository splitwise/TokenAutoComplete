package com.tokenautocomplete;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.Locale;

/**
 * Span that displays +[x]
 *
 * Created on 2/3/15.
 * @author mgod
 */

class CountSpan extends CharacterStyle {
    private String countText;

    CountSpan(int count) {
        super();
        setCount(count);
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        //Do nothing, we are using this span as a location marker
    }

    void setCount(int c) {
        countText = String.format(Locale.getDefault(), "+%d", c);
    }

    String getCountText() {
        return countText;
    }
}
