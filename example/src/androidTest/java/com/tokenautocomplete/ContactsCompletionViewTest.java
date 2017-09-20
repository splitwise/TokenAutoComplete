package com.tokenautocomplete;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static com.tokenautocomplete.TokenMatchers.emailForPerson;
import static com.tokenautocomplete.TokenMatchers.tokenCount;
import static org.hamcrest.Matchers.is;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ContactsCompletionViewTest {

    @Rule
    public ActivityTestRule<TokenActivity> activityRule = new ActivityTestRule<>(
            TokenActivity.class);

    @Test
    public void completesOnComma() throws Exception {
        onView(withId(R.id.searchView))
                .perform(typeText("mar,"))
                .check(matches(emailForPerson(2, is("marshall@example.com"))));
    }

    @Test
    public void doesntCompleteWithoutComma() throws Exception {
        onView(withId(R.id.searchView))
                .perform(typeText("mar"))
                .check(matches(tokenCount(is(2))));
    }
}