/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class ConcurrentOperationLimiterConfigTest
{

    @Test
    public void testWithEmptyProperties()
    {
        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(new Properties());
        assertEquals(config.getTimeout(), 30 * DateUtils.MILLIS_PER_SECOND);
        assertEquals(config.getTimeoutAsync(), DateUtils.MILLIS_PER_DAY);
        assertEquals(config.getLimits(), Collections.emptyList());
    }

    @Test
    public void testWithTimeout()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "1000");
        properties.setProperty("concurrent-operation-limiter.timeout-async", "2000");
        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getTimeout(), 1000);
        assertEquals(config.getTimeoutAsync(), 2000);
    }

    @Test
    public void testWithTimeoutEmpty()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", " ");
        properties.setProperty("concurrent-operation-limiter.timeout-async", " ");
        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getTimeout(), 30 * DateUtils.MILLIS_PER_SECOND);
        assertEquals(config.getTimeoutAsync(), DateUtils.MILLIS_PER_DAY);
    }

    @Test
    public void testWithTimeoutNegative()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "-1000");
        properties.setProperty("concurrent-operation-limiter.timeout-async", "-2000");
        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getTimeout(), 30 * DateUtils.MILLIS_PER_SECOND);
        assertEquals(config.getTimeoutAsync(), DateUtils.MILLIS_PER_DAY);
    }

    @Test
    public void testWithTimeoutNaN()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.timeout", "abc");
        properties.setProperty("concurrent-operation-limiter.timeout-async", "def");
        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getTimeout(), 30 * DateUtils.MILLIS_PER_SECOND);
        assertEquals(config.getTimeoutAsync(), DateUtils.MILLIS_PER_DAY);
    }

    @Test
    public void testWithLimits()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.limits", "limit-1, limit-2");
        properties.setProperty("concurrent-operation-limiter.limit-1.operation", "test-1");
        properties.setProperty("concurrent-operation-limiter.limit-1.limit", "1");
        properties.setProperty("concurrent-operation-limiter.limit-2.operation", "test-2");
        properties.setProperty("concurrent-operation-limiter.limit-2.limit", "2");
        properties.setProperty("concurrent-operation-limiter.limit-3.operation", "test-3");
        properties.setProperty("concurrent-operation-limiter.limit-3.limit", "3");

        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getLimits().get(0).getOperation(), "test-1");
        assertEquals(config.getLimits().get(0).getLimit(), 1);
        assertEquals(config.getLimits().get(1).getOperation(), "test-2");
        assertEquals(config.getLimits().get(1).getLimit(), 2);
    }

    @Test
    public void testWithLimitsEmpty()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.limits", " ");

        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getLimits(), Collections.emptyList());
    }

    @Test
    public void testWithLimitNonexistent()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.limits", "i-dont-exist");

        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getLimits(), Collections.emptyList());
    }

    @Test
    public void testWithLimitWithNegativeLimit()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.limits", "limit");
        properties.setProperty("concurrent-operation-limiter.limit.operation", "test");
        properties.setProperty("concurrent-operation-limiter.limit.limit", "-1");

        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getLimits(), Collections.emptyList());
    }

    @Test
    public void testWithLimitWithEmptyLimit()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.limits", "limit");
        properties.setProperty("concurrent-operation-limiter.limit.operation", "test");
        properties.setProperty("concurrent-operation-limiter.limit.limit", " ");

        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getLimits(), Collections.emptyList());
    }

    @Test
    public void testWithLimitWithEmptyOperation()
    {
        Properties properties = new Properties();
        properties.setProperty("concurrent-operation-limiter.limits", "limit");
        properties.setProperty("concurrent-operation-limiter.limit.operation", " ");
        properties.setProperty("concurrent-operation-limiter.limit.limit", "1");

        ConcurrentOperationLimiterConfig config = new ConcurrentOperationLimiterConfig(properties);
        assertEquals(config.getLimits(), Collections.emptyList());
    }

}
