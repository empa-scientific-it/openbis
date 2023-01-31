/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * @author Franz-Josef Elmer
 */
public class SummaryUtils
{
    private static final String ADDED = "ADDED";

    private static final String REMOVED = "REMOVED";

    private static final String UPDATED = "UPDATED";

    private static final String INFO_MESSAGE = "The following %s have been %s";

    private static final Object SEPARATOR = "---------------------";

    public static void printShortSummaryHeader(Logger logger)
    {
        logger.info("/-------------- Short Summary --------------");
    }

    public static void printShortSummaryFooter(Logger logger)
    {
        logger.info("\\___________________________________________");
    }

    public static void printAddedSummary(Logger logger, Collection<String> details, String type)
    {
        printSummary(logger, details, type, ADDED);
    }

    public static void printUpdatedSummary(Logger logger, Collection<String> details, String type)
    {
        printSummary(logger, details, type, UPDATED);
    }

    public static void printRemovedSummary(Logger logger, Collection<String> details, String type)
    {
        printSummary(logger, details, type, REMOVED);
    }

    private static void printSummary(Logger logger, Collection<String> details, String type, String operation)
    {
        if (details.isEmpty())
        {
            return;
        }
        logger.info(SEPARATOR);
        logger.info(String.format(INFO_MESSAGE, type, operation));
        logger.info(SEPARATOR);
        for (String str : details)
        {
            logger.info(str);
        }
    }

    public static void printShortAddedSummary(Logger logger, int size, String type)
    {
        printShortSummary(logger, size, type, ADDED);
    }

    public static void printShortUpdatedSummary(Logger logger, int size, String type)
    {
        printShortSummary(logger, size, type, UPDATED);
    }

    public static void printShortRemovedSummary(Logger logger, int size, String type)
    {
        printShortSummary(logger, size, type, REMOVED);
    }
    
    public static void printShortSummary(Logger logger, int size, String type, String operation)
    {
        logger.info(String.format("| %7d %s %s", size, type, operation));
    }

    public static void printShortAddedSummaryDetail(Logger logger, int number, String subType)
    {
        printShortSummaryDetail(logger, number, subType, ADDED);
    }

    public static void printShortUpdatedSummaryDetail(Logger logger, int number, String subType)
    {
        printShortSummaryDetail(logger, number, subType, UPDATED);
    }

    public static void printShortRemovedSummaryDetail(Logger logger, int number, String subType)
    {
        printShortSummaryDetail(logger, number, subType, REMOVED);
    }

    private static void printShortSummaryDetail(Logger logger, int number, String subType, String operation)
    {
        logger.info(String.format("|          %7d %s " + operation, number, subType));

    }

}
