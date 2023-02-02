/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.utilities;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.MockLogger;

/**
 * @author Franz-Josef Elmer
 */
public class WaitingHelperTest extends AssertJUnit
{
    private static final class MockWaitingCondition implements IWaitingCondition
    {
        private int numberOfFalse;

        private int numberOfChecks;

        MockWaitingCondition(int numberOfFalse)
        {
            this.numberOfFalse = numberOfFalse;
        }

        @Override
        public boolean conditionFulfilled()
        {
            numberOfChecks++;
            return --numberOfFalse < 0;
        }

        public int getNumberOfChecks()
        {
            return numberOfChecks;
        }

        @Override
        public String toString()
        {
            return "Mock Condition";
        }

    }

    private static final class MockPause implements IPause
    {
        private long pauseTime;

        private MockPause(long pauseTime)
        {
            this.pauseTime = pauseTime;
        }

        @Override
        public long pause()
        {
            return pauseTime;
        }

        @Override
        public String toString()
        {
            return "Pausing " + pauseTime;
        }
    }

    @Test
    public void testConditionImmediatlelyFulfilled()
    {
        MockLogger logger = new MockLogger();
        WaitingHelper waitingHelper = new WaitingHelper(5 * DateUtils.MILLIS_PER_HOUR,
                3 * DateUtils.MILLIS_PER_SECOND, new MockTimeProvider(310000, 0), logger, true);
        MockWaitingCondition condition = new MockWaitingCondition(0);

        boolean success = waitingHelper.waitOn(condition);

        assertEquals("INFO: Condition fulfilled after < 1sec, condition: Mock Condition\n", logger.toString());
        assertEquals(true, success);
        assertEquals(1, condition.getNumberOfChecks());
    }

    @Test
    public void testNoTimeOut()
    {
        MockLogger logger = new MockLogger();
        WaitingHelper waitingHelper = new WaitingHelper(5 * DateUtils.MILLIS_PER_HOUR,
                3 * DateUtils.MILLIS_PER_SECOND, new MockTimeProvider(310000, 0), logger, true);
        MockWaitingCondition condition = new MockWaitingCondition(1000);

        boolean success = waitingHelper.waitOn(condition);

        assertEquals("INFO: Condition still not fulfilled after < 1sec, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 99sec, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 4min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 9min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 15min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 27min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 45min, condition: Mock Condition\n"
                + "INFO: Condition fulfilled after 50min, condition: Mock Condition\n", logger.toString());
        assertEquals(true, success);
        assertEquals(1001, condition.getNumberOfChecks());
    }

    @Test
    public void testTimeOut()
    {
        MockLogger logger = new MockLogger();
        WaitingHelper waitingHelper = new WaitingHelper(5 * DateUtils.MILLIS_PER_HOUR,
                3 * DateUtils.MILLIS_PER_SECOND, new MockTimeProvider(310000, 0), logger, true);
        MockWaitingCondition condition = new MockWaitingCondition(10000);

        boolean success = waitingHelper.waitOn(condition);

        assertEquals("INFO: Condition still not fulfilled after < 1sec, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 99sec, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 4min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 9min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 15min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 27min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 45min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 1h 14min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 2h 1min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 3h 1min, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 4h 1min, condition: Mock Condition\n", logger.toString());
        assertEquals(false, success);
        assertEquals(6000, condition.getNumberOfChecks());
    }

    @Test
    public void testPause()
    {
        MockLogger logger = new MockLogger();
        WaitingHelper waitingHelper = new WaitingHelper(null,
                10 * DateUtils.MILLIS_PER_SECOND, new MockTimeProvider(310000, 0), logger, true);
        MockWaitingCondition condition = new MockWaitingCondition(180);

        boolean success = waitingHelper.waitOn(300000, condition, new MockPause(9 * DateUtils.MILLIS_PER_SECOND));

        assertEquals("INFO: Condition still not fulfilled after 10sec, condition: Mock Condition\n"
                + "INFO: Condition still not fulfilled after 2min, condition: Mock Condition\n"
                + "INFO: Condition fulfilled after 3min, condition: Mock Condition\n", logger.toString());
        assertEquals(true, success);
        assertEquals(181, condition.getNumberOfChecks());
    }

}
