package com.tokenautocompleteexample;

import android.content.Context;
import android.graphics.Paint;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tokenautocomplete.ViewSpan;

import org.junit.Before;
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

    private TestLayout layout;
    private Context context;

    private static class TestLayout implements ViewSpan.Layout {
        int width = 100;

        @Override
        public int getMaxViewSpanWidth() {
            return width;
        }
    }

    @Rule
    public ActivityTestRule<TokenActivity> activityRule = new ActivityTestRule<>(
            TokenActivity.class);

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        layout = new TestLayout();
    }

    @Test
    public void correctLineHeightWithBaseline() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("A person's name");

        ViewSpan span = new ViewSpan(textView, layout);
        Paint paint = new Paint();
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int width = span.getSize(paint, "", 0, 0, fontMetricsInt);
        assertEquals(width, textView.getRight());
        assertEquals(textView.getHeight() - textView.getBaseline(), fontMetricsInt.bottom);
        assertEquals(-textView.getBaseline(), fontMetricsInt.top);
    }

    @Test
    public void correctLineHeightWithoutBaseline() {
        View view = new View(context);
        view.setMinimumHeight(1000);
        view.setMinimumWidth(1000);

        ViewSpan span = new ViewSpan(view, layout);
        Paint paint = new Paint();
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int width = span.getSize(paint, "", 0, 0, fontMetricsInt);
        assertEquals(100, width);
        assertEquals(0, fontMetricsInt.bottom);
        assertEquals(-view.getHeight(), fontMetricsInt.top);
    }

    @Test
    public void usesIntrisicLayoutParametersWhenAllowedZeroWidth() {
        View view = new View(context);
        view.setMinimumHeight(1000);
        view.setMinimumWidth(1000);

        layout.width = 0;
        ViewSpan span = new ViewSpan(view, layout);
        Paint paint = new Paint();
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int width = span.getSize(paint, "", 0, 0, fontMetricsInt);
        assertEquals(1000, width);
        assertEquals(0, fontMetricsInt.bottom);
        assertEquals(-view.getHeight(), fontMetricsInt.top);
    }
}