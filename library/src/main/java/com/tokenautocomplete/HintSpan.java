package com.tokenautocomplete;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.text.style.TextAppearanceSpan;

/**
 * Subclass of TextAppearanceSpan just to work with how Spans get detected
 *
 * Created on 2/3/15.
 * @author mgod
 */
public class HintSpan extends TextAppearanceSpan {

    public static final Creator<HintSpan> CREATOR = new Creator<HintSpan>() {
        @Override
        public HintSpan createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public HintSpan[] newArray(int size) {
            return new HintSpan[0];
        }
    };

    public HintSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
        super(family, style, size, color, linkColor);
    }
}
