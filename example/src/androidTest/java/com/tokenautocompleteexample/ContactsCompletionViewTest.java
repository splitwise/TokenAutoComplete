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
                assertEquals(Person.samplePeople().length, completionView.getObjects().size());
                assertEquals(Person.samplePeople().length, listener.added.size());
            }
        });

        onView(withId(R.id.searchView))
                //The +count text is included
                .check(matches(withText(containsString("+"))))
                .check(matches(tokenCount(is(Person.samplePeople().length))));
        completionView.clearAsync();
        onView(withId(R.id.searchView))
                .check(matches(tokenCount(is(0))))
                //The text should also reset completely
                .check(matches(withText(String.format("To: %s", completionView.getHint()))));

        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals(Person.samplePeople().length, listener.added.size());
                assertEquals(Person.samplePeople().length, listener.removed.size());
                assertEquals(0, listener.ignored.size());
            }
        });

        //Make sure going to 0 while collapsed, then adding an object doesn't hit a crash
        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                completionView.addObjectSync(Person.samplePeople()[0]);
                assertEquals(1, completionView.getObjects().size());
                for (Person person: Person.samplePeople()) {
                    completionView.addObjectSync(person);
                }
                assertEquals(Person.samplePeople().length + 1, completionView.getObjects().size());
            }
        });
        onView(withId(R.id.searchView))
                //The +count text is included
                .check(matches(withText(containsString("+"))));
    }

    @Test
    public void handlesHintOnInitialItemSelected() {
        final ContactsCompletionView completionView = activityRule.getActivity().completionView;

        final TestTokenListener listener = new TestTokenListener();
        completionView.setTokenListener(listener);

        onView(withId(R.id.searchView))
                .check(matches(tokenCount(is(0))))
                //The text should also reset completely
                .check(matches(withText(String.format("To: %s", completionView.getHint()))));
        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                completionView.simulateSelectingPersonFromList(Person.samplePeople()[0]);
            }
        });

        onView(withId(R.id.searchView))
                .check(matches(tokenCount(is(1))))
                //The text should also reset completely
                .check(matches(withText(String.format("To: %s, ", Person.samplePeople()[0].toString()))));
    }

    @Test
    public void ellipsizesPreservingPrefix() {
        final ContactsCompletionView completionView = activityRule.getActivity().completionView;

        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //6th token is as wide as the view
                completionView.addObjectSync(Person.samplePeople()[6]);
                completionView.performCollapse(false);
            }
        });

        onView(withId(R.id.searchView))
                .check(matches(tokenCount(is(1))))
                .check(matches(withText("To:  +1")));
    }


}