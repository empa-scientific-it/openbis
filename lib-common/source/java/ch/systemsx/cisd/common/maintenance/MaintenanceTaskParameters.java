/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.maintenance;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.support.CronSequenceGenerator;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * @author Izabela Adamczyk
 */
public class MaintenanceTaskParameters
{
    public static final String RUN_SCHEDULE_KEY = "run-schedule";

    public static final String RUN_SCHEDULE_FILE_KEY = "run-schedule-file";

    public static final String RETRY_INTERVALS_AFTER_FAILURE_KEY = "retry-intervals-after-failure";

    public static final String CLASS_KEY = "class";

    public static final String INTERVAL_KEY = "interval";

    static final String CRON_PREFIX = "cron:";

    static final String TIME_FORMAT = "HH:mm";

    static final int ONE_DAY_IN_SEC = 60 * 60 * 24;

    static final String START_KEY = "start";

    // If true the task will be executed exactly one, interval will be ignored. By default set to
    // false.
    public static final String ONE_TIME_EXECUTION_KEY = "execute-only-once";

    private final String pluginName;

    private final long interval;

    private final String className;

    private final Properties properties;

    private final Date startDate;

    private final boolean executeOnlyOnce;

    private final List<Long> retryIntervals;

    private INextTimestampProvider nextTimestampProvider;

    private File persistentNextDateFile;

    public MaintenanceTaskParameters(Properties properties, String pluginName)
    {
        this.properties = properties;
        this.pluginName = pluginName;
        interval = DateTimeUtils.getDurationInMillis(properties, INTERVAL_KEY, DateUtils.MILLIS_PER_DAY) / 1000;
        className = PropertyUtils.getMandatoryProperty(properties, CLASS_KEY);
        startDate = extractStartDate(PropertyUtils.getProperty(properties, START_KEY));
        executeOnlyOnce = PropertyUtils.getBoolean(properties, ONE_TIME_EXECUTION_KEY, false);
        List<Long> intervals = getRetryIntervals(properties);
        this.retryIntervals = intervals;
        String runScheduleDescription = properties.getProperty(RUN_SCHEDULE_KEY, null);
        if (runScheduleDescription != null)
        {
            nextTimestampProvider = createNextTimestampProvider(runScheduleDescription);
            String defaultPath = getPersistenNextDateFile(pluginName, className).getAbsolutePath();
            persistentNextDateFile = new File(properties.getProperty(RUN_SCHEDULE_FILE_KEY, defaultPath));
        }
    }

    private static Date extractStartDate(String timeOrNull)
    {
        try
        {
            if (StringUtils.isBlank(timeOrNull))
            {
                return Calendar.getInstance().getTime();
            }
            DateFormat format = new SimpleDateFormat(TIME_FORMAT);
            Date parsedDate = format.parse(timeOrNull);
            Calendar rightHourAndMinutes1970 = Calendar.getInstance();
            rightHourAndMinutes1970.setTime(parsedDate);
            Calendar result = Calendar.getInstance();
            result.set(Calendar.HOUR_OF_DAY, rightHourAndMinutes1970.get(Calendar.HOUR_OF_DAY));
            result.set(Calendar.MINUTE, rightHourAndMinutes1970.get(Calendar.MINUTE));
            Calendar now = Calendar.getInstance();
            if (now.after(result))
            {
                result.add(Calendar.DAY_OF_MONTH, 1);
            }
            return result.getTime();
        } catch (ParseException ex)
        {
            throw new ConfigurationFailureException(String.format(
                    "Start date <%s> does not match the required format <%s>", timeOrNull,
                    TIME_FORMAT));
        }
    }

    private static List<Long> getRetryIntervals(Properties properties)
    {
        String retryIntervals = properties.getProperty(RETRY_INTERVALS_AFTER_FAILURE_KEY);
        List<Long> intervals = new ArrayList<>();
        if (StringUtils.isNotBlank(retryIntervals))
        {
            for (String retryInterval : retryIntervals.split(","))
            {
                intervals.add(DateTimeUtils.parseDurationToMillis(retryInterval));
            }
        }
        return intervals;
    }

