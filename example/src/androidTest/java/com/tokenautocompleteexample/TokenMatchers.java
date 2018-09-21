package com.tokenautocompleteexample;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Locale;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/** Convenience matchers to make it easier to check token view contents
 * Created by mgod on 8/25/17.
 */

class TokenMatchers {
    static Matcher<View> emailForPerson(final int position, final Matcher<String> stringMatcher) {
        checkNotNull(stringMatcher);
        return new BoundedMatcher<View, ContactsCompletionView>(ContactsCompletionView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format(Locale.US, "email for person %d: ", position));
                stringMatcher.describeTo(description);
            }
            @Override
            public boolean matchesSafely(ContactsCompletionView view) {
                if (view.getObjects().size() <= position) { return stringMatcher.matches(null); }
                return stringMatcher.matches(view.getObjects().get(position).getEmail());
            }
        };
    }

    static Matcher<View> tokenCount(final Matcher<Integer> intMatcher) {
        checkNotNull(intMatcher);
        return new BoundedMatcher<View, ContactsCompletionView>(ContactsCompletionView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("token count: ");
                intMatcher.describeTo(description);
            }
            @Override
            public boolean matchesSafely(ContactsCompletionView view) {
                return intMatcher.matches(view.getObjects().size());
            }
        };
    }
}
