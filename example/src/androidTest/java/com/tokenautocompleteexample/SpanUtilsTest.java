package com.tokenautocompleteexample;

import android.text.Spanned;
import android.text.TextPaint;

import com.tokenautocomplete.SpanUtils;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class SpanUtilsTest {

    @Test
    public void testSpanUtilsHandlesNonSpannableTextUtilsResponse() {
        TextPaint dummy = new TextPaint();
        //this used to crash, so we're making sure it runs and returns null
        Spanned ellipsized = SpanUtils.ellipsizeWithSpans(null,
                null, 0, dummy, "", 150);
        assertNull(ellipsized);
    }
}
