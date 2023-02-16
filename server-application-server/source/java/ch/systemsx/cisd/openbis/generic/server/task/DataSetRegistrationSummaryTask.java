/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Maintenance task send e-mails with reports on recently registered data sets.
 * <p>
 * The maintenance task may be run in one of two modes, the normal mode is to report on data sets registered in the period specified by the
 * days-of-month or days-of-week. The period goes from the current day to the last day the maintenance task was run.
 * <p>
 * Sometimes, however, for the first run, the user may want to explicitly specify the date range for the maintenance task. This can be done, but the
 * maintenance task should then only be run once, because it will always report on the specified date range.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationSummaryTask implements IMaintenanceTask
{
    public static final String DAYS_OF_WEEK_KEY = "days-of-week";

    public static final String DAYS_OF_MONTH_KEY = "days-of-month";

    public static final String DATE_RANGE_START_KEY = "date-range-start";

    public static final String DATE_RANGE_STOP_KEY = "date-range-stop";

    public static final String DATA_SET_TYPES = "data-set-types";

    public static final String CONFIGURED_CONTENT = "configured-content";

    public static final String SHOWN_DATA_SET_PROPERTIES_KEY = "shown-data-set-properties";

    private static final String SEPARATOR = ",";

    private static final int NUMBER_OF_DATA_SET_CODES_PER_LINE = 4;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final Template SUBJECT_TEMPLATE = new Template("New data sets registered "
            + "between ${from-date} and ${until-date}");

    private static final Template REPORT_TEMPLATE = new Template("Dear user\n\n"
            + "This report summarizes data sets registered in openBIS "
            + "between ${from-date} and ${until-date}.\n\n${configured-content}\n\n"
            + "${data-sets}\n\nRegards,\nopenBIS");

    private static final Template NO_NEW_DATA_SETS_TEMPLATE = new Template(
            "${data-set-type}: Total number: ${total-number}. No new data sets.\n");

    private static final Template NEW_DATA_SETS_TEMPLATE = new Template(
            "${data-set-type}: Total number: ${total-number}. Number of new data sets: "
                    + "${number-of-new-data-sets}\n${new-data-sets}");

    private static final Template NEW_DATA_SET_TEMPLATE = new Template("\t${data-set-code}\n");

    private static final Template NEW_DATA_SET_WITH_PROPERTIES_TEMPLATE = new Template(
            "\t${data-set-code}: ${properties}\n");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationSummaryTask.class);

    private ICommonServerForInternalUse server;

    private ITimeProvider timeProvider;

    private IMailClient mailClient;

    private Set<Integer> daysOfWeek;

    private Set<Integer> daysOfMonth;

    private Set<String> dataSetTypeCodes;

    private List<String> shownProperties;

    private List<EMailAddress> emailAddresses;

    private String configuredContent;

    public DataSetRegistrationSummaryTask()
    {
        this(CommonServiceProvider.getCommonServer(), SystemTimeProvider.SYSTEM_TIME_PROVIDER,
                CommonServiceProvider.createEMailClient());
    }

    DataSetRegistrationSummaryTask(ICommonServerForInternalUse server, ITimeProvider timeProvider,
            IMailClient mailClient)
    {
        this.server = server;
        this.timeProvider = timeProvider;
        this.mailClient = mailClient;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        daysOfWeek = extractDays(properties, DAYS_OF_WEEK_KEY, "");
        daysOfMonth = extractDays(properties, DAYS_OF_MONTH_KEY, "1");
        shownProperties = getAsList(properties, SHOWN_DATA_SET_PROPERTIES_KEY);
        emailAddresses = PluginUtils.getEMailAddresses(properties, SEPARATOR);
        dataSetTypeCodes = extractDataSetTypeCodes(properties, DATA_SET_TYPES);
        configuredContent = PropertyUtils.getProperty(properties, CONFIGURED_CONTENT, "The data sets are grouped by type.");
        operationLog.info("Task " + pluginName + " initialized.");
    }

    private Set<String> extractDataSetTypeCodes(Properties properties, String key)
    {
        Set<String> result = new HashSet<String>();
        String property = properties.getProperty(key, "").trim();
        if (property.length() > 0)
        {
            for (String dataSetTypeCode : property.split(SEPARATOR))
            {
                result.add(dataSetTypeCode.trim());
            }
        }
        return result;
    }

    private Set<Integer> extractDays(Properties properties, String key, String defaultValue)
    {
        Set<Integer> result = new HashSet<Integer>();
        String property = properties.getProperty(key, defaultValue).trim();
        if (property.length() > 0)
        {

            for (String day : property.split(SEPARATOR))
            {
                try
                {
                    result.add(Integer.valueOf(day.trim()));
                } catch (NumberFormatException ex)
                {
                    throw new IllegalArgumentException("Property '" + key
                            + "' is not a list of numbers separated by commas: " + property);
                }
            }
        }
        return result;
    }

    private List<String> getAsList(Properties properties, String key)
    {
        List<String> list = PropertyUtils.tryGetList(properties, key);
        if (list == null || (list.size() == 1 && list.get(0).length() == 0))
        {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public void execute()
    {
        if (isDay() == false)
        {
            return;
        }
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull == null)
        {
            return;
        }
        long startTime = getStart();
        long endTime = getEnd();
        String sessionToken = contextOrNull.getSessionToken();
        String dataSetsAsString = loadAndRenderDataSets(sessionToken, startTime, endTime);
        String fromDate = DATE_FORMAT.format(new Date(startTime));
        String untilDate = DATE_FORMAT.format(new Date(endTime));
        Template subjectTemplate = SUBJECT_TEMPLATE.createFreshCopy();
        subjectTemplate.bind("from-date", fromDate);
        subjectTemplate.bind("until-date", untilDate);
        String subject = subjectTemplate.createText();
        Template template = REPORT_TEMPLATE.createFreshCopy();
        template.bind("from-date", fromDate);
        template.bind("until-date", untilDate);
        template.bind("data-sets", dataSetsAsString);
        template.bind("configured-content", configuredContent);
        String report = template.createText();
        for (EMailAddress address : emailAddresses)
        {
            mailClient.sendEmailMessage(subject, report, null, null, address);
        }
        operationLog.info("Data set registration report for period from " + fromDate + " until "
                + untilDate + " created and sent.");
    }

    private String loadAndRenderDataSets(final String sessionToken, long startTime, long endTime)
    {
        List<DataSetType> dataSetTypes = getAllDataSetTypes(sessionToken);
        StringBuilder builder = new StringBuilder();
        for (DataSetType dataSetType : dataSetTypes)
        {
            // Only create the report for the data set type codes that the user has specified
            if (excludeDataSetType(dataSetType))
            {
                continue;
            }
            DetailedSearchCriteria criteria = new DetailedSearchCriteria();
            criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
            criteria.setUseWildcardSearchMode(true);
            DetailedSearchCriterion criterion = new DetailedSearchCriterion();
            criterion.setField(DetailedSearchField
                    .createAttributeField(DataSetAttributeSearchFieldKind.DATA_SET_TYPE));
            criterion.setValue(dataSetType.getCode());
            criteria.setCriteria(Arrays.asList(criterion));
            List<AbstractExternalData> dataSets = server.searchForDataSets(sessionToken, criteria);
            List<AbstractExternalData> newDataSets = new ArrayList<AbstractExternalData>();
            for (AbstractExternalData dataSet : dataSets)
            {
                long registrationTime = dataSet.getRegistrationDate().getTime();
                if (startTime <= registrationTime && registrationTime < endTime)
                {
                    newDataSets.add(dataSet);
                }
            }
            Collections.sort(newDataSets, new Comparator<AbstractExternalData>()
                {
                    @Override
                    public int compare(AbstractExternalData d1, AbstractExternalData d2)
                    {
                        return d1.getCode().compareTo(d2.getCode());
                    }
                });
            builder.append(createReport(dataSetType, newDataSets, dataSets.size()));
        }
        String dataSetsAsString = builder.toString();
        return dataSetsAsString;
    }

    /**
     * @return True if the data set type should <b>not</b> be included in the report; return false if it should be included in the report.
     */
    private boolean excludeDataSetType(DataSetType dataSetType)
    {
        final boolean includeDataSetType;
        if (dataSetTypeCodes.isEmpty())
        {
            // If the user did not explicitly restrict the data set types, include all of them
            includeDataSetType = true;
        } else
        {
            // Include those that are explicitly requested
            includeDataSetType = dataSetTypeCodes.contains(dataSetType.getCode());
        }

        // Return the negation of includDataSetType
        return !includeDataSetType;
    }

    private String createReport(DataSetType dataSetType, List<AbstractExternalData> newDataSets,
            int totalNumberOfDataSets)
    {
        Template template =
                (newDataSets.isEmpty() ? NO_NEW_DATA_SETS_TEMPLATE : NEW_DATA_SETS_TEMPLATE)
                        .createFreshCopy();
        template.bind("data-set-type", dataSetType.getCode());
        template.bind("total-number", Integer.toString(totalNumberOfDataSets));
        template.attemptToBind("number-of-new-data-sets", Integer.toString(newDataSets.size()));
        template.attemptToBind("new-data-sets", renderNewDataSets(newDataSets));
        return template.createText();
    }

    private String renderNewDataSets(List<AbstractExternalData> newDataSets)
    {
        StringBuilder builder = new StringBuilder();
        if (shownProperties.isEmpty())
        {
            for (int i = 0, n = newDataSets.size(); i < n; i++)
            {
                if (i % NUMBER_OF_DATA_SET_CODES_PER_LINE == 0)
                {
                    builder.append("\t");
                }
                AbstractExternalData dataSet = newDataSets.get(i);
                builder.append(dataSet.getCode());
                if (i < n - 1)
                {
                    builder.append(", ");
                }
                if ((i + 1) % NUMBER_OF_DATA_SET_CODES_PER_LINE == 0 || i == n - 1)
                {
                    builder.append("\n");
                }
            }
        } else
        {
            Template noPropertiesTemplate = NEW_DATA_SET_TEMPLATE.createFreshCopy();
            Template withPropertiesTemplate =
                    NEW_DATA_SET_WITH_PROPERTIES_TEMPLATE.createFreshCopy();
            for (AbstractExternalData dataSet : newDataSets)
            {
                String code = dataSet.getCode();

                String propertiesToBeShown = getPropertiesToBeShown(dataSet);
                Template t =
                        propertiesToBeShown.length() == 0 ? noPropertiesTemplate
                                : withPropertiesTemplate;
                t.attemptToBind("data-set-code", code);
                t.attemptToBind("properties", propertiesToBeShown);
                builder.append(t.createText());
            }
        }
        return builder.toString();
    }

    private String getPropertiesToBeShown(AbstractExternalData dataSet)
    {
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = getPropertiesAsAMap(dataSet);
        for (String key : shownProperties)
        {
            String value = map.get(key);
            if (value != null)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(key).append(" = ").append(value);
            }
        }
        return builder.toString();
    }

    private Map<String, String> getPropertiesAsAMap(AbstractExternalData dataSet)
    {
        List<IEntityProperty> dataSetProperties = dataSet.getProperties();
        Map<String, String> map = new HashMap<String, String>(dataSetProperties.size());
        for (IEntityProperty property : dataSetProperties)
        {
            map.put(property.getPropertyType().getCode(), property.tryGetAsString());
        }
        return map;
    }

    private List<DataSetType> getAllDataSetTypes(final String sessionToken)
    {
        List<DataSetType> dataSetTypes = server.listDataSetTypes(sessionToken);
        Collections.sort(dataSetTypes, new Comparator<DataSetType>()
            {
                @Override
                public int compare(DataSetType t1, DataSetType t2)
                {
                    return t1.getCode().compareTo(t2.getCode());
                }
            });
        return dataSetTypes;
    }

    private boolean isDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeProvider.getTimeInMilliseconds());
        return isDay(calendar);
    }

    /**
     * The start time is the beginning of the first day of the report's period.
     */
    private long getStart()
    {
        long time = timeProvider.getTimeInMilliseconds();
        Calendar calendar = Calendar.getInstance();
        do
        {
            time -= DateUtils.MILLIS_PER_DAY;
            calendar.setTimeInMillis(time);
        } while (isDay(calendar) == false);
        return getFirstMilliSecondOfTheDay(calendar);
    }

    /**
     * The end time is right now.
     */
    private long getEnd()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeProvider.getTimeInMilliseconds());
        return calendar.getTimeInMillis();
    }

    private boolean isDay(Calendar calendar)
    {
        return daysOfWeek.contains(calendar.get(Calendar.DAY_OF_WEEK))
                || daysOfMonth.contains(calendar.get(Calendar.DAY_OF_MONTH));
    }

    private long getFirstMilliSecondOfTheDay(Calendar calendar)
    {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
