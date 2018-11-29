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

import static com.tokenautocompleteexample.TokenMatchers.emailForPerson;
import static com.tokenautocompleteexample.TokenMatchers.tokenCount;
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
}