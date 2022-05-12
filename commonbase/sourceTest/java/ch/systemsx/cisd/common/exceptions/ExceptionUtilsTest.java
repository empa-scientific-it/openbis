/*
 * Copyright 2008 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.security.DigestException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Test cases for the {@link ExceptionUtilsTest}.
 * 
 * @author Christian Ribeaud
 */
public final class ExceptionUtilsTest
{

    private final void checkReturnedClientSafeException(final String message,
            final Exception rootException, final Exception clientSafeException,
            final boolean clientSafe)
    {
        if (clientSafe)
        {
            assertEquals(clientSafeException.toString(), rootException.toString());
        } else
        {
            assertTrue(clientSafeException instanceof MasqueradingException);
        }
        assertEquals(message, clientSafeException.getMessage());
        assertTrue(Arrays
                .equals(rootException.getStackTrace(), clientSafeException.getStackTrace()));
        if (rootException.getCause() != null)
        {
            assertTrue(clientSafeException.getCause() != null);
        }
    }

    @Test
    public final void testWithNullException()
    {
        try
        {
            ExceptionUtils.createMasqueradingExceptionIfNeeded(null, Collections.<String> emptyList());
            fail("Null exception not allowed.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testWithClientSafeExceptionOneLevel()
    {
        final String message = "Oooops!";
        final UserFailureException exception = new UserFailureException(message);
        final Exception clientSafeException =
                ExceptionUtils.createMasqueradingExceptionIfNeeded(exception, Collections
                        .<String> emptyList());
        checkReturnedClientSafeException(message, exception, clientSafeException, true);
    }

    @Test
    public final void testWithClientSafeExceptionOneLevelFromSpecifiedPackage()
    {
        final String message = "Oooops!";
        final Exception exception = new SAXException(message);
        final Exception clientSafeException =
                ExceptionUtils.createMasqueradingExceptionIfNeeded(exception, Arrays.asList("org.xml"));
        checkReturnedClientSafeException(message, exception, clientSafeException, true);
    }

    @Test
    public final void testWithNonClientSafeExceptionOneLevel()
    {
        final String message = "Oooops!";
        final Exception exception = new SAXException(message);
        final Exception clientSafeException =
                ExceptionUtils.createMasqueradingExceptionIfNeeded(exception, Collections
                        .<String> emptyList());
        checkReturnedClientSafeException(message, exception, clientSafeException, false);
    }

    @Test
    public final void testWithClientSafeExceptionMultipleLevel()
    {
        final String userFailureText = "Oooops!";
        final UserFailureException userFailureException = new UserFailureException(userFailureText);
        final String runtimeText = "Oooops! I did it again...";
        final RuntimeException runtimeException =
                new RuntimeException(runtimeText, userFailureException);
        final String unsupportedOperationText = "Wishiiiii!";
        final UnsupportedOperationException unsupportedOperationException =
                new UnsupportedOperationException(unsupportedOperationText, runtimeException);
        final Exception clientSafeException =
                ExceptionUtils.createMasqueradingExceptionIfNeeded(unsupportedOperationException,
                        Collections.<String> emptyList());
        checkReturnedClientSafeException(unsupportedOperationText, unsupportedOperationException,
                clientSafeException, true);
        checkReturnedClientSafeException(runtimeText, runtimeException,
                (Exception) clientSafeException.getCause(), true);
        checkReturnedClientSafeException(userFailureText, userFailureException,
                (Exception) clientSafeException.getCause().getCause(), true);
    }

    @Test
    public final void testWithNonClientSafeExceptionMultipleLevel()
    {
        final String saxExceptionText = "Oooops!";
        final SAXException saxException = new SAXException(saxExceptionText);
        final String runtimeText = "Oooops! I did it again...";
        final RuntimeException runtimeException = new RuntimeException(runtimeText, saxException);
        final String digestExceptionText = "Wishiiiii!";
        final DigestException digestException =
                new DigestException(digestExceptionText, runtimeException);
        final Exception clientSafeException =
                ExceptionUtils.createMasqueradingExceptionIfNeeded(digestException, Collections
                        .<String> emptyList());
        checkReturnedClientSafeException(digestExceptionText, digestException, clientSafeException,
                false);
        checkReturnedClientSafeException(runtimeText, runtimeException,
                (Exception) clientSafeException.getCause(), true);
        checkReturnedClientSafeException(saxExceptionText, saxException,
                (Exception) clientSafeException.getCause().getCause(), false);
    }

    @Test
    public final void testWithCheckedExceptionTunnel()
    {
        final String text = "Oooops!";
        final IOException ioException = new IOException(text);
        final RuntimeException checkedExceptionTunnel =
                CheckedExceptionTunnel.wrapIfNecessary(ioException);
        final Exception clientSafeException =
                ExceptionUtils.createMasqueradingExceptionIfNeeded(checkedExceptionTunnel,
                        Collections.<String> emptyList());
        assertNotSame(clientSafeException, checkedExceptionTunnel);
        assertNotSame(clientSafeException, ioException);
        assertTrue(clientSafeException instanceof MasqueradingException);
        final Throwable cause = clientSafeException.getCause();
        assertTrue(cause instanceof MasqueradingException);
        assertEquals(IOException.class.getName(), ((MasqueradingException) clientSafeException)
                .getRootExceptionClassName());
        assertEquals(text, cause.getMessage());
    }

    @Test
    public final void testTryGetThrowableOfClass()
    {
        boolean fail = true;
        try
        {
            ExceptionUtils.tryGetThrowableOfClass(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final ParseException parseException = new ParseException("", 2);
        final UserFailureException userFailureException =
                new UserFailureException("", new IllegalArgumentException(parseException));
        assertNull(ExceptionUtils.tryGetThrowableOfClass(userFailureException,
                UnsupportedOperationException.class));
        assertEquals(userFailureException, ExceptionUtils.tryGetThrowableOfClass(
                userFailureException, UserFailureException.class));
        assertEquals(parseException, ExceptionUtils.tryGetThrowableOfClass(userFailureException,
                ParseException.class));
        assertEquals(userFailureException, ExceptionUtils.tryGetThrowableOfClass(
                userFailureException, RuntimeException.class));
    }
}
