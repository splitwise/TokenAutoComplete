### Version 3.0

The `3.0.1` version is now available! This should resolve a number of text handling issues and lay the groundwork for better support of mixed text and token input. If you're still on `2.*`, you can find the docs for `2.0.8` [here](https://github.com/splitwise/TokenAutoComplete/tree/2.0.8).

### Upgrading from 2.* to 3.0

For most developers, the migration should be fairly simple. Here are the likely issues you'll need to resolve:

1. The view now inherits from `AppCompatAutoCompleteTextView`. You probably don't need to make any changes for this, but you will need to include the Android support library if you are not already.

2. `setTokenDeleteStyle` has been removed. Something similar to the Clear style has been hardcoded in. This feature never worked reliably and caused a lot of crashes.

3. `addObject` has been renamed `addObjectAsync`. `removeObject` has been renamed `removeObjectAsync`. There are also `addObjectSync`/`removeObjectSync` versions that can be called from the UI thread and guarantee that `getObjects` will include these changes on the next call.

4. `setAllowDuplicates(false)` has been made more flexible to deal with issues around different kinds of equality. If you need the 2.* version of the behavior, add this to your `TokenCompleteTextView` subclass:

```
@Override
public boolean shouldIgnoreToken(T token) {
    return getObjects().contains(token);
}
```

5. `TokenListener` has a new method you will need to add:

```
public interface TokenListener<T> {
    void onTokenAdded(T token);
    void onTokenRemoved(T token);
    void onTokenIgnored(T token);
}
```

6. `convertSerializableArrayToObjectArray` has been renamed `convertSerializableObjectsToTypedObjects`.

You may also find that the vertical alignment of your tokens has changed. It appears that the app compat text view layout is slightly different than the normal one. You will likely find that you need to adjust the baseline values for your token views.

There have been a number of under the hood changes to the text handling, so if you've been directly accessing the text or using your own tokenizer, you may need to make more changes than this.

### Upgrading from 1.* to 2.0

There is one breaking change from 1.* to 2.0. You need to extend ```TokenCompleteTextView<Object>``` instead of ```TokenCompleteTextView```.

TokenAutoComplete
=================

TokenAutoComplete is an Android Gmail style token auto-complete text field and filter. It's designed to have an extremely simple API to make it easy for anyone to implement this functionality while still exposing enough customization to let you make it awesome.

