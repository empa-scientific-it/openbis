/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.collection;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.common.collection.ValidatorUtils;

/**
 * Tests for {@link ValidatorUtils}.
 * 
 * @author Christian Ribeaud
 */
public final class ValidatorUtilsTest
{
    @Test
    public final void testConvertToRegEx()
    {
        String s = null;
        boolean exceptionThrown = false;
        try
        {
            ValidatorUtils.convertToRegEx(s);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Pattern can not be null.", exceptionThrown);

        s = "hello";
        String regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("hello", regEx);
        Pattern pattern = Pattern.compile(regEx);
        assertEquals(false, pattern.matcher("heelo").matches());
        assertEquals(true, pattern.matcher("hello").matches());

        s = "he?lo";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("he.lo", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(true, pattern.matcher("heelo").matches());
        assertEquals(false, pattern.matcher("helllo").matches());
        assertEquals(false, pattern.matcher("xhello").matches());

        s = "he?l";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("he.l", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(false, pattern.matcher("cheul").matches());
        assertEquals(true, pattern.matcher("heul").matches());
        assertEquals(false, pattern.matcher("hello").matches());
        assertEquals(false, pattern.matcher("helloo").matches());

        s = "he*o";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("he.*o", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(true, pattern.matcher("hello").matches());
        assertEquals(true, pattern.matcher("helllllllllllllllo").matches());
        assertEquals(true, pattern.matcher("helloo").matches());
        assertEquals(false, pattern.matcher("chelloo").matches());

        s = "h?l*o";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("h.l.*o", regEx);

        s = "h\\*ll*o";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("h\\*ll.*o", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(true, pattern.matcher("h*llooooooooooo").matches());
    }

    @Test
    public final void testCreateStarPatternValidator()
    {
        assertNull(ValidatorUtils
                .createCaseInsensitivePatternValidator(ArrayUtils.EMPTY_STRING_ARRAY));
        IValidator<String> validator = ValidatorUtils.createCaseInsensitivePatternValidator("he*");
        assertTrue(validator.isValid("he"));
        assertTrue(validator.isValid("hello"));
        assertFalse(validator.isValid("hullo"));
    }

    @Test
    public final void testCreateQuestionMarkPatternValidator()
    {
        IValidator<String> validator =
                ValidatorUtils.createCaseInsensitivePatternValidator("he?lo");
        assertFalse(validator.isValid("helo"));
        assertTrue(validator.isValid("hello"));
        assertTrue(validator.isValid("he lo"));
        assertTrue(validator.isValid("he.lo"));
        assertFalse(validator.isValid("he\nlo"));
    }

    @Test
    public final void testPatternValidatorIsCaseInsensitive()
    {
        IValidator<String> validator = ValidatorUtils.createCaseInsensitivePatternValidator("he*");
        assertTrue(validator.isValid("HELL"));
        assertTrue(validator.isValid("HeLL"));
        assertTrue(validator.isValid("hell"));
        assertFalse(validator.isValid("HiLL"));
    }
}