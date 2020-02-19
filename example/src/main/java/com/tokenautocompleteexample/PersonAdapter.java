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

public class PersonAdapter extends FilteredArrayAdapter<Person> {
    @LayoutRes private int layoutId;

    PersonAdapter(Context context, @LayoutRes int layoutId, Person[] people) {
        super(context, layoutId, people);
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {

            LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = l.inflate(layoutId, parent, false);
        }

        Person p = getItem(position);
        ((TextView)convertView.findViewById(R.id.name)).setText(p.getName());
        ((TextView)convertView.findViewById(R.id.email)).setText(p.getEmail());

        return convertView;
    }

    @Override
    protected boolean keepObject(Person person, String mask) {
        mask = mask.toLowerCase();
        return person.getName().toLowerCase().startsWith(mask) || person.getEmail().toLowerCase().startsWith(mask);
    }
}
