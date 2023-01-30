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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * Test cases for corresponding {@link DefaultReturnValueFilter} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = DefaultReturnValueFilter.class)
public final class DefaultReturnValueFilterTest
{

    private Mockery context;

    private DefaultReturnValueFilter defaultReturnValueFilter;

    private BufferedAppender logRecorder;

    private IValidator<Object> validator;

    private Level previousLevel;

    private final static Method getAnyMethod()
    {
        return Object.class.getDeclaredMethods()[0];
    }

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        context = new Mockery();
        validator = context.mock(IValidator.class);
        defaultReturnValueFilter = new DefaultReturnValueFilter(null);
        logRecorder = LogRecordingUtils.createRecorder("%m%n", Level.DEBUG);
        // Because 'log.xml' set the root logger level to INFO, we have to reset it here to DEBUG if
        // we want to catch the messages we are looking for.
        final Logger rootLogger = Logger.getRootLogger();
        previousLevel = rootLogger.getLevel();
        rootLogger.setLevel(Level.DEBUG);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        Logger.getRootLogger().setLevel(previousLevel);
    }

    @Test(groups = "broken")
    public final void testApplyFilterWithNull()
    {
        boolean fail = true;
        try
        {
            defaultReturnValueFilter.applyFilter(null, null, null);
        } catch (final AssertionError error)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test(groups = "broken")
    public final void testApplyFilterWithNullReturnValue()
    {
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "No filter applied on method 'Object.hashCode': return value is null.");
        final Object filtered =
                defaultReturnValueFilter.applyFilter(AuthorizationTestUtil.createSession(),
                        getAnyMethod(), null);
        appender.verifyLogHasHappened();
        assertNull(filtered);
    }

    @Test(groups = "broken")
    public final void testApplyFilterWithNoAnnotationFound()
    {
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "No filter applied on method 'Object.hashCode': "
                                + "no annotation 'ReturnValueFilter' found.");
        final Object filtered =
                defaultReturnValueFilter.applyFilter(AuthorizationTestUtil.createSession(),
                        getAnyMethod(), ObjectUtils.NULL);
        appender.verifyLogHasHappened();
        assertEquals(ObjectUtils.NULL, filtered);
    }

    @Test
    public final void testProceedWithValue()
    {
        final PersonPE person = AuthorizationTestUtil.createSession().tryGetPerson();
        final Null value = ObjectUtils.NULL;
        context.checking(new Expectations()
            {
                {
                    one(validator).isValid(person, value);
                    will(returnValue(false));
                }
            });
        final Object proceeded =
                defaultReturnValueFilter.proceed(person, getAnyMethod(), value, validator);
        assertNull(proceeded);
    }

    @SuppressWarnings("unchecked")
    @Test
    public final void testProceedWithList()
    {
        final PersonPE person = AuthorizationTestUtil.createSession().tryGetPerson();
        final List<String> list =
                new ArrayList<String>(Arrays.asList(StringUtilities.getStrings(2)));
        context.checking(new Expectations()
            {
                {
                    one(validator).isValid(person, list.get(0));
                    will(returnValue(true));

                    one(validator).isValid(person, list.get(1));
                    will(returnValue(false));
                }
            });
        final Object proceeded =
                defaultReturnValueFilter.proceed(person, getAnyMethod(), list, validator);
        assertTrue(proceeded instanceof List);
        assertEquals(1, ((List<String>) proceeded).size());
    }

    @Test
    public final void testProceedWithArray()
    {
        final PersonPE person = AuthorizationTestUtil.createSession().tryGetPerson();
        final String[] array = StringUtilities.getStrings(2);
        context.checking(new Expectations()
            {
                {
                    one(validator).isValid(person, array[0]);
                    will(returnValue(true));

                    one(validator).isValid(person, array[1]);
                    will(returnValue(true));
                }
            });
        final Object proceeded =
                defaultReturnValueFilter.proceed(person, getAnyMethod(), array, validator);
        System.out.println(proceeded);
        assertTrue(proceeded instanceof String[]);
        assertEquals(2, ((String[]) proceeded).length);
    }
}
