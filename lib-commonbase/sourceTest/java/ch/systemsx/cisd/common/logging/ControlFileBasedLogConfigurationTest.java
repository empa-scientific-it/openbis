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
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.logging.event.BooleanEvent;
import ch.systemsx.cisd.common.logging.event.LongEvent;

/**
 * @author pkupczyk
 */
public class ControlFileBasedLogConfigurationTest extends ControlFileBasedTest
{

    private static final String TEST_EVENT_NAME = "test-event";

    private static final String TEST_PARAMETER_NAME = "test-parameter";

    private static final String UNKNOWN_EVENT_NAME = "unknown-event";

    private static final String UNKNOWN_PARAMETER_NAME = "unknown-parameter";

    @Test
    public void testTriggerBooleanEvent() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanEvent(TEST_EVENT_NAME);

        testTriggerBooleanEvent(TEST_EVENT_NAME, new IDelegatedActionWithResult<BooleanEvent>()
            {

                @Override
                public BooleanEvent execute(boolean didOperationSucceed)
                {
                    return config.getBooleanEvent(TEST_EVENT_NAME);
                }
            });
    }

    @Test
    public void testTriggerLongEvent() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addLongEvent(TEST_EVENT_NAME);

        testTriggerLongEvent(TEST_EVENT_NAME, new IDelegatedActionWithResult<LongEvent>()
            {

                @Override
                public LongEvent execute(boolean didOperationSucceed)
                {
                    return config.getLongEvent(TEST_EVENT_NAME);
                }
            });
    }

    @Test
    public void testTriggerUnknownEvent() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanEvent(TEST_EVENT_NAME);

        Assert.assertNull(config.getBooleanEvent(TEST_EVENT_NAME));
        Assert.assertNull(config.getBooleanEvent(UNKNOWN_EVENT_NAME));

        File testEventFile = new File(getControlFileDirectory(), TEST_EVENT_NAME);
        File unknownEventFile = new File(getControlFileDirectory(), UNKNOWN_EVENT_NAME);

        testEventFile.createNewFile();
        unknownEventFile.createNewFile();

        Assert.assertNotNull(config.getBooleanEvent(TEST_EVENT_NAME));
        Assert.assertNull(config.getBooleanEvent(UNKNOWN_EVENT_NAME));

        Assert.assertFalse(testEventFile.exists());
        Assert.assertTrue(unknownEventFile.exists());
    }

    @Test
    public void testTriggerEventsWithSamePrefixes() throws IOException
    {
        final String SOME = "some";
        final String SOME_EVENT = "some-event";
        final String SOME_EVENT_NAME = "some-event-name";

        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanEvent(SOME);
        config.addBooleanEvent(SOME_EVENT);
        config.addBooleanEvent(SOME_EVENT_NAME);

        Assert.assertNull(config.getBooleanEvent(SOME));
        Assert.assertNull(config.getBooleanEvent(SOME));
        Assert.assertNull(config.getBooleanEvent(SOME));

        new File(getControlFileDirectory(), "some-on").createNewFile();

        Assert.assertNotNull(config.getBooleanEvent(SOME));
        Assert.assertNull(config.getBooleanEvent(SOME_EVENT));
        Assert.assertNull(config.getBooleanEvent(SOME_EVENT_NAME));

        new File(getControlFileDirectory(), "some-event-on").createNewFile();

        Assert.assertNull(config.getBooleanEvent(SOME));
        Assert.assertNotNull(config.getBooleanEvent(SOME_EVENT));
        Assert.assertNull(config.getBooleanEvent(SOME_EVENT_NAME));

        new File(getControlFileDirectory(), "some-event-name-on").createNewFile();

        Assert.assertNull(config.getBooleanEvent(SOME));
        Assert.assertNull(config.getBooleanEvent(SOME_EVENT));
        Assert.assertNotNull(config.getBooleanEvent(SOME_EVENT_NAME));
    }

    @Test
    public void testSwitchBooleanParameter() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(TEST_PARAMETER_NAME, true);

        testSwitchBooleanParameter(TEST_PARAMETER_NAME, true, new IDelegatedActionWithResult<Boolean>()
            {
                @Override
                public Boolean execute(boolean didOperationSucceed)
                {
                    return config.getBooleanParameterValue(TEST_PARAMETER_NAME);
                }
            });
    }

    @Test
    public void testSwitchLongParameter() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addLongParameter(TEST_PARAMETER_NAME, 123L);

        testSwitchLongParameter(TEST_PARAMETER_NAME, 123L, new IDelegatedActionWithResult<Long>()
            {
                @Override
                public Long execute(boolean didOperationSucceed)
                {
                    return config.getLongParameterValue(TEST_PARAMETER_NAME);
                }
            });
    }

    @Test
    public void testSwitchUnknownParameter() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(TEST_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNull(config.getBooleanParameterValue(UNKNOWN_PARAMETER_NAME));

        File testParameterFile = new File(getControlFileDirectory(), TEST_PARAMETER_NAME + "-on");
        File unknownParameterFile = new File(getControlFileDirectory(), UNKNOWN_PARAMETER_NAME);

        testParameterFile.createNewFile();
        unknownParameterFile.createNewFile();

        Assert.assertTrue(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNull(config.getBooleanParameterValue(UNKNOWN_PARAMETER_NAME));

        Assert.assertFalse(testParameterFile.exists());
        Assert.assertTrue(unknownParameterFile.exists());
    }

    @Test
    public void testSwitchAndTriggerMixed() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(TEST_PARAMETER_NAME, false);
        config.addBooleanEvent(TEST_EVENT_NAME);

        Assert.assertFalse(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNull(config.getBooleanEvent(TEST_EVENT_NAME));

        new File(getControlFileDirectory(), TEST_PARAMETER_NAME + "-on").createNewFile();
        new File(getControlFileDirectory(), TEST_EVENT_NAME).createNewFile();

        Assert.assertTrue(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNotNull(config.getBooleanEvent(TEST_EVENT_NAME));

        Assert.assertTrue(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNull(config.getBooleanEvent(TEST_EVENT_NAME));

        new File(getControlFileDirectory(), TEST_PARAMETER_NAME + "-off").createNewFile();

        Assert.assertFalse(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNull(config.getBooleanEvent(TEST_EVENT_NAME));

        Assert.assertFalse(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
        Assert.assertNull(config.getBooleanEvent(TEST_EVENT_NAME));
    }

    @Test
    public void testNotExistingControlDirectory() throws IOException
    {
        File notExistingFile = new File(getControlFileDirectory(), "notExisting");

        ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(notExistingFile, -1);
        config.addBooleanParameter(TEST_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
    }

    @Test
    public void testControlDirectoryThatIsAFile() throws IOException
    {
        File file = new File(getControlFileDirectory(), "aFile");
        file.createNewFile();

        ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(file, -1);
        config.addBooleanParameter(TEST_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(TEST_PARAMETER_NAME));
    }

}