    private static File getPersistenNextDateFile(String pluginName, String className)
    {
        return new File(findFolderForPersistentNextDateFile(), pluginName + "_" + className);
    }

    private static File findFolderForPersistentNextDateFile()
    {
        File etc = new File(System.getProperty("user.dir"), "etc");
        for (File file = etc; file != null; file = file.getParentFile())
        {
            if (file.getName().equals("servers"))
            {
                return file.getParentFile();
            }
        }
        return etc;
    }

    public boolean isExecuteOnlyOnce()
    {
        return executeOnlyOnce;
    }

    public long getIntervalSeconds()
    {
        return interval;
    }

    public String getClassName()
    {
        return className;
    }

    public String getPluginName()
    {
        return pluginName;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public List<Long> getRetryIntervals()
    {
        return retryIntervals;
    }

    public INextTimestampProvider getNextTimestampProvider()
    {
        return nextTimestampProvider;
    }

    public File getPersistentNextDateFile()
    {
        return persistentNextDateFile;
    }

    private static final INextTimestampProvider createNextTimestampProvider(String runScheduleDescription)
    {
        if (runScheduleDescription.startsWith(CRON_PREFIX))
        {
            CronSequenceGenerator cronSequenceGenerator =
                    new CronSequenceGenerator(runScheduleDescription.substring(CRON_PREFIX.length()));
            return new CronSequenceBaseNextTimestampProvider(cronSequenceGenerator);
        }
        return new NextTimestampProviderCollection(runScheduleDescription);

    }

    private static final class CronSequenceBaseNextTimestampProvider implements INextTimestampProvider
    {
        private CronSequenceGenerator cronSequenceGenerator;

        CronSequenceBaseNextTimestampProvider(CronSequenceGenerator cronSequenceGenerator)
        {
            this.cronSequenceGenerator = cronSequenceGenerator;
        }

        @Override
        public Date getNextTimestamp(Date timestamp)
        {
            return cronSequenceGenerator.next(timestamp);
        }
    }

    private static final class NextTimestampProviderCollection implements INextTimestampProvider
    {
        private List<INextTimestampProvider> providers = new ArrayList<>();

        NextTimestampProviderCollection(String definition)
        {
            String[] splittedDefinition = definition.split(",");
            for (String def : splittedDefinition)
            {
                providers.add(new SimpleNextTimestampProvider(def));
            }
        }

        @Override
        public Date getNextTimestamp(Date timestamp)
        {
            Date bestNext = null;
            for (INextTimestampProvider provider : providers)
            {
                Date next = provider.getNextTimestamp(timestamp);
                if (bestNext == null || next.before(bestNext))
                {
                    bestNext = next;
                }
            }
            return bestNext;
        }
    }

    private static final class SimpleNextTimestampProvider implements INextTimestampProvider
    {
        private static final INextDayFactory[] NEXT_DAY_FACTORIES = {
                new NextWeekDayFactory(),
                new NextMonthDayFactory(),
                new NextWeekDayInMonthFactory(),
                new NextDayInYearFactory(),
                new AnyDayFactory() };

        private int hour;

        private int minute;

        private INextDay nextDay;

        SimpleNextTimestampProvider(String definition)
        {
            try
            {
                String[] splitted = StringUtils.split(definition);
                String[] splittedTime = splitted[splitted.length - 1].split(":");
                hour = Integer.parseInt(splittedTime[0]);
                minute = splittedTime.length > 1 ? Integer.parseInt(splittedTime[1]) : 0;
                nextDay = createNextDay(splitted);
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid property '" + RUN_SCHEDULE_KEY
                        + "' (Reason: " + e.getMessage() + "): " + definition, e);
            }
        }

        private INextDay createNextDay(String[] splitted)
        {
            List<Object> descriptors = new ArrayList<>();
            if (splitted.length > 1)
            {
                for (int i = 0; i < splitted.length - 1; i++)
                {
                    for (String string : splitted[i].split("\\."))
                    {
                        parseDescriptor(descriptors, string);
                    }
                }
            }
            for (INextDayFactory factory : NEXT_DAY_FACTORIES)
            {
                if (factory.accept(descriptors))
                {
                    return factory.create(descriptors);
                }
            }
            throw new IllegalArgumentException("Invalid description");
        }

        private void parseDescriptor(List<Object> descriptors, String descriptorString)
        {
            try
            {
                descriptors.add(Integer.parseInt(descriptorString));
            } catch (NumberFormatException e)
            {
                WeekDay weekDay = WeekDay.get(descriptorString);
                if (weekDay != null)
                {
                    descriptors.add(weekDay);
                } else
                {
                    Month month = Month.get(descriptorString);
                    if (month != null)
                    {
                        descriptors.add(month);
                    } else
                    {
                        throw new IllegalArgumentException("Neither a number nor a 3-letter month "
                                + "nor a 2-letter week day nor a 3-letter week day: " + descriptorString);
                    }
                }

            }
        }

        @Override
        public Date getNextTimestamp(Date timestamp)
        {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis((timestamp.getTime() / 1000) * 1000);
            calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
            calendar.set(GregorianCalendar.MINUTE, minute);
            calendar.set(GregorianCalendar.SECOND, 0);
            Date next = calendar.getTime();
            if (nextDay.isValidDay(calendar) && next.after(timestamp))
            {
                return next;
            }
            nextDay.setNextPossibleDay(calendar);
            return calendar.getTime();
        }
    }

