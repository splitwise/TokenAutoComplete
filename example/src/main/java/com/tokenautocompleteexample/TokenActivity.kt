package com.tokenautocompleteexample

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TabHost
import android.widget.TextView

import com.tokenautocomplete.TagTokenizer
import com.tokenautocomplete.TokenCompleteTextView
import kotlinx.android.synthetic.main.activity_main.*

import java.util.Arrays
import java.util.Random

class TokenActivity : AppCompatActivity(), TokenCompleteTextView.TokenListener<Person> {
    @VisibleForTesting val completionView: ContactsCompletionView = searchView
    private val people = Person.samplePeople()
    private val adapter = PersonAdapter(this, R.layout.person_layout, people)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabs = findViewById<View>(R.id.tabHost) as TabHost
        tabs.setup()
        tabs.addTab(tabs.newTabSpec("Contacts").setContent(R.id.contactsFrame).setIndicator("Contacts"))
        tabs.addTab(tabs.newTabSpec("Composer").setContent(R.id.hashtagsFrame).setIndicator("Composer"))

        completionView.setAdapter(adapter)
        completionView.threshold = 1
        completionView.setTokenListener(this)
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.None)
        completionView.isLongClickable = true
        completionView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                (findViewById<View>(R.id.textValue) as TextView).text = editable.toString()
            }
        })


        if (savedInstanceState == null) {
            completionView.addObjectSync(people[0])
            completionView.addObjectSync(people[1])
        }

        val removeButton = findViewById<View>(R.id.removeButton) as Button
        removeButton.setOnClickListener {
            val people = completionView.objects
            if (people.size > 0) {
                completionView.removeObjectAsync(people[people.size - 1])
            }
        }

        val addButton = findViewById<View>(R.id.addButton) as Button
        addButton.setOnClickListener {
            val rand = Random()
            completionView.addObjectAsync(people[rand.nextInt(people.size)])
        }

        //Setup the tag composer view
        val tagView = findViewById<TagCompletionView>(R.id.composeView)
        tagView.performBestGuess(false)
        tagView.preventFreeFormText(false)
        tagView.setTokenizer(TagTokenizer(Arrays.asList('@', '#')))
        tagView.setAdapter(TagAdapter(this, R.layout.tag_layout, Tag.sampleTags()))
        tagView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select)
        tagView.threshold = 1

        val taggedContentPreview = findViewById<TextView>(R.id.composedValue)

        tagView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                taggedContentPreview.text = tagView.contentText.toString()
            }
        })

        //NOTE: this is not a great general solution to the problem of setting/restoring tokenized
        //freeform text. I'm still looking for a good solution that would also allow pasting raw
        //text into the view and having tokens get processed in it
        tagView.setText("A sample ")
        tagView.addObjectSync(Tag.sampleTags()[0])
        tagView.append("tweet with ")
        tagView.addObjectSync(Tag.sampleTags()[5])
        tagView.addObjectSync(Tag.sampleTags()[10])
    }

    private fun updateTokenConfirmation() {
        val sb = StringBuilder("Current tokens:\n")
        for (token in completionView.objects) {
            sb.append(token.toString())
            sb.append("\n")
        }

        (findViewById<View>(R.id.tokens) as TextView).text = sb
    }


    override fun onTokenAdded(token: Person) {
        (findViewById<View>(R.id.lastEvent) as TextView).text = "Added: $token"
        updateTokenConfirmation()
    }

    override fun onTokenRemoved(token: Person) {
        (findViewById<View>(R.id.lastEvent) as TextView).text = "Removed: $token"
        updateTokenConfirmation()
    }

    override fun onTokenIgnored(token: Person) {
        (findViewById<View>(R.id.lastEvent) as TextView).text = "Ignored: $token"
        updateTokenConfirmation()
    }
}