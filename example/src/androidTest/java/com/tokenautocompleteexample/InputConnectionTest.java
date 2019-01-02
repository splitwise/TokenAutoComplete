package com.tokenautocompleteexample;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.tokenautocompleteexample.TokenMatchers.emailForPerson;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by mgod on 11/29/17.
 */

@RunWith(AndroidJUnit4.class)
public class InputConnectionTest {

    @Rule
    public ActivityTestRule<TestCleanTokenActivity> activityRule = new ActivityTestRule<>(
            TestCleanTokenActivity.class);

    @Test
    public void ignoresComposingTextFromHint() throws Exception {
        onView(withId(R.id.searchView))
                .check(matches(withText(containsString("enter"))))
                .perform(typeText("asjdfka "))
                .perform(forceComposingText("enter"))
                .check(matches(withText(not(containsString("enter")))))
                .check(matches(withText(containsString("To:"))));
    }

    @Test
    public void keepsNonHintComposingText() throws Exception {
        onView(withId(R.id.searchView))
                .check(matches(withText(containsString("enter"))))
                .perform(click())
                .perform(forceComposingText("bears"))
                .check(matches(withText(containsString("bears"))))
                .check(matches(withText(not(containsString("enter")))))
                .check(matches(withText(containsString("To:"))));
    }

    @Test
    public void keepsCloseNonHintComposingText() throws Exception {
        onView(withId(R.id.searchView))
                .check(matches(withText(containsString("enter"))))
                .perform(click())
                .perform(forceComposingText("ente"))
                .check(matches(withText(containsString("ente"))))
                .check(matches(withText(not(containsString("enter")))))
                .check(matches(withText(containsString("To:"))));
    }

    @Test
    public void suppressesErroneousThreeLetterPreviousCompletions() throws Exception {
        onView(withId(R.id.searchView))
                .perform(typeText("mar,"))
                .check(matches(emailForPerson(0, is("marshall@example.com"))))
                .perform(forceComposingText("marz"))
                .check(matches(withText(not(containsString("mar")))))
                .check(matches(withText(containsString("z"))));
    }

    @Test
    public void keepsReasonableThreeLetterCompletions() throws Exception {
        onView(withId(R.id.searchView))
                .perform(typeText("mar,"))
                .check(matches(emailForPerson(0, is("marshall@example.com"))))
                .perform(forceComposingText("maaz"))
                .check(matches(withText(containsString("maaz"))));
    }

    @Test
    public void ignoresSpacesWhenTheyAreAddedToSplitChars() {
        //Many keyboards add "helpful" spaces after punctuation and we detect single characters
        //for completions, so we need to ignore these spaces when looking for split characters
        onView(withId(R.id.searchView))
                .perform(typeText("mar"))
                .perform(forceCommitText(", "))
                .check(matches(emailForPerson(0, is("marshall@example.com"))));
    }

    //This is to emulate the behavior of some keyboards (Google Android O) to choose unusual text
    public static ViewAction forceComposingText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(ContactsCompletionView.class);
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ContactsCompletionView completionView = (ContactsCompletionView)view;
                InputConnection connection = completionView.testAccessibleInputConnection;
                connection.setComposingText(text, -1);
            }
        };
    }

    //This is to emulate the comma + space behavior of the Fleksy keyboard
    public static ViewAction forceCommitText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(ContactsCompletionView.class);
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ContactsCompletionView completionView = (ContactsCompletionView)view;
                InputConnection connection = completionView.testAccessibleInputConnection;
                connection.commitText(text, 1);
            }
        };
    }
}
