/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Class for profiling and debugging by developers. Stores debug information about logged events, allows to see duration time of logged events.
 * 
 * @author Tomasz Pylak
 */
public class ProfilingTable implements IProfilingTable
{
    private final ArrayList<ProfilingEventStarted> startEvents;

    private final HashMap<Integer/* task id */, Date/* stop timestamp */> stopEvents;

    public static IProfilingTable create(boolean isLoggingEnabled)
    {
        if (isLoggingEnabled)
        {
            return new ProfilingTable();
        } else
        {
            return createProfilingDummy();
        }
    }

    private static IProfilingTable createProfilingDummy()
    {
        return new IProfilingTable()
            {
                @Override
                public void clearLog()
                {
                }

                @Override
                public List<String> getLoggedEvents()
                {
                    return new ArrayList<String>();
                }

                @Override
                public boolean isLoggingEnabled()
                {
                    return false;
                }

                @Override
                public int log(String description)
                {
                    return 0;
                }

                @Override
                public void log(int taskId, String description)
                {
                }

                @Override
                public void logStop(int taskId)
                {
                }
            };
    }

    private ProfilingTable()
    {
        this.startEvents = new ArrayList<ProfilingEventStarted>();
        this.stopEvents = new HashMap<Integer, Date>();
    }

    @Override
    public boolean isLoggingEnabled()
    {
        return true;
    }

    private static class ProfilingEventStarted
    {
        private final Date timestamp;

        private final int taskId;

        private final String description;

        public ProfilingEventStarted(int taskId, String description)
        {
            this.timestamp = new Date();
            this.taskId = taskId;
            this.description = description;
        }

        public Date getTimestamp()
        {
            return timestamp;
        }

        public int getTaskId()
        {
            return taskId;
        }

        public String getDescription()
        {
            return description;
        }
    }

    /**
     * Logs the event.
     * 
     * @return id of the logged task. Use this id in {@link #logStop} method to measure the time between event start and stop.
     */
    @Override
    public int log(String description)
    {
        int taskId = startEvents.size();
        startEvents.add(new ProfilingEventStarted(taskId, description));
        return taskId;
    }

    /**
     * Logs the event with the specified id. Use {@link #log(String)} if you do not want to manage the events ids.
     */
    @Override
    public void log(int taskId, String description)
    {
        startEvents.add(new ProfilingEventStarted(taskId, description));
    }

    /**
     * Logs end of the event. It's cheap to call this method. Call this method only if you want to measure time between start and stop.
     */
    @Override
    public void logStop(int taskId)
    {
        stopEvents.put(taskId, new Date());
    }

    @Override
    public void clearLog()
    {
        startEvents.clear();
        stopEvents.clear();
    }

    @Override
    public List<String> getLoggedEvents()
    {
        List<String> result = new ArrayList<String>();
        for (ProfilingEventStarted event : startEvents)
        {
            Date stopDateOrNull = stopEvents.get(event.getTaskId());
            result.add(createEventDescription(event, stopDateOrNull));
        }
        return result;
    }

    private String createEventDescription(ProfilingEventStarted event, Date stopDateOrNull)
    {
        String eventDesc = DurationPrinter.printTime(event.getTimestamp());
        if (stopDateOrNull != null)
        {
            long durationInMilisec = stopDateOrNull.getTime() - event.getTimestamp().getTime();
            String durationText = DurationPrinter.printDuration(durationInMilisec);
            eventDesc +=
                    " [" + DurationPrinter.printTime(stopDateOrNull) + ": " + durationText + "]";
        }
        return eventDesc + " " + event.getDescription();
    }

    private static class DurationPrinter
    {
        public static String printTime(Date date)
        {
            long durationInMilisec = date.getTime();
            long milisec = getMilisecPart(durationInMilisec);
            long sec = getSecondsPart(durationInMilisec);
            long min = getMinutesPart(durationInMilisec);
            long hour = getHoursPart(durationInMilisec);
            return asTwoDigits(hour) + ":" + asTwoDigits(min) + ":" + asTwoDigits(sec) + "."
                    + asThreeDigits(milisec);
        }

        private static String asTwoDigits(long value)
        {
            if (value < 10)
            {
                return "0" + value;
            } else
            {
                return "" + value;
            }
        }

        private static String asThreeDigits(long value)
        {
            if (value < 10)
            {
                return "00" + value;
            } else if (value < 100)
            {
                return "0" + value;
            } else
            {
                return "" + value;
            }
        }

        public static String printDuration(long durationInMilisec)
        {
            long milisec = getMilisecPart(durationInMilisec);
            long sec = getSecondsPart(durationInMilisec);
            long min = getMinutesAll(durationInMilisec);
            String result = "";
            if (min > 0)
            {
                result += asTwoDigits(min) + "min ";
            }
            if (sec > 0)
            {
                result += asTwoDigits(sec) + "sec ";
            }
            result += asThreeDigits(milisec) + "msec";
            return result;
        }

        private static long getMilisecPart(long durationInMilisec)
        {

            return durationInMilisec % 1000;
        }

        private static long getSecondsPart(long durationInMilisec)
        {
            return (durationInMilisec / 1000) % 60;
        }

        private static long getMinutesPart(long durationInMilisec)
        {
            return (durationInMilisec / (1000 * 60)) % 60;
        }

        private static long getHoursPart(long durationInMilisec)
        {
            return ((durationInMilisec / (1000 * 60 * 60)) + 2) % 24;
        }

        private static long getMinutesAll(long durationInMilisec)
        {
            return (durationInMilisec / (1000 * 60));
        }
    }
}