Support for Android 4.0.3 (API 14) and up. If you need support for earlier versions of Android, [version 1.2.1](https://github.com/splitwise/TokenAutoComplete/releases/tag/v1.2.1) is the most recent version that supports Android 2.2 (API 8) and up.

![Focused TokenAutoCompleteTextView example](https://raw.github.com/splitwise/TokenAutoComplete/gh-pages/images/focused.png)

![Unfocused TokenAutoCompleteTextView example](https://raw.github.com/splitwise/TokenAutoComplete/gh-pages/images/not_focused.png)

Setup
=====

### Gradle
```
dependencies {
    compile "com.splitwise:tokenautocomplete:3.0.1@aar"
}
```
### Maven
```
<dependency>
  <groupId>com.splitwise</groupId>
  <artifactId>tokenautocomplete</artifactId>
  <version>3.0.1</version>
  <type>aar</type>
</dependency>
```
### No build tools

[Download the jar file](https://github.com/splitwise/TokenAutoComplete/releases) and add it to your project

If you would like to get the most recent code in a jar, clone the project and run ```./gradlew jar``` from the root folder. This will build a tokenautocomplete.jar in ```library/build/libs/```.

You may also add the library as an Android Library to your project. All the library files live in ```library```.

Creating your auto complete view
--------------------------------

If you'd rather just start with a working example, clone the project and take a look.

For a basic token auto complete view, you'll need to

1. Subclass TokenCompleteTextView
2. Create a layout and activity for your completion view

### Subclass TokenCompleteTextView

You'll need to provide your own implementations for `getViewForObject` and `defaultObject`. You should return a view that displays the token from `getViewForObject`. In `defaultObject`, you need to guess what the user meant with their completion. This is usually from the user typing something and hitting "," - see the way gmail for Android handles this for example. Here's a simple example:

```java
public class ContactsCompletionView extends TokenCompleteTextView<Person> {
    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Person person) {

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        view.setText(person.getEmail());

        return view;
    }

    @Override
    protected Person defaultObject(String completionText) {
        //Oversimplified example of guessing if we have an email or not
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
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/name"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/token_background"
    android:padding="5dp"
    android:textColor="@android:color/white"
    android:textSize="18sp" />
```

Token background drawable

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" >
    <solid android:color="#ffafafaf" />
    <corners android:radius="5dp" />
</shape>
```

Person object code

```java
public class Person implements Serializable {
    private String name;
    private String email;

    public Person(String n, String e) { name = n; email = e; }

    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return name; }
}
```

Note that the class implements ```Serializable```. In order to restore the view state properly, the ```TokenCompleteTextView``` needs to be able to save and restore your objects from disk. If your objects cannot be made ```Serializable```, please look at [restoring the view state](#restoring-the-view-state).

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

    <com.yourpackagename.ContactsCompletionView
        android:id="@+id/searchView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

</RelativeLayout>
```

That's it! You can grab the objects the user tokenized with `getObjects()` on the `TokenCompleteTextView` when you need to get the data out.


Setting a prefix prompt
=======================

If you have a short prompt like "To: ", you can probably get away with setting a drawable on the left side of the ```TokenCompleteTextView```. If you have something longer, you will probably not want your prefix to take up the whole height of the view. If you would like to have a prefix that only indents the first line, you should use ```setPrefix```. This code is a little quirky when restoring the activity, so you want to make sure it only gets called on a fresh start in ```onCreate```:

```java
if (savedInstanceState == null) {
    completionView.setPrefix("Your bestest friends: ");
}
```

Custom filtering
================

If you've used the gmail auto complete, you know that it doesn't use the default "toString" filtering you get with an `ArrayAdapter`.

I've added my own FilteredArrayAdapter to the jar file that is a subclass of ArrayAdapter but has some good hooks for custom filtering. You'll want to be fairly efficient in this as it gets called a lot, but it's a simple process to add a custom filter. If you are using the `TokenActivity` above, you simply replace the line

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

Duplicate objects
=================

In addition to custom filtering, you may want to make sure you don't get duplicate tokens. In your `TokenCompleteTextView` subclass, override `shouldIgnoreToken`:

```
@Override
public boolean shouldIgnoreToken(T token) {
    return getObjects().contains(token);
}
```

Any text the user entered for the duplicate token will be cleared. You can implement whatever matching behavior you need. This implementation assumes the `equals` method on your token objects is a reasonable comparison.

Responding to user selections
=============================

If you're solving a similar problem to Splitwise, you need to handle users adding and removing tokens. I've provided a simple interface to get these events and allow you to respond to them in the TokenCompleteTextView:

```java
public static interface TokenListener<T> {
    public void onTokenAdded(T token);
    public void onTokenRemoved(T token);
    public void onTokenIgnored(T token)
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
    public void onTokenAdded(Person token) {
        System.out.println("Added: " + token);
    }

    @Override
    public void onTokenRemoved(Person token) {
        System.out.println("Removed: " + token);
    }

    @Override
    public void onTokenIgnored(Person token) {
        System.out.println("Ignored: " + token);
    }
}
```

In Splitwise we use these callbacks to handle users selecting a group when adding an expense. When a user adds a group to an expense, we remove all the users in the group and the other groups from the array adapter. A user should only be able to select one group and it would be redundant to add users in the group to the expense again.

Programatically add and remove objects
======================================

You may want to prefill the list with objects. For example when replying to an email, you would want the To: and CC: fields to have the correct emails in them. You can use ```addObjectSync``` to put these tokens in. You can also remove objects programatically with ```removeObjectSync``` though this will remove all objects that return true when calling ```equals``` on them. If you have copies in the array, you may need to take special care with this.

The `Sync` versions of these methods must be called from the UI thread. There are also `addObjectAsync` and `removeObjectAsync` that can be called from any thread, but will not update the view or data immediately. Finally, there is a ```clearAsync``` function to empty the EditText and remove all the objects.

Letting users click to select and delete tokens
===============================================

There are four different styles of click handling build into the project. Call ```setTokenClickStyle``` to change the behavior. If you need more control over how click behavior works, please see issue #350.

#### TokenCompleteTextView.TokenClickStyle.None

This is the default, even though it doesn't match the Gmail behavior. When the user clicks on a token, the view will move the cursor in front of or after the token. Users should not be able to get the cursor in the token as this causes confusing behavior.

#### TokenCompleteTextView.TokenClickStyle.Delete

When the user clicks on a token, the token will be removed from the field. If you need some kind of confirmation, handle it with the onTokenRemoved callback and re-add the token if the user changes their mind.

#### TokenCompleteTextView.TokenClickStyle.Select

This behavior most closely matches the Gmail token field behavior, but I did not make it the default to simplify the initial tutorial. The first click on a token will unselect any currently selected token views, then it will call ```setSelected(true)``` on the selected token.

#### TokenCompleteTextView.TokenClickStyle.SelectDeselect

This works the same as `Select` except that a second click on the token will deselect it and call `setSelected(false)`.

#### Showing token selected state

If you want to change the colors of the token when it is selected, you will need to add appropriate drawables to your project. In the test project, we have the following:

token_background.xml
```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/token_default" android:state_selected="false" />
    <item android:drawable="@drawable/token_selected" android:state_selected="true" />
</selector>
```

token_default.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" >
    <stroke
        android:width="1dp"
        android:color="#ffd4d4d4" />
    <solid android:color="#ffafafaf" />

    <corners android:radius="3dp"/>
</shape>
```

token_selected.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" >
    <stroke
        android:width="1dp"
        android:color="#ffa4a4a4" />
    <solid android:color="#ff7a7a7a" />

    <corners android:radius="3dp"/>
</shape>
```

If you need more detailed view customization like changing a picture in the token or resizing the token, you will need to provide a custom view to use in the layout you inflate in ```getViewForObject``` and override ```setSelected``` in that view. You can then make appropriate changes to the view.

### Example custom view

In a view implementation (see ```com.tokenautocomplete.TokenTextView```):
```java
public class TokenTextView extends TextView {

    ...

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, selected ? R.drawable.close_x : 0, 0);
    }
}
```

contact_token.xml
```xml
<com.tokenautocomplete.TokenTextView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/name"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/token_background"
    android:textColor="@android:color/white"
    android:textSize="14sp"
    android:maxLines="1"
    android:ellipsize="end"
    android:padding="4dp"
    tools:text="Glenda Jönsson" />
```

Inflate your custom view:

```java
public class ContactsCompletionView extends TokenCompleteTextView<Person> {

    ...

    @Override
    protected View getViewForObject(Person person) {
        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TokenTextView token = (TokenTextView) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        token.setText(person.getEmail());
        return token;
    }
}
```

Restoring the view state
========================

If your token objects implement ```Serializable``` or `Parcelable`, the ```TokenCompleteTextView``` will automatically handle ```onSaveInstanceState``` and ```onRestoreInstanceState```. If you cannot make your objects ```Serializable``` or `Parcelable`, you should override ```getSerializableObjects``` and ```convertSerializableObjectsToTypedObjects```. ```getSerializableObjects``` should return an array of ```Serializable``` objects that can be used to rebuild your original objects when restoring the view state. ```convertSerializableObjectsToTypedObjects``` should take an array of ```Serializable``` objects and use them to rebuild your token objects.

We use something similar to this at [splitwise](http://splitwise.com) to avoid saving complicated object graphs:

```java
@Override
protected ArrayList<Object> convertSerializableObjectsToTypedObjects(ArrayList<Serializable> sers) {
    ArrayList<Object> objs = new ArrayList<Object>();
    for (Serializable s: sers) {
        if (s instanceof Long) {
            Contact c = Contact.loadFromDatabase((Long)s);
            objs.add(c);
        } else {
            objs.add(s);
        }
    }

    return objs;
}

@Override
protected ArrayList<Serializable> getSerializableObjects() {
    ArrayList<Serializable> s = new ArrayList<Serializable>();
    for (Object obj: getObjects()) {
        if (obj instanceof Serializable) {
            s.add((Serializable)obj);
        } else {
            //obj is a Contact
            s.add(((Contact)obj).getId());
        }
    }
    return s;
}
```

Other options
=============
* Turn off making a best guess when converting text into a token
```java
performBestGuess(false);
```

* Prevent the TokenCompleteTextView collapsing to a single line when it loses focus
```java
allowCollapse(false);
```

* Change the set of characters that complete a token
```java
setTokenizer(new CharacterTokenizer(Arrays.asList('.', ','), ","));
```

* Detect tokens based on their first character
```java
//Detect @usernames and #hashtags
setTokenizer(new TagTokenizer(Arrays.asList('@', '#')));
```

* Change the number of characters required to start showing suggestions
```java
setThreshold(1);
```

* Limit the total number of tokens in the field
```java
setTokenLimit(10);
```

* Prevent specific tokens from being deleted by overriding ```isTokenRemovable``` on your completion view

#### Experimental mixed freeform text and token input support

These options should allow you to build something similar to a Tweet composing view, but is likely to still have some edge cases with unusual behavior.

* Allow mixed text and token input
```java
preventFreeFormText(false);
```

* Get the string value of the text content of the view, including reasonable string representations of the tokens. If `getContextText` is not using an acceptable string representation of the token, you can override `tokenToString` to change how the token is represented.
```java
getContentText();
```

License
=======

    Copyright (c) 2013, 2014 splitwise, Wouter Dullaert

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
