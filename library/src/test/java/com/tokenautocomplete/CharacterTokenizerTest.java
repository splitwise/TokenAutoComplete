package com.tokenautocomplete;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Make sure the tokenizer finds the right boundaries
 *
 * Created by mgod on 8/24/17.
 */

public class CharacterTokenizerTest {

    @Test
    public void handleWhiteSpaceWithCommaTokens() {
        char[] splits = {','};
        CharacterTokenizer tokenizer = new CharacterTokenizer(splits);
        String text = "bears, ponies";
        assertEquals(0, tokenizer.findTokenStart(text, 0));
        assertEquals(0, tokenizer.findTokenStart(text, 1));
        assertEquals(0, tokenizer.findTokenStart(text, 5));
        assertEquals(7, tokenizer.findTokenStart(text, 6));
        assertEquals(7, tokenizer.findTokenStart(text, 7));
        assertEquals(7, tokenizer.findTokenStart(text, 11));

        assertEquals(5, tokenizer.findTokenEnd(text, 0));
        assertEquals(5, tokenizer.findTokenEnd(text, 4));
        assertEquals(5, tokenizer.findTokenEnd(text, 5));
        assertEquals(13, tokenizer.findTokenEnd(text, 6));
        assertEquals(13, tokenizer.findTokenEnd(text, 10));
        assertEquals(13, tokenizer.findTokenEnd(text, 11));
    }

    @Test
    public void handleWhiteSpaceWithWhitespaceTokens() {
        char[] splits = {' '};
        CharacterTokenizer tokenizer = new CharacterTokenizer(splits);
        String text = "bears ponies";
        assertEquals(0, tokenizer.findTokenStart(text, 0));
        assertEquals(0, tokenizer.findTokenStart(text, 1));
        assertEquals(0, tokenizer.findTokenStart(text, 5));
        assertEquals(6, tokenizer.findTokenStart(text, 6));
        assertEquals(6, tokenizer.findTokenStart(text, 7));
        assertEquals(6, tokenizer.findTokenStart(text, 11));

        assertEquals(5, tokenizer.findTokenEnd(text, 0));
        assertEquals(5, tokenizer.findTokenEnd(text, 4));
        assertEquals(5, tokenizer.findTokenEnd(text, 5));
        assertEquals(12, tokenizer.findTokenEnd(text, 6));
        assertEquals(12, tokenizer.findTokenEnd(text, 10));
        assertEquals(12, tokenizer.findTokenEnd(text, 11));
    }

    @Test
    public void handleLotsOfWhitespace() {
        char[] splits = {','};
        CharacterTokenizer tokenizer = new CharacterTokenizer(splits);
        String text = "bears,      ponies     ,another";
        assertEquals(12, tokenizer.findTokenStart(text, 6));
        assertEquals(12, tokenizer.findTokenStart(text, 7));
        assertEquals(12, tokenizer.findTokenStart(text, 18));
        assertEquals(12, tokenizer.findTokenStart(text, 23));
        assertEquals(24, tokenizer.findTokenStart(text, 24));

        assertEquals(23, tokenizer.findTokenEnd(text, 6));
        assertEquals(23, tokenizer.findTokenEnd(text, 7));
        assertEquals(23, tokenizer.findTokenEnd(text, 18));
        assertEquals(23, tokenizer.findTokenEnd(text, 23));
        assertEquals(31, tokenizer.findTokenEnd(text, 24));
    }
}
