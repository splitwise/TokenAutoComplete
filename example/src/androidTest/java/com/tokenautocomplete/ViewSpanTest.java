package com.tokenautocomplete;

import android.content.Context;
import android.graphics.Paint;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ViewSpanTest {

    @Rule
    public ActivityTestRule<TokenActivity> activityRule = new ActivityTestRule<>(
            TokenActivity.class);

    @Test
    public void correctLineHeightWithBaseline() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        TextView textView = new TextView(appContext);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("A person's name");

        ViewSpan span = new ViewSpan(textView, 100);
        Paint paint = new Paint();
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int width = span.getSize(paint, "", 0, 0, fontMetricsInt);
        assertEquals(width, textView.getRight());
        assertEquals(textView.getHeight() - textView.getBaseline(), fontMetricsInt.bottom);
        assertEquals(-textView.getBaseline(), fontMetricsInt.top);
    }

    @Test
    public void correctLineHeightWithoutBaseline() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        View view = new View(appContext);
        view.setMinimumHeight(1000);
        view.setMinimumWidth(1000);

        ViewSpan span = new ViewSpan(view, 100);
        Paint paint = new Paint();
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int width = span.getSize(paint, "", 0, 0, fontMetricsInt);
        assertEquals(width, 100);
        assertEquals(0, fontMetricsInt.bottom);
        assertEquals(-view.getHeight(), fontMetricsInt.top);
    }
}