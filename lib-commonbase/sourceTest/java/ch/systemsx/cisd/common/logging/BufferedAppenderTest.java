/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import static org.testng.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 *
 */
public class BufferedAppenderTest
{

    @Test
    public void test()
    {
        // Given
        BufferedAppender appender = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        appender.addRegexForLoggingEventsToBeDropped("ab.*f");
        System.err.println(appender.getFilter());
        
        // Then
        appender.append(new LoggingEvent("my-class", LogManager.getRootLogger(), 123456, Level.INFO, 
                "testing", "my-thread", null, "ndc", new LocationInfo(null, null), null));
        appender.append(new LoggingEvent("my-class", LogManager.getRootLogger(), 123456, Level.INFO, 
                "abcdef", "my-thread", null, "ndc", new LocationInfo(null, null), null));
        
        // When
        assertEquals(appender.getLogContent(), "INFO  root - testing");
    }

}
