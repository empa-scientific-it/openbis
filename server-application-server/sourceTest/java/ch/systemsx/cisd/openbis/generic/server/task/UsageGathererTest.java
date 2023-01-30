/*
 * Copyright 2018 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.test.ToStringMatcher;

/**
 * @author Franz-Josef Elmer
 */
public class UsageGathererTest
{
    private static final Period PERIOD = new Period(new Date(1234), new Date(2345));

    private static final String SESSION_TOKEN = "session-token";

    private static final String INACTIVE_USER = "user_inactive";

    private static final String USER_IN_A = "user_in_a";

    private static final String USER_IN_B = "user_in_b";

    private static final String USER_IN_A_AND_B = "user_in_a_and_b";

    private static final String USER1 = "user1";

    private static final String USER2 = "user2";

    private static final String[] ALL_USERS = { INACTIVE_USER, USER_IN_A, USER_IN_B, USER_IN_A_AND_B, USER1, USER2 };

    private Mockery context;

    private IApplicationServerInternalApi service;

    private UsageGatherer usageGatherer;

    private Set<String> spacesToBeIgnored;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IApplicationServerInternalApi.class);
        context.checking(new Expectations()
            {
                {
                    allowing(service).loginAsSystem();
                    will(returnValue(SESSION_TOKEN));

                    allowing(service).logout(SESSION_TOKEN);

                    allowing(service).searchPersons(with(SESSION_TOKEN), with(any(PersonSearchCriteria.class)),
                            with(any(PersonFetchOptions.class)));
                    List<Person> allUsers = new ArrayList<>();
                    for (String user : ALL_USERS)
                    {
                        allUsers.add(person(user));
                    }
                    will(returnValue(new SearchResult<>(allUsers, allUsers.size())));

                    AuthorizationGroupFetchOptions groupFetchOptions = new AuthorizationGroupFetchOptions();
                    groupFetchOptions.withUsers();
                    AuthorizationGroup groupA = group("A", groupFetchOptions, USER_IN_A, USER_IN_A_AND_B);
                    AuthorizationGroup groupB = group("B", groupFetchOptions, USER_IN_B, USER_IN_A_AND_B);
                    allowing(service).getAuthorizationGroups(with(SESSION_TOKEN),
                            with(Arrays.asList(groupA.getPermId(), groupB.getPermId())),
                            with(new ToStringMatcher<AuthorizationGroupFetchOptions>(groupFetchOptions)));
                    Map<IAuthorizationGroupId, AuthorizationGroup> map = new HashMap<>();
                    map.put(groupA.getPermId(), groupA);
                    map.put(groupB.getPermId(), groupB);
                    will(returnValue(map));
                }
            });
        spacesToBeIgnored = new HashSet<>();
        usageGatherer = new UsageGatherer(service, spacesToBeIgnored);
    }

    @Test
    public void testWithGroups()
    {
        // Given
        prepareSearchExperiments(experiment(USER_IN_A, space("A_STORAGE")), experiment(USER1, space("DEFAULT")),
                experiment(USER1, space("DEFAULT")));
        prepareSearchSamples(sample(USER_IN_A_AND_B, space("B_METHODS")), sample(USER_IN_A, space("B")), sample(USER2, space("A")));
        prepareSearchDataSets(dataSet(sample(USER1, space("BETA"))), dataSet(experiment(INACTIVE_USER, space("DEFAULT"))));

        // When
        UsageAndGroupsInfo info = usageGatherer.gatherUsageAndGroups(PERIOD, Arrays.asList("A", "B"));

        // Then
        String renderedMap = renderUsageInfo(info.getUsageByUsersAndSpaces());
        assertEquals(renderedMap, "user1 is active in space BETA, 1 new data sets\n"
                + "user1 is active in space DEFAULT, 2 new experiments\n"
                + "user2 is active in space A, 1 new samples\n"
                + "user_in_a is active in space A_STORAGE, 1 new experiments\n"
                + "user_in_a is active in space B, 1 new samples\n"
                + "user_in_a_and_b is active in space B_METHODS, 1 new samples\n"
                + "user_inactive is active in space DEFAULT, 1 new data sets\n");
        assertEquals(info.getUsersByGroups().toString(), "{A=[user_in_a, user_in_a_and_b], B=[user_in_a_and_b, user_in_b]}");
        context.assertIsSatisfied();
    }

    @Test
    public void testWithGroupsIgnoringSpaceDefault()
    {
        // Given
        spacesToBeIgnored.add("DEFAULT");
        prepareSearchExperiments(experiment(USER_IN_A, space("A_STORAGE")), experiment(USER1, space("DEFAULT")),
                experiment(USER1, space("DEFAULT")));
        prepareSearchSamples(sample(USER_IN_A_AND_B, space("B_METHODS")), sample(USER_IN_A, space("B")), sample(USER2, space("A")));
        prepareSearchDataSets(dataSet(sample(USER1, space("BETA"))), dataSet(experiment(INACTIVE_USER, space("DEFAULT"))));

        // When
        UsageAndGroupsInfo info = usageGatherer.gatherUsageAndGroups(PERIOD, Arrays.asList("A", "B"));

        // Then
        String renderedMap = renderUsageInfo(info.getUsageByUsersAndSpaces());
        assertEquals(renderedMap, "user1 is active in space BETA, 1 new data sets\n"
                + "user2 is active in space A, 1 new samples\n"
                + "user_in_a is active in space A_STORAGE, 1 new experiments\n"
                + "user_in_a is active in space B, 1 new samples\n"
                + "user_in_a_and_b is active in space B_METHODS, 1 new samples\n");
        assertEquals(info.getUsersByGroups().toString(), "{A=[user_in_a, user_in_a_and_b], B=[user_in_a_and_b, user_in_b]}");
        context.assertIsSatisfied();
    }

    @Test
    public void testWithoutGroups()
    {
        // Given
        prepareSearchExperiments(experiment(USER_IN_A, space("A_STORAGE")), experiment(USER1, space("DEFAULT")));
        prepareSearchSamples(sample(USER_IN_A_AND_B, space("B_METHODS")), sample(USER_IN_A, space("B")), sample(USER2, space("A")));
        prepareSearchDataSets(dataSet(sample(USER1, space("BETA"))), dataSet(experiment(INACTIVE_USER, space("DEFAULT"))));

        // When
        UsageAndGroupsInfo info = usageGatherer.gatherUsageAndGroups(PERIOD, null);

        // Then
        String renderedMap = renderUsageInfo(info.getUsageByUsersAndSpaces());
        assertEquals(renderedMap, "user1 is active in space BETA, 1 new data sets\n"
                + "user1 is active in space DEFAULT, 1 new experiments\n"
                + "user2 is active in space A, 1 new samples\n"
                + "user_in_a is active in space A_STORAGE, 1 new experiments\n"
                + "user_in_a is active in space B, 1 new samples\n"
                + "user_in_a_and_b is active in space B_METHODS, 1 new samples\n"
                + "user_inactive is active in space DEFAULT, 1 new data sets\n");
        assertEquals(info.getUsersByGroups().toString(), "{}");
        context.assertIsSatisfied();
    }

    @Test
    public void testWithoutGroupsIgnoringSpaceA()
    {
        // Given
        spacesToBeIgnored.add("A");
        prepareSearchExperiments(experiment(USER_IN_A, space("A_STORAGE")), experiment(USER1, space("DEFAULT")));
        prepareSearchSamples(sample(USER_IN_A_AND_B, space("B_METHODS")), sample(USER_IN_A, space("B")), sample(USER2, space("A")));
        prepareSearchDataSets(dataSet(sample(USER1, space("BETA"))), dataSet(experiment(INACTIVE_USER, space("DEFAULT"))));

        // When
        UsageAndGroupsInfo info = usageGatherer.gatherUsageAndGroups(PERIOD, null);

        // Then
        String renderedMap = renderUsageInfo(info.getUsageByUsersAndSpaces());
        assertEquals(renderedMap, "user1 is active in space BETA, 1 new data sets\n"
                + "user1 is active in space DEFAULT, 1 new experiments\n"
                + "user_in_a is active in space A_STORAGE, 1 new experiments\n"
                + "user_in_a is active in space B, 1 new samples\n"
                + "user_in_a_and_b is active in space B_METHODS, 1 new samples\n"
                + "user_inactive is active in space DEFAULT, 1 new data sets\n");
        assertEquals(info.getUsersByGroups().toString(), "{}");
        context.assertIsSatisfied();
    }

    private String renderUsageInfo(Map<String, Map<String, UsageInfo>> usageByUsersAndSpaces)
    {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, Map<String, UsageInfo>> entry : usageByUsersAndSpaces.entrySet())
        {
            String user = entry.getKey();
            for (Entry<String, UsageInfo> entry2 : entry.getValue().entrySet())
            {
                String group = entry2.getKey();
                UsageInfo info = entry2.getValue();
                builder.append(user).append(" is ").append(info.isIdle() ? "idle" : "active");
                builder.append(" in space ").append(group);
                addNumberOfType(builder, info.getNumberOfNewExperiments(), "new experiments");
                addNumberOfType(builder, info.getNumberOfNewSamples(), "new samples");
                addNumberOfType(builder, info.getNumberOfNewDataSets(), "new data sets");
                builder.append("\n");
            }
        }
        String renderedMap = builder.toString();
        return renderedMap;
    }

    private void addNumberOfType(StringBuilder builder, int number, String type)
    {
        if (number > 0)
        {
            builder.append(", ").append(number).append(" ").append(type);
        }
    }

    private void prepareSearchExperiments(Experiment... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    ExperimentSearchCriteria searchCriteria = new ExperimentSearchCriteria();
                    searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(PERIOD.getFrom());
                    searchCriteria.withRegistrationDate().thatIsEarlierThanOrEqualTo(PERIOD.getUntil());
                    ExperimentFetchOptions fetchOptions = experimentFetchOption();
                    one(service).searchExperiments(with(SESSION_TOKEN), with(new ToStringMatcher<ExperimentSearchCriteria>(searchCriteria)),
                            with(new ToStringMatcher<ExperimentFetchOptions>(fetchOptions)));
                    will(returnValue(new SearchResult<>(Arrays.asList(experiments), experiments.length)));
                }
            });
    }

    private void prepareSearchSamples(Sample... samples)
    {
        context.checking(new Expectations()
            {
                {
                    SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
                    searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(PERIOD.getFrom());
                    searchCriteria.withRegistrationDate().thatIsEarlierThanOrEqualTo(PERIOD.getUntil());
                    SampleFetchOptions fetchOptions = sampleFetchOption();
                    one(service).searchSamples(with(SESSION_TOKEN), with(new ToStringMatcher<SampleSearchCriteria>(searchCriteria)),
                            with(new ToStringMatcher<SampleFetchOptions>(fetchOptions)));
                    will(returnValue(new SearchResult<>(Arrays.asList(samples), samples.length)));
                }
            });
    }

    private void prepareSearchDataSets(DataSet... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
                    searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(PERIOD.getFrom());
                    searchCriteria.withRegistrationDate().thatIsEarlierThanOrEqualTo(PERIOD.getUntil());
                    DataSetFetchOptions fetchOptions = dataSetFetchOption();
                    one(service).searchDataSets(with(SESSION_TOKEN), with(new ToStringMatcher<DataSetSearchCriteria>(searchCriteria)),
                            with(new ToStringMatcher<DataSetFetchOptions>(fetchOptions)));
                    will(returnValue(new SearchResult<>(Arrays.asList(dataSets), dataSets.length)));
                }
            });
    }

    private DataSet dataSet(Experiment experiment)
    {
        DataSet dataSet = new DataSet();
        dataSet.setFetchOptions(dataSetFetchOption());
        dataSet.setExperiment(experiment);
        dataSet.setRegistrator(experiment.getRegistrator());
        return dataSet;
    }

    private DataSet dataSet(Sample sample)
    {
        DataSet dataSet = new DataSet();
        dataSet.setFetchOptions(dataSetFetchOption());
        dataSet.setSample(sample);
        dataSet.setRegistrator(sample.getRegistrator());
        return dataSet;
    }

    private Experiment experiment(String registrator, Space space)
    {
        Experiment experiment = new Experiment();
        Person person = new Person();
        person.setUserId(registrator);
        experiment.setRegistrator(person);
        experiment.setFetchOptions(experimentFetchOption());
        Project project = new Project();
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();
        project.setFetchOptions(fetchOptions);
        project.setSpace(space);
        experiment.setProject(project);
        return experiment;
    }

    private Sample sample(String registrator, Space space)
    {
        Sample sample = new Sample();
        Person person = new Person();
        person.setUserId(registrator);
        sample.setRegistrator(person);
        sample.setFetchOptions(sampleFetchOption());
        sample.setSpace(space);
        return sample;
    }

    private Space space(String code)
    {
        Space space = new Space();
        space.setCode(code);
        return space;
    }

    private AuthorizationGroup group(String groupCode, AuthorizationGroupFetchOptions groupFetchOptions, String... users)
    {
        AuthorizationGroup group = new AuthorizationGroup();
        group.setCode(groupCode);
        group.setPermId(new AuthorizationGroupPermId(groupCode));
        group.setFetchOptions(groupFetchOptions);
        List<Person> persons = new ArrayList<>();
        for (String user : users)
        {
            persons.add(person(user));
        }
        group.setUsers(persons);
        return group;
    }

    private Person person(String user)
    {
        Person person = new Person();
        person.setActive(user.equals(INACTIVE_USER) == false);
        person.setUserId(user);
        person.setPermId(new PersonPermId(user));
        return person;
    }

    private ExperimentFetchOptions experimentFetchOption()
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject().withSpace();
        fetchOptions.withRegistrator();
        return fetchOptions;
    }

    private SampleFetchOptions sampleFetchOption()
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        return fetchOptions;
    }

    private DataSetFetchOptions dataSetFetchOption()
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withRegistrator();
        SampleFetchOptions sampleFetchOptions = fetchOptions.withSample();
        sampleFetchOptions.withSpace();
        ExperimentFetchOptions experimentFetchOptions = fetchOptions.withExperiment();
        experimentFetchOptions.withProject().withSpace();
        return fetchOptions;
    }

}
