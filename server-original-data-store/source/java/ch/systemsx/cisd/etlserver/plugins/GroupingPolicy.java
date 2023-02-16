/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.plugins;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.plugins.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.etlserver.plugins.grouping.Grouping;
import ch.systemsx.cisd.etlserver.plugins.grouping.IGroupKeyProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;

/**
 * Configurable auto archiving policy which allows to find a group of data sets with total size from a specified interval. Grouping can be defined by
 * space, project, experiment, sample, data set type or a combination of those. The combination defines a so called 'grouping key'. Groups can be
 * merged if they are too small. Several grouping keys can be specified.
 * <p>
 * Searching for an appropriate group of data sets for auto archiving is logged. If no group could be found the admin (as specified in log.xml) is
 * notified be email with the searching log as content.
 *
 * @author Franz-Josef Elmer
 */
public class GroupingPolicy extends BaseGroupingPolicy
{
    static final String GROUPING_KEYS_KEY = "grouping-keys";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GroupingPolicy.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, GroupingPolicy.class);

    private final List<CombinedGroupKeys> groupKeyProviders = new ArrayList<CombinedGroupKeys>();

    public GroupingPolicy(Properties properties)
    {
        super(properties);
        List<String> groupingKeys = PropertyUtils.getList(properties, GROUPING_KEYS_KEY);
        for (String groupingKey : groupingKeys)
        {
            String[] splitted = groupingKey.split(":", 2);
            boolean merge = false;
            if (splitted.length > 1)
            {
                if ("merge".equals(splitted[1]) == false)
                {
                    throw new ConfigurationFailureException("Invalid grouping key in property '" + GROUPING_KEYS_KEY
                            + "' because 'merge' is expected after ':': " + groupingKey);
                }
                merge = true;
            }
            String[] keyItems = splitted[0].split("#");
            List<IGroupKeyProvider> groupings = new ArrayList<IGroupKeyProvider>();
            for (String keyItem : keyItems)
            {
                try
                {
                    groupings.add(Grouping.valueOf(keyItem));
                } catch (IllegalArgumentException ex)
                {
                    throw new ConfigurationFailureException("Invalid basic grouping key in property '"
                            + GROUPING_KEYS_KEY + "': " + keyItem + " (valid values are "
                            + Arrays.asList(Grouping.values()) + ")");
                }
            }
            groupKeyProviders.add(new CombinedGroupKeys(groupings, merge));
        }
    }

    @Override
    protected List<AbstractExternalData> filterDataSetsWithSizes(List<AbstractExternalData> dataSets)
    {
        List<String> log = new ArrayList<String>();
        log(log, "Search for a group of data sets with total size between "
                + FileUtils.byteCountToDisplaySize(minArchiveSize) + " and "
                + FileUtils.byteCountToDisplaySize(maxArchiveSize) + ". Data sets: "
                + CollectionUtils.abbreviate(Code.extractCodes(dataSets), 50));
        for (CombinedGroupKeys combinedGroupKeys : groupKeyProviders)
        {
            List<DatasetListWithTotal> groups = splitIntoGroups(dataSets, combinedGroupKeys);
            log(log, combinedGroupKeys + " has grouped " + dataSets.size() + " data sets into "
                    + groups.size() + " groups.");
            if (groups.isEmpty() == false)
            {
                List<AbstractExternalData> result = tryFindGroupOrMerge(groups, combinedGroupKeys.isMerge(), log);
                if (result != null)
                {
                    log(log, "filtered data sets: " + CollectionUtils.abbreviate(Code.extractCodes(result), 20));
                    return result;
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("From " + dataSets.size() + " data sets no group could be found to be fit between ");
        builder.append(FileUtils.byteCountToDisplaySize(minArchiveSize)).append(" and ");
        builder.append(FileUtils.byteCountToDisplaySize(maxArchiveSize));
        builder.append("\n\nLog:");
        for (String logMessage : log)
        {
            builder.append('\n').append(logMessage);
        }
        notificationLog.warn(builder.toString());
        return new ArrayList<AbstractExternalData>();
    }

    private List<AbstractExternalData> tryFindGroupOrMerge(List<DatasetListWithTotal> groups,
            boolean merge, List<String> log)
    {
        List<DatasetListWithTotal> tooSmallGroups = new ArrayList<DatasetListWithTotal>();
        List<DatasetListWithTotal> fittingGroups = new ArrayList<DatasetListWithTotal>();
        for (DatasetListWithTotal group : groups)
        {
            long size = group.getCumulatedSize();
            if (size < minArchiveSize)
            {
                tooSmallGroups.add(group);
            } else if (size <= maxArchiveSize)
            {
                fittingGroups.add(group);
            }
        }
        log(log, fittingGroups.size() + " groups match in size, " + tooSmallGroups.size() + " groups are too small and "
                + (groups.size() - fittingGroups.size() - tooSmallGroups.size()) + " groups are too large.");
        if (fittingGroups.isEmpty() == false)
        {
            return getOldestGroup(fittingGroups, log);
        }
        if (tooSmallGroups.size() < 2 || merge == false)
        {
            return null;
        }
        return tryMerge(tooSmallGroups, log);
    }

    private List<AbstractExternalData> getOldestGroup(List<DatasetListWithTotal> groups, List<String> log)
    {
        if (groups.size() == 1)
        {
            return groups.get(0).getList();
        }
        GroupWithAge oldestGroup = sortGroupsByAge(groups).get(0);
        String timestamp = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(new Date(oldestGroup.age));
        log(log, "All data sets of the selected group have been accessed at " + timestamp + " or before.");
        return oldestGroup.group.getList();
    }

    private List<GroupWithAge> sortGroupsByAge(List<DatasetListWithTotal> groups)
    {
        List<GroupWithAge> groupsWithAge = new ArrayList<GroupWithAge>();
        for (DatasetListWithTotal group : groups)
        {
            groupsWithAge.add(new GroupWithAge(group));
        }
        Collections.sort(groupsWithAge);
        return groupsWithAge;
    }

    private List<AbstractExternalData> tryMerge(List<DatasetListWithTotal> groups, List<String> log)
    {
        List<GroupWithAge> groupsWithAge = sortGroupsByAge(groups);
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        long total = 0;
        for (int i = 0; i < groupsWithAge.size(); i++)
        {
            DatasetListWithTotal group = groupsWithAge.get(i).group;
            result.addAll(group.getList());
            total += group.getCumulatedSize();
            if (total >= minArchiveSize)
            {
                if (total <= maxArchiveSize)
                {
                    log(log, (i + 1) + " groups have been merged.");
                    return result;
                }
                log(log, (i + 1) + " groups have been merged, but the total size of " + FileUtils.byteCountToDisplaySize(total)
                        + " is above the required maximum of " + FileUtils.byteCountToDisplaySize(maxArchiveSize));
                return null;
            }
        }
        log(log, "Merging all " + groups.size() + " groups gives a total size of " + FileUtils.byteCountToDisplaySize(total)
                + " which is still below required minimum of " + FileUtils.byteCountToDisplaySize(minArchiveSize));
        return null;
    }

    private List<DatasetListWithTotal> splitIntoGroups(List<AbstractExternalData> dataSets, IGroupKeyProvider groupKeyProvider)
    {
        List<DatasetListWithTotal> groups = new ArrayList<DatasetListWithTotal>(
                splitDataSetsInGroupsAccordingToCriteria(dataSets, groupKeyProvider));
        Collections.sort(groups);
        return groups;
    }

    private void log(List<String> log, Object logMessage)
    {
        log.add(logMessage.toString());
        operationLog.info(logMessage.toString());
    }

    private static final class CombinedGroupKeys implements IGroupKeyProvider
    {
        private final List<IGroupKeyProvider> groupKeyProviders;

        private final boolean merge;

        CombinedGroupKeys(List<IGroupKeyProvider> groupKeyProviders, boolean merge)
        {
            this.groupKeyProviders = groupKeyProviders;
            this.merge = merge;
        }

        public boolean isMerge()
        {
            return merge;
        }

        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            StringBuilder builder = new StringBuilder();
            for (IGroupKeyProvider groupKeyProvider : groupKeyProviders)
            {
                if (builder.length() > 0)
                {
                    builder.append('#');
                }
                builder.append(groupKeyProvider.getGroupKey(dataset));
            }
            return builder.toString();
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            for (IGroupKeyProvider keyProvider : groupKeyProviders)
            {
                if (builder.length() > 0)
                {
                    builder.append('#');
                }
                builder.append(keyProvider);
            }
            if (merge)
            {
                builder.append(":merge");
            }
            return "Grouping key: '" + builder.toString() + "'";
        }

    }

    private static class GroupWithAge implements Comparable<GroupWithAge>
    {
        private long age;

        private DatasetListWithTotal group;

        GroupWithAge(DatasetListWithTotal group)
        {
            this.group = group;
            List<AbstractExternalData> dataSets = group.getList();
            for (AbstractExternalData dataSet : dataSets)
            {
                age = Math.max(age, dataSet.getAccessTimestamp().getTime());
            }
        }

        @Override
        public int compareTo(GroupWithAge that)
        {
            return Long.signum(this.age - that.age);
        }
    }
}
