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

package ch.systemsx.cisd.common.time;

import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.time.TimingParameters;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link TimingParameters}.
 * 
 * @author Bernd Rinn
 */
public class TimingParametersTest
{

    @Test
    public void testCreateTimingParametersDefaults()
    {
        final Properties props = new Properties();
        final TimingParameters parameters = TimingParameters.create(props);
        assertEquals(TimingParameters.getDefaultParameters(), parameters);
    }

    @Test
    public void testCreateTimingParameters()
    {
        final int timeout = 33;
        final int maxRetries = 7;
        final int failureInterval = 13;
        final Properties props = new Properties();
        props.put(TimingParameters.TIMEOUT_PROPERTY_NAME, Integer.toString(timeout));
        props.put(TimingParameters.MAX_RETRY_PROPERTY_NAME, Integer.toString(maxRetries));
        props.put(TimingParameters.FAILURE_INTERVAL_NAME, Integer.toString(failureInterval));
        final TimingParameters parameters = TimingParameters.create(props);
        assertEquals(timeout * DateUtils.MILLIS_PER_SECOND, parameters.getTimeoutMillis());
        assertEquals(maxRetries, parameters.getMaxRetriesOnFailure());
        assertEquals(failureInterval * DateUtils.MILLIS_PER_SECOND, parameters
                .getIntervalToWaitAfterFailureMillis());
    }

    @Test
    public void testHasTimingParametersFalse()
    {
        final Properties props = new Properties();
        assertFalse(TimingParameters.hasTimingParameters(props));
    }

    @Test
    public void testHasTimingParametersTrue()
    {
        final Properties props = new Properties();
        props.put(TimingParameters.TIMEOUT_PROPERTY_NAME, "30");
        assertTrue(TimingParameters.hasTimingParameters(props));
    }

    @Test
    public void testHasTimingParametersTrue2()
    {
        final Properties props = new Properties();
        props.put(TimingParameters.MAX_RETRY_PROPERTY_NAME, "2");
        assertTrue(TimingParameters.hasTimingParameters(props));
    }

    @Test
    public void testHasTimingParametersTrue3()
    {
        final Properties props = new Properties();
        props.put(TimingParameters.FAILURE_INTERVAL_NAME, "xxx");
        assertTrue(TimingParameters.hasTimingParameters(props));
    }
}
