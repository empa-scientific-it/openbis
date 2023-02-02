/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.event.BooleanEvent;
import ch.systemsx.cisd.common.logging.event.LongEvent;
import ch.systemsx.cisd.common.utilities.TestResources;

/**
 * @author pkupczyk
 */
public class ControlFileBasedTest
{

    @BeforeMethod
    protected void beforeMethod()
    {
        deleteControlFileDirectory();
        getControlFileDirectory().mkdirs();
    }

    @AfterTest
    protected void afterTest()
    {
        deleteControlFileDirectory();
    }

    protected void testTriggerBooleanEvent(String eventName, IDelegatedActionWithResult<BooleanEvent> getEventAction) throws IOException
    {
        BooleanEvent event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName).createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertNull(event.getValue());

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName + "-on").createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertTrue(event.getValue());

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName + "-off").createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertFalse(event.getValue());

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName + "-invalid").createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        File onFile = new File(getControlFileDirectory(), eventName + "-on");
        File offFile = new File(getControlFileDirectory(), eventName + "-off");

        onFile.createNewFile();
        offFile.createNewFile();

        onFile.setLastModified(System.currentTimeMillis() - 5000);
        offFile.setLastModified(System.currentTimeMillis());

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertFalse(event.getValue());

        event = getEventAction.execute(true);
        Assert.assertNull(event);
    }

    protected void testTriggerLongEvent(String eventName, IDelegatedActionWithResult<LongEvent> getEventAction) throws IOException
    {
        LongEvent event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName).createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertNull(event.getValue());

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName + "-100").createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertEquals(event.getValue(), Long.valueOf(100));

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName + "-101").createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertEquals(event.getValue(), Long.valueOf(101));

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        new File(getControlFileDirectory(), eventName + "-invalid").createNewFile();

        event = getEventAction.execute(true);
        Assert.assertNull(event);

        File onFile = new File(getControlFileDirectory(), eventName + "-102");
        File offFile = new File(getControlFileDirectory(), eventName + "-103");

        onFile.createNewFile();
        offFile.createNewFile();

        onFile.setLastModified(System.currentTimeMillis() - 5000);
        offFile.setLastModified(System.currentTimeMillis());

        event = getEventAction.execute(true);
        Assert.assertNotNull(event);
        Assert.assertEquals(event.getValue(), Long.valueOf(103));

        event = getEventAction.execute(true);
        Assert.assertNull(event);
    }

    protected void testSwitchBooleanParameter(String parameterName, boolean parameterDefault,
            IDelegatedActionWithResult<Boolean> getParameterValueAction) throws IOException
    {
        Assert.assertEquals(getParameterValueAction.execute(true), Boolean.valueOf(parameterDefault));

        new File(getControlFileDirectory(), parameterName + "-on").createNewFile();
        Assert.assertTrue(getParameterValueAction.execute(true));

        new File(getControlFileDirectory(), parameterName + "-invalid").createNewFile();
        Assert.assertTrue(getParameterValueAction.execute(true));

        new File(getControlFileDirectory(), parameterName + "-off").createNewFile();
        Assert.assertFalse(getParameterValueAction.execute(true));

        new File(getControlFileDirectory(), parameterName + "-invalid").createNewFile();
        Assert.assertFalse(getParameterValueAction.execute(true));

        File onFile = new File(getControlFileDirectory(), parameterName + "-on");
        File offFile = new File(getControlFileDirectory(), parameterName + "-off");

        onFile.createNewFile();
        offFile.createNewFile();

        onFile.setLastModified(System.currentTimeMillis() - 5000);
        offFile.setLastModified(System.currentTimeMillis());

        Assert.assertFalse(getParameterValueAction.execute(true));
    }

    protected void testSwitchLongParameter(String parameterName, long parameterDefault,
            IDelegatedActionWithResult<Long> getParameterValueAction) throws IOException
    {
        Assert.assertEquals(getParameterValueAction.execute(true), Long.valueOf(parameterDefault));

        new File(getControlFileDirectory(), parameterName + "-100").createNewFile();
        Assert.assertEquals(getParameterValueAction.execute(true), Long.valueOf(100));

        new File(getControlFileDirectory(), parameterName + "-invalid").createNewFile();
        Assert.assertEquals(getParameterValueAction.execute(true), Long.valueOf(100));

        new File(getControlFileDirectory(), parameterName + "-101").createNewFile();
        Assert.assertEquals(getParameterValueAction.execute(true), Long.valueOf(101));

        new File(getControlFileDirectory(), parameterName + "-invalid").createNewFile();
        Assert.assertEquals(getParameterValueAction.execute(true), Long.valueOf(101));

        File onFile = new File(getControlFileDirectory(), parameterName + "-102");
        File offFile = new File(getControlFileDirectory(), parameterName + "-103");

        onFile.createNewFile();
        offFile.createNewFile();

        onFile.setLastModified(System.currentTimeMillis() - 5000);
        offFile.setLastModified(System.currentTimeMillis());

        Assert.assertEquals(getParameterValueAction.execute(true), Long.valueOf(103));
    }

    protected File getControlFileDirectory()
    {
        TestResources resources = new TestResources(getClass());
        return resources.getResourcesDirectory();
    }

    protected void deleteControlFileDirectory()
    {
        File directory = getControlFileDirectory();
        if (directory.exists())
        {
            FileUtilities.deleteRecursively(directory);
        }
    }

}
