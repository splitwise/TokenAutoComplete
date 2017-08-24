package com.tokenautocomplete;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;

/**
 * Tokenizer with configurable array of characters to tokenize on.
 *
 * Created on 2/3/15.
 * @author mgod
 */
public class CharacterTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    ArrayList<Character> splitChar;

    CharacterTokenizer(char[] splitChar){
        super();
        this.splitChar = new ArrayList<>(splitChar.length);
        for(char c : splitChar) this.splitChar.add(c);
    }

    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;

        while (i > 0 && !splitChar.contains(text.charAt(i - 1))) {
            i--;
        }
        while (i > 0 && text.charAt(i) == ' ') {
            i++;
        }

        return i;
    }

    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();

        while (i < len) {
            if (splitChar.contains(text.charAt(i))) {
                return i;
            } else {
                i++;
            }
        }

        return len;
    }

    public CharSequence terminateToken(CharSequence text) {
        int i = text.length();

        while (i > 0 && text.charAt(i - 1) == ' ') {
            i--;
        }

        if (i > 0 && splitChar.contains(text.charAt(i - 1))) {
            return text;
        } else {
            // Try not to use a space as a token character
            String token = (splitChar.size()>1 && splitChar.get(0)==' ' ? splitChar.get(1) : splitChar.get(0))+" ";
            if (text instanceof Spanned) {
                SpannableString sp = new SpannableString(text + token);
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                        Object.class, sp, 0);
                return sp;
            } else {
                return text + token;
            }
        }
    }
}
