package com.tokenautocompleteexample;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.tokenautocomplete.TagTokenizer;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TokenActivity extends AppCompatActivity implements TokenCompleteTextView.TokenListener<Person> {
    ContactsCompletionView completionView;
    Person[] people;
    ArrayAdapter<Person> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabs = (TabHost) findViewById(R.id.tabHost);
        tabs.setup();
        tabs.addTab(tabs.newTabSpec("Contacts").setContent(R.id.contactsFrame).setIndicator("Contacts"));
        tabs.addTab(tabs.newTabSpec("Composer").setContent(R.id.hashtagsFrame).setIndicator("Composer"));

        //Set up the contacts example views
        people = Person.samplePeople();
        adapter = new PersonAdapter(this, R.layout.person_layout, people);

        completionView = (ContactsCompletionView)findViewById(R.id.searchView);
        completionView.setAdapter(adapter);
        completionView.setThreshold(1);
        completionView.setTokenListener(this);
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        completionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ((TextView)findViewById(R.id.textValue)).setText(editable.toString());
            }
        });


        if (savedInstanceState == null) {
            completionView.setPrefix("To: ", Color.parseColor("blue"));
            completionView.addObjectSync(people[0]);
            completionView.addObjectSync(people[1]);
        }

        Button removeButton = (Button)findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Person> people = completionView.getObjects();
                if (people.size() > 0) {
                    completionView.removeObjectAsync(people.get(people.size() - 1));
                }
            }
        });

        Button addButton = (Button)findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rand = new Random();
                completionView.addObjectAsync(people[rand.nextInt(people.length)]);
            }
        });

        //Setup the tag composer view
        final TagCompletionView tagView = findViewById(R.id.composeView);
        tagView.performBestGuess(false);
        tagView.preventFreeFormText(false);
        tagView.setTokenizer(new TagTokenizer(Arrays.asList('@', '#')));
        tagView.setAdapter(new TagAdapter(this, R.layout.tag_layout, Tag.sampleTags()));
        tagView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        tagView.setThreshold(1);

        final TextView taggedContentPreview = findViewById(R.id.composedValue);

        tagView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                taggedContentPreview.setText(tagView.getContentText().toString());
            }
        });

        //NOTE: this is not a great general solution to the problem of setting/restoring tokenized
        //freeform text. I'm still looking for a good solution that would also allow pasting raw
        //text into the view and having tokens get processed in it
        tagView.setText("A sample ");
        tagView.addObjectSync(Tag.sampleTags()[0]);
        tagView.append("tweet with ");
        tagView.addObjectSync(Tag.sampleTags()[5]);
        tagView.addObjectSync(Tag.sampleTags()[10]);
    }

    private void updateTokenConfirmation() {
        StringBuilder sb = new StringBuilder("Current tokens:\n");
        for (Object token: completionView.getObjects()) {
            sb.append(token.toString());
            sb.append("\n");
        }

        ((TextView)findViewById(R.id.tokens)).setText(sb);
    }


    @Override
    public void onTokenAdded(Person token) {
        ((TextView)findViewById(R.id.lastEvent)).setText("Added: " + token);
        updateTokenConfirmation();
    }

    @Override
    public void onTokenRemoved(Person token) {
        ((TextView)findViewById(R.id.lastEvent)).setText("Removed: " + token);
        updateTokenConfirmation();
    }

    @Override
    public void onTokenIgnored(Person token) {
        ((TextView)findViewById(R.id.lastEvent)).setText("Ignored: " + token);
        updateTokenConfirmation();
    }
}
