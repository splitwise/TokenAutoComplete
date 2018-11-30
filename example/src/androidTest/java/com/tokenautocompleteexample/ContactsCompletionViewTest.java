package com.tokenautocompleteexample;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.tokenautocompleteexample.TokenMatchers.emailForPerson;
import static com.tokenautocompleteexample.TokenMatchers.tokenCount;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ContactsCompletionViewTest {

    @Rule
    public ActivityTestRule<TestCleanTokenActivity> activityRule = new ActivityTestRule<>(
            TestCleanTokenActivity.class);

    @Test
    public void completesOnComma(){
        onView(withId(R.id.searchView))
                .perform(typeText("mar,"))
                .check(matches(emailForPerson(0, is("marshall@example.com"))))
                .check(matches(tokenCount(is(1))));
    }

    @Test
    public void doesntCompleteWithoutComma() {
        onView(withId(R.id.searchView))
                .perform(typeText("mar"))
                .check(matches(tokenCount(is(0))));
    }

    @Test
    public void ignoresObjects() {
        final Person ignorable = Person.samplePeople()[0];
        final Person notIgnorable = Person.samplePeople()[1];
        final ContactsCompletionView completionView = activityRule.getActivity().completionView;
        completionView.setPersonToIgnore(ignorable);

        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                completionView.addObjectSync(notIgnorable);
                assertEquals(1, completionView.getObjects().size());
                completionView.addObjectSync(ignorable);
                assertEquals(1, completionView.getObjects().size());
            }
        });

        onView(withId(R.id.searchView))
                .perform(typeText(ignorable.getName() + ","))
                .check(matches(tokenCount(is(1))));
        onView(withId(R.id.searchView))
                .perform(typeText("ter,"))
                .check(matches(tokenCount(is(2))));
    }

    @Test
    public void clearsAllObjects() {
        final ContactsCompletionView completionView = activityRule.getActivity().completionView;
        completionView.allowCollapse(true);

        final TestTokenListener listener = new TestTokenListener();
        completionView.setTokenListener(listener);

        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Person person: Person.samplePeople()) {
                    completionView.addObjectSync(person);
                }
                assertEquals(Person.samplePeople().length, completionView.getObjects().size());
                assertEquals(Person.samplePeople().length, listener.added.size());
                completionView.performCollapse(false);
            }
        });

        onView(withId(R.id.searchView))
                //The +count text is included
                .check(matches(withText(containsString("+"))))
                .check(matches(tokenCount(is(Person.samplePeople().length))));
        completionView.clearAsync();
        onView(withId(R.id.searchView))
                .check(matches(tokenCount(is(0))));

        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals(Person.samplePeople().length, listener.added.size());
                assertEquals(Person.samplePeople().length, listener.removed.size());
                assertEquals(0, listener.ignored.size());
            }
        });
    }
}