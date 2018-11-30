package com.tokenautocompleteexample;

import com.tokenautocomplete.TokenCompleteTextView;

import java.util.ArrayList;
import java.util.List;

class TestTokenListener implements TokenCompleteTextView.TokenListener<Person> {

    List<Person> added = new ArrayList<>();
    List<Person> ignored = new ArrayList<>();
    List<Person> removed = new ArrayList<>();

    @Override
    public void onTokenAdded(Person token) {
        added.add(token);
    }

    @Override
    public void onTokenIgnored(Person token) {
        ignored.add(token);
    }

    @Override
    public void onTokenRemoved(Person token) {
        removed.add(token);
    }
}
