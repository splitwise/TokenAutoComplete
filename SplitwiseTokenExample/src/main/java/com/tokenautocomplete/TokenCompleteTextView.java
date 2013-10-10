package com.tokenautocomplete;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.QwertyKeyListener;
import android.text.style.ReplacementSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gmail style auto complete view with easy token customization
 * override getViewForObject to provide your token view
 *
 * Created by mgod on 9/12/13.
 *
 * @author mgod
 */
public abstract class TokenCompleteTextView extends MultiAutoCompleteTextView {
    //When the token is deleted...
    public enum TokenDeleteStyle {
        _Parent, //...do the parent behavior, not recommended
        Clear, //...clear the underlying text
        PartialCompletion, //...return the original text used for completion
        ToString //...replace the token with toString of the token object
    }

    //When the user clicks on a token...
    public enum TokenClickStyle {
        None, //...do nothing, but make sure the cursor is not in the token
        Delete,//...delete the token
        Select//...select the token. A second click will delete it.
    }

    private Tokenizer tokenizer;
    private Object selectedObject;
    private TokenListener listener;
    private TokenSpanWatcher spanWatcher;
    private ArrayList<Object> objects;
    private TokenDeleteStyle deletionStyle = TokenDeleteStyle._Parent;
    private TokenClickStyle tokenClickStyle = TokenClickStyle.None;
    private String prefix = "";
    private boolean hintVisible = false;
    private Layout lastLayout = null;
    private boolean allowDuplicates = true;

    private void init() {
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        objects = new ArrayList<Object>();
        Editable text = getText();
        assert null != text;
        spanWatcher = new TokenSpanWatcher();
        text.setSpan(spanWatcher, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        //This handles some cases where older Android SDK versions don't send onSpanRemoved
        //Needed in 2.2, 2.3.3, 3.0
        //Not needed after 4.0
        //I haven't tested on other 3.x series SDKs
        if (Build.VERSION.SDK_INT < 14) {
            addTextChangedListener(new TokenTextWatcherAPI8());

        } else {
            addTextChangedListener(new TokenTextWatcher());
        }

        setFilters(new InputFilter[] {new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                //We need to not do anything when we would delete the prefix
                if (dstart < prefix.length() && dend == prefix.length()) {
                    return prefix.substring(dstart, dend);
                }
                return null;
            }
        }});