    private enum WeekDay
    {
        SU(1), SUN(1), MO(2), MON(2), TU(3), TUE(3), WE(4), WED(4), TH(5), THU(5), FR(6), FRI(6), SA(7), SAT(7);

        private int number;

        WeekDay(int number)
        {
            this.number = number;
        }

        public int getNumber()
        {
            return number;
        }

        static WeekDay get(String string)
        {
            for (WeekDay weekDay : values())
            {
                if (weekDay.toString().equalsIgnoreCase(string))
                {
                    return weekDay;
                }
            }
            return null;
        }
    }

    private enum Month
    {
        JAN(0), FEB(1), MAR(2), APR(3), MAY(4), JUN(5), JUL(6), AUG(7), SEP(8), OCT(9), NOV(10), DEC(11);

        private int number;

        Month(int number)
        {
            this.number = number;
        }

        public int getNumber()
        {
            return number;
        }

        static Month get(String string)
        {
            for (Month month : values())
            {
                if (month.toString().equalsIgnoreCase(string))
                {
                    return month;
                }
            }
            return null;
        }
    }

    private static interface INextDay
    {
        public boolean isValidDay(GregorianCalendar calendar);

        public void setNextPossibleDay(GregorianCalendar calendar);
    }

    private static interface INextDayFactory
    {
        public boolean accept(List<Object> descriptors);

        public INextDay create(List<Object> descriptors);
    }

    private static class NextWeekDayFactory implements INextDayFactory
    {
        @Override
        public boolean accept(List<Object> descriptors)
        {
            return descriptors.size() == 1 && descriptors.get(0) instanceof WeekDay;
        }

        @Override
        public INextDay create(List<Object> descriptors)
        {
            WeekDay weekDay = (WeekDay) descriptors.get(0);
            return new AnyDay()
                {
                    @Override
                    public boolean isValidDay(GregorianCalendar calendar)
                    {
                        return calendar.get(GregorianCalendar.DAY_OF_WEEK) == weekDay.getNumber();
                    }

                    @Override
                    public void setNextPossibleDay(GregorianCalendar calendar)
                    {
                        int diff = weekDay.getNumber() - calendar.get(GregorianCalendar.DAY_OF_WEEK);
                        if (diff <= 0)
                        {
                            diff += 7;
                        }
                        setNextPossibleDay(calendar, diff);
                    }
                };
        }
    }

    private static class NextMonthDayFactory implements INextDayFactory
    {
        @Override
        public boolean accept(List<Object> descriptors)
        {
            return descriptors.size() == 1 && descriptors.get(0) instanceof Integer;
        }

        @Override
        public INextDay create(List<Object> descriptors)
        {
            int monthDay = ((Integer) descriptors.get(0)).intValue();
            return new AnyDay()
                {
                    @Override
                    public boolean isValidDay(GregorianCalendar calendar)
                    {
                        return calendar.get(GregorianCalendar.DAY_OF_MONTH) == monthDay;
                    }

                    @Override
                    public void setNextPossibleDay(GregorianCalendar calendar)
                    {
                        int diff = monthDay - calendar.get(GregorianCalendar.DAY_OF_MONTH);
                        if (diff <= 0)
                        {
                            diff += calendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
                        }
                        setNextPossibleDay(calendar, diff);
                    }
                };
        }
    }

