/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.util;

import org.apache.log4j.Level;

import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * Helper methods to create a log recorder which suppresses log entries from MonitoringPoolingDataSource and full text indexer.
 *
 * @author Franz-Josef Elmer
 */
public class LogRecordingUtils
{
    public static BufferedAppender createRecorder()
    {
        return suppress(new BufferedAppender());
    }

    public static BufferedAppender createRecorder(Level logLevel)
    {
        return suppress(new BufferedAppender(logLevel));
    }

    public static BufferedAppender createRecorder(String pattern, Level logLevel)
    {
        return suppress(new BufferedAppender(pattern, logLevel));
    }

    public static BufferedAppender createRecorder(String pattern, Level logLevel, String loggerNameRegex)
    {
        return suppress(new BufferedAppender(pattern, logLevel, loggerNameRegex));
    }

    private static BufferedAppender suppress(BufferedAppender logRecorder)
    {
        logRecorder.addRegexForLoggingEventsToBeDropped("OPERATION.*FullTextIndex.*");
        logRecorder.addRegexForLoggingEventsToBeDropped("MACHINE.MonitoringPoolingDataSource.*");
        logRecorder.addRegexForLoggingEventsToBeDropped("OPERATION.DynamicPropertyEvaluationRunnable.*");
        logRecorder.addRegexForLoggingEventsToBeDropped("OPERATION.DefaultBatchDynamicPropertyEvaluator.*");
        return logRecorder;
    }
}
