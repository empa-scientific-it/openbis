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
package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

/**
 * @author Franz-Josef Elmer
 */
public class UsageGatherer
{
    static final String UNNAMED_GROUP = "";

    private IApplicationServerInternalApi service;

    private Set<String> spacesToBeIgnored;

    public UsageGatherer(IApplicationServerInternalApi service, Set<String> spacesToBeIgnored)
    {
        this.service = service;
        this.spacesToBeIgnored = spacesToBeIgnored;
    }

    public UsageAndGroupsInfo gatherUsageAndGroups(Period period, List<String> groupsOrNull)
    {
        String sessionToken = service.loginAsSystem();
        try
        {
            Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces = gatherUsageByUsersAndSpaces(sessionToken, period);
            Map<String, Set<String>> usersByGroup = gatherUsersByGroups(sessionToken, groupsOrNull);
            return new UsageAndGroupsInfo(usageByUsersAndSpaces, usersByGroup);
        } finally
        {
            service.logout(sessionToken);
        }
    }

    private Map<String, Map<String, UsageInfo>> gatherUsageByUsersAndSpaces(String sessionToken, Period period)
    {
        Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces = new TreeMap<>();
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        for (Person user : service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects())
        {
            Map<String, UsageInfo> usageBySpaces = new TreeMap<>();
            usageByUsersAndSpaces.put(user.getUserId(), usageBySpaces);
        }
        gatherNewExperimentUsage(sessionToken, usageByUsersAndSpaces, period);
        gatherNewSampleUsage(sessionToken, usageByUsersAndSpaces, period);
        gatherNewDataSetUsage(sessionToken, usageByUsersAndSpaces, period);
        return usageByUsersAndSpaces;
    }

    private Map<String, Set<String>> gatherUsersByGroups(String sessionToken, List<String> groupsOrNull)
    {
        Map<String, Set<String>> usersByGroup = new TreeMap<>();
        if (groupsOrNull != null)
        {
            List<AuthorizationGroupPermId> groupIds = groupsOrNull.stream().map(AuthorizationGroupPermId::new).collect(Collectors.toList());
            AuthorizationGroupFetchOptions groupFetchOptions = new AuthorizationGroupFetchOptions();
            groupFetchOptions.withUsers();
            for (AuthorizationGroup group : service.getAuthorizationGroups(sessionToken, groupIds, groupFetchOptions).values())
            {
                TreeSet<String> userIds = new TreeSet<String>();
                for (Person user : group.getUsers())
                {
                    userIds.add(user.getUserId());
                }
                usersByGroup.put(group.getCode(), userIds);
            }
        }
        return usersByGroup;
    }

    private void gatherNewExperimentUsage(String sessionToken, Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces, Period period)
    {
        ExperimentSearchCriteria searchCriteria = new ExperimentSearchCriteria();
        setPeriod(searchCriteria, period);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject().withSpace();
        fetchOptions.withRegistrator();
        List<Experiment> experiments = service.searchExperiments(sessionToken, searchCriteria, fetchOptions).getObjects();
        Function<Experiment, String> spaceExtractor = exp -> exp.getProject().getSpace().getCode();
        gatherUsage(usageByUsersAndSpaces, experiments, spaceExtractor, UsageInfo::addNewExperiment);
    }

    private void gatherNewSampleUsage(String sessionToken, Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces, Period period)
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        setPeriod(searchCriteria, period);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        List<Sample> samples = service.searchSamples(sessionToken, searchCriteria, fetchOptions).getObjects();
        Function<Sample, String> spaceExtractor = sample -> sample.getSpace() != null ? sample.getSpace().getCode() : null;
        gatherUsage(usageByUsersAndSpaces, samples, spaceExtractor, UsageInfo::addNewSample);
    }

    private void gatherNewDataSetUsage(String sessionToken, Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces, Period period)
    {
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        setPeriod(searchCriteria, period);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withRegistrator();
        SampleFetchOptions sampleFetchOptions = fetchOptions.withSample();
        sampleFetchOptions.withSpace();
        ExperimentFetchOptions experimentFetchOptions = fetchOptions.withExperiment();
        experimentFetchOptions.withProject().withSpace();
        List<DataSet> dataSets = service.searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        Function<DataSet, String> spaceExtractor = new Function<DataSet, String>()
            {
                @Override
                public String apply(DataSet dataSet)
                {
                    Experiment experiment = dataSet.getExperiment();
                    if (experiment != null)
                    {
                        return experiment.getProject().getSpace().getCode();
                    }

                    Sample sample = dataSet.getSample();
                    if (sample != null)
                    {
                        return sample.getSpace() != null ? sample.getSpace().getCode() : null;
                    }

                    return null;
                }
            };
        gatherUsage(usageByUsersAndSpaces, dataSets, spaceExtractor, UsageInfo::addNewDataSet);
    }

    private <T extends IRegistratorHolder> void gatherUsage(Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces, List<T> entities,
            Function<T, String> spaceExtractor, Consumer<UsageInfo> consumer)
    {
        for (T entity : entities)
        {
            String userId = entity.getRegistrator().getUserId();
            Map<String, UsageInfo> usageBySpaces = usageByUsersAndSpaces.get(userId);
            if (usageBySpaces != null)
            {
                String space = spaceExtractor.apply(entity);
                if (space != null && spacesToBeIgnored.contains(space) == false)
                {
                    UsageInfo usageInfo = usageBySpaces.get(space);
                    if (usageInfo == null)
                    {
                        usageInfo = new UsageInfo();
                        usageBySpaces.put(space, usageInfo);
                    }
                    consumer.accept(usageInfo);
                }
            }
        }
    }

    private void setPeriod(AbstractEntitySearchCriteria<?> searchCriteria, Period period)
    {
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(period.getFrom());
        searchCriteria.withRegistrationDate().thatIsEarlierThanOrEqualTo(period.getUntil());
    }
}
