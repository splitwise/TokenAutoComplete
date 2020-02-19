package com.tokenautocompleteexample;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tokenautocomplete.FilteredArrayAdapter;

public class TagAdapter extends FilteredArrayAdapter<Tag> {

    @LayoutRes
    private int layoutId;

    TagAdapter(Context context, @LayoutRes int layoutId, Tag[] tags) {
        super(context, layoutId, tags);
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {

            LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = l.inflate(layoutId, parent, false);
        }

        Tag t = getItem(position);
        ((TextView)convertView.findViewById(R.id.value)).setText(t.getFormattedValue());

        return convertView;
    }

    @Override
    protected boolean keepObject(Tag tag, String mask) {
        mask = mask.toLowerCase();
        return tag.getPrefix() == mask.charAt(0) &&
                tag.getValue().toLowerCase().startsWith(mask.substring(1, mask.length()));
    }
}
