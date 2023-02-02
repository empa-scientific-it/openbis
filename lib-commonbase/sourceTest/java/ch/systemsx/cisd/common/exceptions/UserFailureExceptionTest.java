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

package ch.systemsx.cisd.common.exceptions;

import static org.testng.AssertJUnit.*;

import java.util.MissingFormatArgumentException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for the {@link UserFailureException}
 * 
 * @author Bernd Rinn
 */
public final class UserFailureExceptionTest
{

    /**
     * Test method for {@link ch.systemsx.cisd.common.exceptions.UserFailureException#fromTemplate(java.lang.String, java.lang.Object[])}.
     */
    @Test
    public final void testFromTemplateStringObjectArray()
    {
        UserFailureException exception =
                UserFailureException.fromTemplate("This is a %s exception to see how it works.",
                        "dummy");
        assertEquals("This is a dummy exception to see how it works.", exception.getMessage());
        // With not enough arguments.
        try
        {
            exception =
                    UserFailureException
                            .fromTemplate("This is a %s exception to see how it works.");
            fail("MissingFormatArgumentException must be thrown.");
        } catch (MissingFormatArgumentException e)
        {
            // Does nothing here.
        }
        // With more arguments than needed.
        exception =
                UserFailureException.fromTemplate("This is a %s exception to see how it works.",
                        "dummy", "more dummy");
        assertEquals("This is a dummy exception to see how it works.", exception.getMessage());
        exception = UserFailureException.fromTemplate("This is an exception to see how it works.");
        // No format needed.
        assertEquals("This is an exception to see how it works.", exception.getMessage());
    }

}
