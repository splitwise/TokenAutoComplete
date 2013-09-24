TokenAutoComplete 
=================
<img align="right" src="http://splitwise.files.wordpress.com/2013/09/untitled.png" width=300 />

TokenAutoComplete is a Android Gmail style token  auto-complete text field and filter. It's designed to have an extremely simple API to make it easy for anyone to implement this functionality while still exposing enough customization to let you make it awesome.

Support for Android 2.2 and up. Will probably work back to Android 2.0, but I haven't tested it.

Setup
=====

* Download the [jar file](https://github.com/splitwise/TokenAutoComplete/releases/download/v1.0/token-autocomplete.jar) and add it to your project

Creating your auto complete view
--------------------------------

If you'd rather just start with a working example, clone the project and take a look.

For a basic token auto complete view, you'll need to

1. Subclass TokenMultiAutoCompleteView
2. Create a layout and activity for your completion view

### Subclass TokenMultiAutoCompleteView

You'll need to provide your own implementations for getViewForObject and defaultObject. You should return a view that displays the token from getViewForObject. In defaultObject, you need to guess what the user meant with their completion. This is usually from the user typing something and hitting "," - see the way gmail for Android handles this for example. Here's a simple example:

```java
public class ContactsCompletionView extends TokenCompleteTextView {
    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Object object) {
        Person p = (Person)object;

        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.contact_token, (ViewGroup)ContactsCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.name)).setText(p.getEmail());

        return view;
    }

    @Override
    protected Object defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new Person(completionText, completionText.replace(" ", "") + "@example.com");
        } else {
            return new Person(completionText.substring(0, index), completionText);
        }
    }
}
```

Layout code for contact_token

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_height="wrap_content"
    android:layout_width="wrap_content">

    <TextView android:id="@+id/name"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/token_background"
        android:padding="5dp"
        android:textColor="@android:color/white"
        android:textSize="18sp" />

</LinearLayout>
```

Token backgound drawable

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" >
    <solid android:color="#ffafafaf" />
    <corners
        android:topLeftRadius="5dp"
        android:bottomLeftRadius="5dp"
        android:topRightRadius="5dp"
        android:bottomRightRadius="5dp" />
</shape>
```

Person object code

```java
public class Person {
    private String name;
    private String email;

    public Person(String n, String e) { name = n; email = e; }

    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return name; }
}
```

### Create a layout and activity for your completion view

I'm adding some very stupid "contacts" to the app so you can see it work, but you should read data from the contacts data provider in a real app.

Activity code

```java
public class TokenActivity extends Activity {
    ContactsCompletionView completionView;
    Person[] people;
    ArrayAdapter<Person> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        people = new Person[]{
                new Person("Marshall Weir", "marshall@example.com"),
                new Person("Margaret Smith", "margaret@example.com"),
                new Person("Max Jordan", "max@example.com"),
                new Person("Meg Peterson", "meg@example.com"),
                new Person("Amanda Johnson", "amanda@example.com"),
                new Person("Terry Anderson", "terry@example.com")
        };

        adapter = new ArrayAdapter<Person>(this, android.R.layout.simple_list_item_1, people);

        completionView = (ContactsCompletionView)findViewById(R.id.searchView);
        completionView.setAdapter(adapter);
    }
}
```

Layout code

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.tokenautocomplete.ContactsCompletionView
        android:id="@+id/searchView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

</RelativeLayout>
```

That's it! You can grab the objects the user tokenized with getObjects() on the TokenCompleteTextView when you need to get the data out.

Custom filtering
================

If you've used the gmail auto complete, you know that it doesn't use the default "toString" filtering you get with an ArrayAdapter. If you've dug in to the ArrayAdapter, you find an unfortunate mess with no good place for you to add your own custom filter code without re-writing the whole class. If you need to support older versions of Android, this quickly becomes difficult as you'll need to carefully handle API differences for each version.

(NOTE: ArrayAdapter is actually well written, it just doesn't allow for easy custom filters)

I've added my own FilteredArrayAdapter to the jar file that is a subclass of ArrayAdapter but does have some good hooks for custom filtering. You'll want to be fairly efficient in this as it gets called a lot, but it's a simple process to add a custom filter. If you are using the TokenActivity above, you simply replace the line

```java
adapter = new ArrayAdapter<Person>(this, android.R.layout.simple_list_item_1, people);
```

with

```java
adapter = new FilteredArrayAdapter<Person>(this, android.R.layout.simple_list_item_1, people) {
    @Override
    protected boolean keepObject(Person obj, String mask) {
        mask = mask.toLowerCase();
        return obj.getName().toLowerCase().startsWith(mask) || obj.getEmail().toLowerCase().startsWith(mask);
    }
};
```

Responding to user selections
=============================

If you're solving a similar problem to Splitwise, you need to handle users adding and removing tokens. I've provided a simple interface to get these events and allow you to respond to them in the TokenCompleteTextView:

```java
public static interface TokenListener {
    public void onTokenAdded(Object token);
    public void onTokenRemoved(Object token);
}
```

We can modify the TokenActivity to see how these callbacks work:

```java
public class TokenActivity extends Activity implements TokenCompleteTextView.TokenListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* code from the initial example */

        completionView.setTokenListener(this);
    }

    @Override
    public void onTokenAdded(Object token) {
        System.out.println("Added: " + token);
    }

    @Override
    public void onTokenRemoved(Object token) {
        System.out.println("Removed: " + token);
    }
}
```

In Splitwise we use these callbacks to handle users selecting a group when adding an expense. When a user adds a group to an expense, we remove all the users in the group and the other groups from the array adapter. A user should only be able to select one group and it would be redundant to add users in the group to the expense again.

Custom completion delete behavior
=================================

We've defaulted to the gmail style delete handling. That is, the most recently completed token, when deleted, turns into the text that was there before. All other tokens simply disappear when deleted.

While this is the best in our case, we've provided a couple of other options if you want them. Call setDeletionStyle on the TokenCompleteTextView for new behaviors.

#### TokenCompleteTextView.Clear

This is the default. The most recently completed token will turn into the partial completion text it replaces, all other tokens will just disappear when deleted

#### TokenCompleteTextView.PartialCompletion

All tokens will turn into the partial completion text they replaced

#### TokenCompleteTextView.ToString

Tokens will be replaced with the toString value of the objects they represent when they are deleted

License
=======

    Copyright (c) 2013 splitwise

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the "Software"), to deal in
    the Software without restriction, including without limitation the rights to
    use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
    the Software, and to permit persons to whom the Software is furnished to do so,
    subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
    IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




