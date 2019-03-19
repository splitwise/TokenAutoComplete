package com.tokenautocompleteexample;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenautocomplete.TokenCompleteTextView;

public class TagCompletionView extends TokenCompleteTextView<Tag> {

    public TagCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Tag object) {
        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TokenTextView token = (TokenTextView) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        token.setText(object.getFormattedValue());
        return token;
    }

    @Override
    protected Tag defaultObject(String completionText) {
        if (completionText.length() == 1) {
            return null;
        } else {
            return new Tag(completionText.charAt(0), completionText.substring(1, completionText.length()));
        }
    }
}