    private static class NextWeekDayInMonthFactory implements INextDayFactory
    {
        @Override
        public boolean accept(List<Object> descriptors)
        {
            return descriptors.size() == 2
                    && descriptors.get(0) instanceof Integer && descriptors.get(1) instanceof WeekDay;
        }

        @Override
        public INextDay create(List<Object> descriptors)
        {
            int weekNumber = ((Integer) descriptors.get(0)).intValue();
            WeekDay weekDay = (WeekDay) descriptors.get(1);
            return new AnyDay()
                {
                    @Override
                    public boolean isValidDay(GregorianCalendar calendar)
                    {
                        return calendar.get(GregorianCalendar.DAY_OF_WEEK) == weekDay.getNumber()
                                && getWeekNumber(calendar) == weekNumber;
                    }

                    @Override
                    public void setNextPossibleDay(GregorianCalendar calendar)
                    {
                        int diff = weekDay.getNumber() - calendar.get(GregorianCalendar.DAY_OF_WEEK);
                        if (diff <= 0)
                        {
                            diff += 7;
                        }
                        setNextPossibleDay(calendar, diff);
                        while (getWeekNumber(calendar) != weekNumber)
                        {
                            setNextPossibleDay(calendar, 7);
                        }
                    }

                    private int getWeekNumber(GregorianCalendar calendar)
                    {
                        return (calendar.get(GregorianCalendar.DAY_OF_MONTH) / 7) + 1;
                    }
                };
        }
    }

    private static class NextDayInYearFactory implements INextDayFactory
    {
        @Override
        public boolean accept(List<Object> descriptors)
        {
            return descriptors.size() == 2
                    && descriptors.get(0) instanceof Integer
                    && (descriptors.get(1) instanceof Integer || descriptors.get(1) instanceof Month);
        }

        @Override
        public INextDay create(List<Object> descriptors)
        {
            int monthDay = ((Integer) descriptors.get(0)).intValue();
            int monthIndex = descriptors.get(1) instanceof Integer ? ((Integer) descriptors.get(1)).intValue() - 1
                    : ((Month) descriptors.get(1)).getNumber();
            return new AnyDay()
                {
                    @Override
                    public boolean isValidDay(GregorianCalendar calendar)
                    {
                        return calendar.get(GregorianCalendar.DAY_OF_MONTH) == monthDay
                                && calendar.get(GregorianCalendar.MONTH) == monthIndex;
                    }

                    @Override
                    public void setNextPossibleDay(GregorianCalendar calendar)
                    {
                        int dayOfYear = calendar.get(GregorianCalendar.DAY_OF_YEAR);
                        calendar.set(GregorianCalendar.MONTH, monthIndex);
                        calendar.set(GregorianCalendar.DAY_OF_MONTH, monthDay);
                        if (calendar.get(GregorianCalendar.DAY_OF_YEAR) <= dayOfYear)
                        {
                            calendar.set(GregorianCalendar.YEAR, calendar.get(GregorianCalendar.YEAR) + 1);
                        }
                    }
                };
        }
    }

    private static class AnyDayFactory implements INextDayFactory
    {
        @Override
        public boolean accept(List<Object> descriptors)
        {
            return descriptors.isEmpty();
        }

        @Override
        public INextDay create(List<Object> descriptors)
        {
            return new AnyDay();
        }
    }

    private static class AnyDay implements INextDay
    {
        @Override
        public boolean isValidDay(GregorianCalendar calendar)
        {
            return true;
        }

        public void setNextPossibleDay(GregorianCalendar calendar)
        {
            setNextPossibleDay(calendar, 1);
        }

        protected void setNextPossibleDay(GregorianCalendar calendar, int diff)
        {
            calendar.set(GregorianCalendar.DAY_OF_YEAR, calendar.get(GregorianCalendar.DAY_OF_YEAR) + diff);
        }
    }
}