        //We had _Parent style during initialization to handle an edge case in the parent
        //now we can switch to Clear, usually the best choice
        setDeletionStyle(TokenDeleteStyle.Clear);
    }

    public TokenCompleteTextView(Context context) {
        super(context);
        init();
    }

    public TokenCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TokenCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void performFiltering(CharSequence text, int start, int end,
                                    int keyCode) {
        if (start < prefix.length()) {
            start = prefix.length();
        }
        getFilter().filter(text.subSequence(start, end), this);
    }


    @Override
    public void setTokenizer(Tokenizer t) {
        super.setTokenizer(t);
        tokenizer = t;
    }

    public void setDeletionStyle(TokenDeleteStyle dStyle) {
        deletionStyle = dStyle;
    }

    public void setTokenClickStyle(TokenClickStyle cStyle) {
        tokenClickStyle = cStyle;
    }

    public void setTokenListener(TokenListener l) {
        listener = l;
    }

    public void setPrefix(String p) {
        //Have to clear and set the actual text before saving the prefix to avoid the prefix filter
        prefix = "";
        Editable text = getText();
        if (text != null) {
            text.insert(0, p);
        }
        prefix = p;

        updateHint();
    }

    public List<Object> getObjects() {
        return objects;
    }

    /**
     * Sets whether to allow duplicate objects. If false, when the user selects
     * an object that's already in the view, the current text is just cleared.
     *
     * Defaults to true. Requires that the objects implement equals() correctly.
     */
    public void allowDuplicates(boolean allow) {
        allowDuplicates = allow;
    }

    /**
     * A token view for the object
     *
     * @param object the object selected by the user from the list
     * @return a view to display a token in the text field for the object
     */
    abstract protected View getViewForObject(Object object);

    /**
     * Provides a default completion when the user hits , and there is no item in the completion
     * list
     *
     * @param completionText the current text we are completing against
     * @return a best guess for what the user meant to complete
     */

    abstract protected Object defaultObject(String completionText);

    protected String currentCompletionText() {
        Editable editable = getText();
        int end = getSelectionEnd();
        int start = tokenizer.findTokenStart(editable, end);
        if (start < prefix.length()) {
            start = prefix.length();
        }
        return TextUtils.substring(editable, start, end);
    }

    private float maxTextWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public void invalidate() {
        //Need to force the TextView private mEditor to reset as well
        setEnabled(!isEnabled());
        setEnabled(!isEnabled());
        super.invalidate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_COMMA) {
            if (getListSelection() == ListView.INVALID_POSITION) {
                Object bestGuess = null;
                if (getAdapter().getCount() > 0) {
                    bestGuess = getAdapter().getItem(0);
                } else {
                    bestGuess = defaultObject(currentCompletionText());
                }
                replaceText(convertSelectionToString(bestGuess));
            } else {
                performCompletion();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        Editable text = getText();

        if (isFocused() && text != null && lastLayout != null && action == MotionEvent.ACTION_UP) {

            int offset = -1;
            if (Build.VERSION.SDK_INT < 14) {
                offset = TextPositionCompatibilityAPI8.getOffsetForPosition(event.getX(), event.getY(), this, lastLayout);
            } else {
                offset = getOffsetForPosition(event.getX(), event.getY());
            }

            if (offset != -1) {
                TokenImageSpan[] links = text.getSpans(offset, offset, TokenImageSpan.class);

                if (links.length > 0) {
                    links[0].onClick();
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);

    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (hintVisible) {
            //Don't let users select the hint
            selStart = 0;
        }
        //Never let users select text
        selEnd = selStart;

        if (tokenClickStyle == TokenClickStyle.Select) {
            Editable text = getText();
            if (text != null) {
                clearSelections();
            }
        }


        if (prefix != null && (selStart < prefix.length() || selEnd < prefix.length())) {
            //Don't let users select the prefix
            selStart = prefix.length();

            if (selEnd < prefix.length()) {
                selEnd = prefix.length();
            }
            setSelection(selStart, selEnd);
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        lastLayout = getLayout(); //Used for checking text positions
    }

    @Override
    public void onFocusChanged(boolean hasFocus, int direction, Rect previous) {
        super.onFocusChanged(hasFocus, direction, previous);
        if (!hasFocus) {
            setSingleLine(true);

            Editable text = getText();
            if (text != null && lastLayout != null) {
                //Display +x thingy if appropriate
                int lastPosition = lastLayout.getLineVisibleEnd(0);
                TokenImageSpan[] tokens = text.getSpans(0, lastPosition, TokenImageSpan.class);
                int count = objects.size() - tokens.length;
                if (count > 0) {
                    lastPosition++;
                    CountSpan cs = new CountSpan(count, TokenCompleteTextView.this);
                    text.insert(lastPosition, cs.text);

                    float newWidth = Layout.getDesiredWidth(text, 0,
                            lastPosition + cs.text.length(), lastLayout.getPaint());
                    //If the +x span will be moved off screen, move it one token in
                    if (newWidth > maxTextWidth()) {
                        text.delete(lastPosition, lastPosition + cs.text.length());

                        if (tokens.length > 0) {
                            TokenImageSpan token = tokens[tokens.length - 1];
                            lastPosition = text.getSpanStart(token);
                            cs.setCount(count + 1);
                        } else {
                            lastPosition = prefix.length();
                        }

                        text.insert(lastPosition, cs.text);
                    }

                    text.setSpan(cs, lastPosition, lastPosition + cs.text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }


        } else {
            setSingleLine(false);
            Editable text = getText();
            if (text != null) {
                CountSpan[] counts = text.getSpans(0, text.length(), CountSpan.class);
                for (CountSpan count: counts) {
                    text.delete(text.getSpanStart(count), text.getSpanEnd(count));
                    text.removeSpan(count);
                }

                if (hintVisible) {
                    setSelection(prefix.length());
                } else {
                    setSelection(text.length());
                }

                TokenSpanWatcher[] watchers = getText().getSpans(0, getText().length(), TokenSpanWatcher.class);
                if (watchers.length == 0) {
                    //Someone removes watchers? I'm pretty sure this isn't in this code... -mgod
                    text.setSpan(spanWatcher, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
    }

    @Override
    protected CharSequence convertSelectionToString(Object object) {
        selectedObject = object;

        //if the token gets deleted, this text will get put in the field instead
        switch (deletionStyle) {
            case Clear:
                return "";
            case PartialCompletion:
                return currentCompletionText();
            case ToString:
                return object.toString();
            case _Parent:
            default:
                return super.convertSelectionToString(object);

        }
    }

    private SpannableStringBuilder buildSpannableForText(CharSequence text) {
        //Add a sentinel , at the beginning so the user can remove an inner token and keep auto-completing
        //This is a hack to work around the fact that the tokenizer cannot directly detect spans
        return new SpannableStringBuilder("," + tokenizer.terminateToken(text));
    }

    private TokenImageSpan buildSpanForObject(Object obj, SpannableStringBuilder ssb) {
        View tokenView = getViewForObject(obj);
        return new TokenImageSpan(tokenView, obj);
    }

    @Override
    protected void replaceText(CharSequence text) {
        clearComposingText();
        SpannableStringBuilder ssb = buildSpannableForText(text);
        TokenImageSpan tokenSpan = buildSpanForObject(selectedObject, ssb);

        Editable editable = getText();
        int end = getSelectionEnd();
        int start = tokenizer.findTokenStart(editable, end);
        if (start < prefix.length()) {
            start = prefix.length();
        }
        String original = TextUtils.substring(editable, start, end);

        if (editable != null) {
            if (!allowDuplicates && objects.contains(selectedObject)) {
                editable.replace(start, end, " ");
            } else {
                QwertyKeyListener.markAsReplaced(editable, start, end, original);
                editable.replace(start, end, ssb);
                editable.setSpan(tokenSpan, start, start + ssb.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }


    /**
     * Append a token object to the object list
     *
     * @param object the object to add to the displayed tokens
     * @param sourceText the text used if this object is deleted
     */
    public void addObject(final Object object, final CharSequence sourceText) {
        post(new Runnable() {
            @Override
            public void run() {
                if (!allowDuplicates && objects.contains(object)) return;

                SpannableStringBuilder ssb = buildSpannableForText(sourceText);
                TokenImageSpan tokenSpan = buildSpanForObject(object, ssb);

                Editable editable = getText();
                if (editable != null) {
                    int offset = editable.length();
                    //There might be a hint visible...
                    if (hintVisible) {
                        //...so we need to put the object in in front of the hint
                        offset = prefix.length();
                        editable.insert(offset, ssb);
                    } else {
                        editable.append(ssb);
                    }
                    editable.setSpan(tokenSpan, offset, offset + ssb.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        });
    }

    /**
     * Shorthand for addObject(object, "")
     *
     * @param object the object to add to the displayed token
     */
    public void addObject(Object object) {
        addObject(object, "");
    }

    /**
     * Remove an object from the token list. Will remove duplicates or do nothing if no object
     * present in the view.
     *
     * @param object object to remove, may be null or not in the view
     */
    public void removeObject(final Object object) {
        post(new Runnable() {
            @Override
            public void run() {
                //To make sure all the appropriate callbacks happen, we just want to piggyback on the
                //existing code that handles deleting spans when the text changes
                Editable text = getText();
                if (text == null) return;

                TokenImageSpan[] spans = text.getSpans(0, text.length(), TokenImageSpan.class);
                for (TokenImageSpan span: spans) {
                    if (span.getToken().equals(object)) {
                        //Add 1 to the end because we put a " " at the end of the spans when adding them
                        text.delete(text.getSpanStart(span), text.getSpanEnd(span) + 1);
                    }
                }
            }
        });
    }

    private void updateHint() {
        Editable text = getText();
        if (text == null) {
            return;
        }

        //Show hint if we need to
        if (prefix.length() > 0) {
            HintSpan[] hints = text.getSpans(0, text.length(), HintSpan.class);
            HintSpan hint = null;
            int testLength = prefix.length();
            if (hints.length > 0) {
                hint = hints[0];
                testLength += text.getSpanEnd(hint) - text.getSpanStart(hint);
            }

            if (text.length() == testLength) {
                if (hint != null) {
                    return;//hint already visible
                }

                //We need to display the hint manually
                Typeface tf = getTypeface();
                int style = Typeface.NORMAL;
                if (tf != null) {
                    style = tf.getStyle();
                }
                ColorStateList colors = getHintTextColors();

                HintSpan hintSpan = new HintSpan(null, style, (int)getTextSize(), colors, colors);
                text.insert(prefix.length(), getHint());
                text.setSpan(hintSpan, prefix.length(), prefix.length() + getHint().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                setSelection(prefix.length());

                hintVisible = true;
            } else {
                if (hint == null) {
                    return; //hint already removed
                }

                //Remove the hint. There should only ever be one
                int sStart = text.getSpanStart(hint);
                int sEnd = text.getSpanEnd(hint);

                text.removeSpan(hint);
                text.replace(sStart, sEnd, "");

                hintVisible = false;
            }
        }
    }

    private void clearSelections() {
        if (tokenClickStyle != TokenClickStyle.Select) return;

        Editable text = getText();
        if (text == null) return;

        TokenImageSpan[] tokens = text.getSpans(0, text.length(), TokenImageSpan.class);
        for (TokenImageSpan token: tokens) {
            token.view.setSelected(false);
        }
        invalidate();
    }

    public static class HintSpan extends TextAppearanceSpan {
        public HintSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
            super(family, style, size, color, linkColor);
        }
    }

    private class ViewSpan extends ReplacementSpan {
        protected View view;

        public ViewSpan(View v) {
            view = v;
        }

        private void prepView() {
            int widthSpec = MeasureSpec.makeMeasureSpec((int)maxTextWidth(), MeasureSpec.AT_MOST);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

            view.measure(widthSpec, heightSpec);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            prepView();

            canvas.save();
            canvas.translate(x, bottom - view.getBottom());
            view.draw(canvas);
            canvas.restore();
        }

        public int getSize(Paint paint, CharSequence charSequence, int i, int i2, Paint.FontMetricsInt fm) {
            prepView();
            if (fm != null) {
                fm.ascent = view.getBottom();
                fm.descent = 0;

                fm.top = fm.ascent;
                fm.bottom = 0;
            }

            return view.getRight();
        }
    }

    private class CountSpan extends ViewSpan {
        public String text = "";

        public CountSpan(int count, TokenCompleteTextView parent) {
            super(new TextView(parent.getContext()));
            TextView v = (TextView)view;
            v.setTextColor(parent.getCurrentTextColor());
            v.setTextSize(TypedValue.COMPLEX_UNIT_PX, parent.getTextSize());
            //Make the view as wide as the parent to push the tokens off screen
            v.setMinimumWidth((int) parent.maxTextWidth());
            setCount(count);
        }

        public void setCount(int count) {
            text = "+" + count;
            ((TextView)view).setText(text);
        }
    }

    private class TokenImageSpan extends ViewSpan {
        private Object token;

        public TokenImageSpan(View d, Object token) {
            super(d);
            this.token = token;
        }

        public Object getToken() {
            return this.token;
        }

        public void onClick() {
            Editable text = getText();
            if (text == null) return;

            switch (tokenClickStyle) {
                case Select:
                    if (!view.isSelected()) {
                        clearSelections();
                        view.setSelected(true);
                        break;
                    }
                    //If the view is already selected, we want to delete it
                case Delete:
                    //Add 1 to the end because we put a " " at the end of the spans when adding them
                    text.delete(text.getSpanStart(this), text.getSpanEnd(this) + 1);
                    break;
                case None:
                default:
                    if (getSelectionStart() != text.getSpanEnd(this) + 1) {
                        //Make sure the selection is not in the middle of the span
                        setSelection(text.getSpanEnd(this) + 1);
                    }
            }
        }

    }

    public static interface TokenListener {
        public void onTokenAdded(Object token);
        public void onTokenRemoved(Object token);
    }

    private class TokenSpanWatcher implements SpanWatcher {
        @Override
        public void onSpanAdded(Spannable text, Object what, int start, int end) {
            if (what instanceof TokenImageSpan) {
                TokenImageSpan token = (TokenImageSpan)what;
                objects.add(token.getToken());
                if (listener != null)
                    listener.onTokenAdded(token.getToken());
            }
        }

        @Override
        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what instanceof TokenImageSpan) {
                TokenImageSpan token = (TokenImageSpan)what;
                objects.remove(token.getToken());
                if (listener != null)
                    listener.onTokenRemoved(token.getToken());
            }
        }

        @Override
        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {}
    }

    /**
     * deletes tokens if you delete the space in front of them
     * without this, you get the auto-complete dropdown a character early
     */
    private class TokenTextWatcher implements TextWatcher {

        protected void removeToken(TokenImageSpan token, Editable text) {
            text.removeSpan(token);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Editable text = getText();
            if (text == null)
                return;

            clearSelections();
            updateHint();

            TokenImageSpan[] spans = text.getSpans(start - before, start - before + count, TokenImageSpan.class);

            for (TokenImageSpan token: spans) {

                int position = start + count;
                if (text.getSpanStart(token) < position && position <= text.getSpanEnd(token)) {
                    //We may have to manually reverse the auto-complete and remove the extra ,'s
                    int spanStart = text.getSpanStart(token);
                    int spanEnd = text.getSpanEnd(token);

                    removeToken(token, text);

                    //The end of the span is the character index after it
                    spanEnd--;

                    if (spanEnd >= 0 && text.charAt(spanEnd) == ',') {
                        text.delete(spanEnd, spanEnd + 1);
                    }

                    if (spanStart >= 0 && text.charAt(spanStart) == ',') {
                        text.delete(spanStart, spanStart + 1);
                    }
                }
            }
        }
    }

    /**
     * On some older versions of android sdk, the onSpanRemoved and onSpanChanged are not reliable
     * this class supplements the TokenSpanWatcher to manually trigger span updates
     */
    private class TokenTextWatcherAPI8 extends TokenTextWatcher {
        private ArrayList<TokenImageSpan> currentTokens = new ArrayList<TokenImageSpan>();

        @Override
        protected void removeToken(TokenImageSpan token, Editable text) {
            currentTokens.remove(token);
            super.removeToken(token, text);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            currentTokens.clear();

            Editable text = getText();
            if (text == null)
                return;

            TokenImageSpan[] spans = text.getSpans(0, text.length(), TokenImageSpan.class);
            currentTokens.addAll(Arrays.asList(spans));
        }

        @Override
        public void afterTextChanged(Editable s) {
            TokenImageSpan[] spans = s.getSpans(0, s.length(), TokenImageSpan.class);
            for (TokenImageSpan token: currentTokens) {
                if (!Arrays.asList(spans).contains(token)) {
                    spanWatcher.onSpanRemoved(s, token, s.getSpanStart(token), s.getSpanEnd(token));
                }
            }
        }
    }

    private static class TextPositionCompatibilityAPI8 {
        //Borrowing some code from API 14
        static public int getOffsetForPosition(float x, float y, TextView tv, Layout layout) {
            if (layout == null) return -1;
            final int line = getLineAtCoordinate(y, tv, layout);
            return getOffsetAtCoordinate(line, x, tv, layout);
        }

        static private float convertToLocalHorizontalCoordinate(float x, TextView tv) {
            x -= tv.getTotalPaddingLeft();
            // Clamp the position to inside of the view.
            x = Math.max(0.0f, x);
            x = Math.min(tv.getWidth() - tv.getTotalPaddingRight() - 1, x);
            x += tv.getScrollX();
            return x;
        }

        static private int getLineAtCoordinate(float y, TextView tv, Layout layout) {
            y -= tv.getTotalPaddingTop();
            // Clamp the position to inside of the view.
            y = Math.max(0.0f, y);
            y = Math.min(tv.getHeight() - tv.getTotalPaddingBottom() - 1, y);
            y += tv.getScrollY();
            return layout.getLineForVertical((int) y);
        }

        static private int getOffsetAtCoordinate(int line, float x, TextView tv, Layout layout) {
            x = convertToLocalHorizontalCoordinate(x, tv);
            return layout.getOffsetForHorizontal(line, x);
        }
    }
}
