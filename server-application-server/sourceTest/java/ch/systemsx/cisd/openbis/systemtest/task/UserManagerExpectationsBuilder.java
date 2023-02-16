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
package ch.systemsx.cisd.openbis.systemtest.task;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.server.task.UserManager;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

class UserManagerExpectationsBuilder
{
    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private final IApplicationServerInternalApi v3api;

    private final UserManagerTestService testService;

    private final IOpenBisSessionManager sessionManager;

    private Map<Role, List<String>> commonSpaces = new TreeMap<>();

    private List<Principal> unknownUsers = new ArrayList<>();

    private Map<String, String> homeSpacesByUserId = new TreeMap<>();

    private List<String> globalSpaces = new ArrayList<>();

    private List<String> groups = new ArrayList<>();

    private Set<String> users = new TreeSet<>();

    private Set<String> usersWithoutAuthentication = new TreeSet<>();

    private Map<String, List<String>> sampleIdentifiersByType = new TreeMap<>();

    private Map<String, List<String>> experimentIdentifiersByType = new TreeMap<>();

    private Map<String, Map<AuthorizationLevel, Set<String>>> usersByLevelBySpace = new TreeMap<>();

    private boolean deactivation = true;

    private boolean useEmailAsUserId;

    UserManagerExpectationsBuilder(IApplicationServerInternalApi v3api, UserManagerTestService testService,
            IOpenBisSessionManager sessionManager, Map<Role, List<String>> commonSpaces)
    {
        this.v3api = v3api;
        this.testService = testService;
        this.sessionManager = sessionManager;
        this.commonSpaces = commonSpaces;
    }

    UserManagerExpectationsBuilder(IApplicationServerInternalApi v3api, UserManagerTestService testService,
            IOpenBisSessionManager sessionManager)
    {
        this.v3api = v3api;
        this.testService = testService;
        this.sessionManager = sessionManager;
    }

    public UserManagerExpectationsBuilder useEmailAsUserId()
    {
        useEmailAsUserId = true;
        return this;
    }

    public UserManagerExpectationsBuilder noDeactivation()
    {
        deactivation = false;
        return this;
    }

    public UserManagerExpectationsBuilder globalSpaces(List<String> globalSpaces)
    {
        this.globalSpaces = globalSpaces;
        return this;
    }

    UserManagerExpectationsBuilder unknownUser(Principal user)
    {
        unknownUsers.add(user);
        return usersWithoutAuthentication(user);
    }

    UserManagerExpectationsBuilder usersWithoutAuthentication(Principal... users)
    {
        usersWithoutAuthentication.addAll(getUserIds(users));
        return this;
    }

    UserManagerExpectationsBuilder groups(String... groups)
    {
        this.groups = Arrays.asList(groups);
        return this;
    }

    UserManagerExpectationsBuilder commonSpaces(Map<Role, List<String>> commonSpaces)
    {
        this.commonSpaces = commonSpaces;
        return this;
    }

    UserManagerExpectationsBuilder samples(String type, String... sampleIdentifiers)
    {
        return addIdentifiers(sampleIdentifiersByType, type, sampleIdentifiers);
    }

    UserManagerExpectationsBuilder experiments(String type, String... experimentIdentifiers)
    {
        return addIdentifiers(experimentIdentifiersByType, type, experimentIdentifiers);
    }

    private UserManagerExpectationsBuilder addIdentifiers(Map<String, List<String>> identifiersByType, String type, String... identifiers)
    {
        List<String> list = identifiersByType.get(type);
        if (list == null)
        {
            list = new ArrayList<String>();
            identifiersByType.put(type, list);
        }
        list.addAll(Arrays.asList(identifiers));
        return this;
    }

    UserManagerExpectationsBuilder users(Principal... users)
    {
        this.users.addAll(getUserIds(users));
        return this;
    }

    UserManagerAuthorizationExpectationsBuilder space(String spaceCode)
    {
        TreeMap<AuthorizationLevel, Set<String>> usersByLevel = new TreeMap<>();
        Map<AuthorizationLevel, Set<String>> previous = usersByLevelBySpace.put(spaceCode, usersByLevel);
        if (previous != null)
        {
            fail("Authorization expectations for space " + spaceCode + " already specified.");
        }
        return new UserManagerAuthorizationExpectationsBuilder(usersByLevel, this, useEmailAsUserId);
    }

    UserManagerExpectationsBuilder homeSpace(Principal user, String homeSpace)
    {
        homeSpacesByUserId.put(getUserIds(user).get(0), homeSpace);
        return this;
    }

    void assertExpectations()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertSamples(sessionToken);
        assertExperiments(sessionToken);
        assertSpaces(sessionToken);
        assertUnknownUsers(sessionToken);
        assertUsers(sessionToken);
        assertNoAuthentication();
        checkMissingAuthorizationExpectations();
        assertAuthorizationOfGlobalSpaces();
        assertAuthorization();
        v3api.logout(sessionToken);
    }

    private void assertNoAuthentication()
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (String userId : usersWithoutAuthentication)
        {
            try
            {
                String sessionToken = v3api.login(userId, PASSWORD);
                if (sessionToken != null)
                {
                    builder.append(userId);
                    v3api.logout(sessionToken);
                }
            } catch (UserFailureException e)
            {
                // expected exception silently ignored
            }
        }
        if (builder.toString().length() > 0)
        {
            fail("The following user can still be authenticated: " + builder);
        }
    }

    private void assertSamples(String sessionToken)
    {
        if (sampleIdentifiersByType.isEmpty() == false)
        {
            for (Entry<String, List<String>> entry : sampleIdentifiersByType.entrySet())
            {
                String typeCode = entry.getKey();
                List<String> sampleIdentifiers = entry.getValue();
                Collections.sort(sampleIdentifiers);
                List<SampleIdentifier> sampleIds = sampleIdentifiers.stream().map(SampleIdentifier::new).collect(Collectors.toList());
                SampleFetchOptions fetchOptions = new SampleFetchOptions();
                fetchOptions.withType();
                Collection<Sample> samples = v3api.getSamples(sessionToken, sampleIds, fetchOptions).values();
                List<String> actualIdentifiers = samples.stream().map(s -> s.getIdentifier().getIdentifier()).collect(Collectors.toList());
                Collections.sort(actualIdentifiers);
                assertEquals(actualIdentifiers.toString(), sampleIdentifiers.toString());
                samples.forEach(s -> assertEquals(s.getType().getCode(), typeCode, "Type of sample " + s.getIdentifier()));
            }
        }
    }

    private void assertExperiments(String sessionToken)
    {
        if (experimentIdentifiersByType.isEmpty() == false)
        {
            Set<Entry<String, List<String>>> entrySet = experimentIdentifiersByType.entrySet();
            for (Entry<String, List<String>> entry : entrySet)
            {
                String typeCode = entry.getKey();
                List<String> identifiers = entry.getValue();
                Collections.sort(identifiers);
                List<ExperimentIdentifier> experimentIds = identifiers.stream().map(ExperimentIdentifier::new).collect(Collectors.toList());
                ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
                fetchOptions.withType();
                Collection<Experiment> experiments = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).values();
                List<String> actualIdentifiers = experiments.stream().map(e -> e.getIdentifier().getIdentifier()).collect(Collectors.toList());
                Collections.sort(actualIdentifiers);
                assertEquals(actualIdentifiers.toString(), identifiers.toString());
                experiments.forEach(s -> assertEquals(s.getType().getCode(), typeCode, "Type of experiment " + s.getIdentifier()));
            }
        }
    }

    private void checkMissingAuthorizationExpectations()
    {
        Set<String> allSpaces = new TreeSet<>();
        allSpaces.addAll(globalSpaces);
        for (String group : groups)
        {
            Collection<List<String>> values = commonSpaces.values();
            for (List<String> spaces : values)
            {
                allSpaces.addAll(spaces.stream().map(s -> createCommonSpaceCode(group, s)).collect(Collectors.toSet()));
            }
        }
        allSpaces.removeAll(usersByLevelBySpace.keySet());
        if (allSpaces.isEmpty() == false)
        {
            fail("For the following spaces no authorization expectations have been added: " + allSpaces);
        }
    }

    private void assertSpaces(String sessionToken)
    {
        Set<String> spaceCodes = usersByLevelBySpace.keySet();
        List<SpacePermId> spaceIds = spaceCodes.stream().map(SpacePermId::new).collect(Collectors.toList());
        Collection<Space> spaces = v3api.getSpaces(sessionToken, spaceIds, new SpaceFetchOptions()).values();
        List<String> actualSpaces = spaces.stream().map(Space::getCode).collect(Collectors.toList());
        Collections.sort(actualSpaces);
        assertEquals(actualSpaces.toString(), spaceCodes.toString());
    }

    private void assertAuthorization()
    {
        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();
        for (Entry<String, Map<AuthorizationLevel, Set<String>>> entry : usersByLevelBySpace.entrySet())
        {
            String spaceCode = entry.getKey();
            expected.append(spaceCode).append(":");
            actual.append(spaceCode).append(":");
            Map<String, AuthorizationLevel> levelByUser = getLevelsByUser(spaceCode, entry.getValue());
            for (Entry<String, AuthorizationLevel> entry2 : levelByUser.entrySet())
            {
                String userId = entry2.getKey();
                AuthorizationLevel level = entry2.getValue();
                String sessionToken = v3api.login(userId, PASSWORD);
                assertNotNull(sessionToken, "User " + userId + " can not be authenticated.");
                try
                {
                    IOperationContext context = new OperationContext(sessionManager.getSession(sessionToken));
                    AuthorizationLevel actualLevel = getActualLevel(context, testService, spaceCode);
                    expected.append(" ").append(userId).append(":").append(level);
                    actual.append(" ").append(userId).append(":").append(actualLevel);
                } finally
                {
                    v3api.logout(sessionToken);
                }
            }
            expected.append("\n");
            actual.append("\n");
        }
        assertEquals(actual.toString(), expected.toString());
    }

    private Map<String, AuthorizationLevel> getLevelsByUser(String spaceCode, Map<AuthorizationLevel, Set<String>> usersByLevel)
    {
        Set<String> expectedUsers = new TreeSet<>(users);
        Map<String, AuthorizationLevel> levelByUser = new TreeMap<>();
        for (Entry<AuthorizationLevel, Set<String>> entry : usersByLevel.entrySet())
        {
            AuthorizationLevel level = entry.getKey();
            for (String userId : entry.getValue())
            {
                expectedUsers.remove(userId);
                AuthorizationLevel previous = levelByUser.put(userId, level);
                if (previous != null)
                {
                    fail("There is already an authorization expectation specified for user " + userId
                            + " for space " + spaceCode + ": " + previous);
                }
            }
        }
        if (expectedUsers.isEmpty() == false)
        {
            fail("Missing authorization expectation for the following users for space " + spaceCode + ": " + expectedUsers);
        }
        return levelByUser;
    }

    private void assertUnknownUsers(String sessionToken)
    {
        if (unknownUsers.isEmpty())
        {
            return;
        }
        Map<String, Set<String>> usersByGroupId = getAllUsersOfGroups(sessionToken);
        assertEquals(usersByGroupId.isEmpty(), false);
        List<PersonPermId> personIds = unknownUsers.stream().map(p -> new PersonPermId(p.getUserId())).collect(Collectors.toList());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments();
        for (Person person : v3api.getPersons(sessionToken, personIds, fetchOptions).values())
        {
            String userId = person.getUserId();
            assertEquals(person.isActive(), Boolean.valueOf(deactivation == false), "Active flag of " + person);
            String homeSpace = homeSpacesByUserId.get(userId);
            String space = person.getSpace() != null ? person.getSpace().getCode() : null;
            assertEquals(space, homeSpace, "Home space of " + person);
            assertEquals(person.getRoleAssignments().size(), 0, "Role assignments of " + person);
            for (Entry<String, Set<String>> entry : usersByGroupId.entrySet())
            {
                if (entry.getValue().contains(userId))
                {
                    fail("User " + userId + " still in authorization group " + entry.getKey());
                }
            }
        }
    }

    private Map<String, Set<String>> getAllUsersOfGroups(String sessionToken)
    {
        AuthorizationGroupFetchOptions groupFetchOptions = new AuthorizationGroupFetchOptions();
        groupFetchOptions.withUsers();
        List<AuthorizationGroupPermId> ids = new ArrayList<>();
        for (String groupCode : groups)
        {
            ids.add(new AuthorizationGroupPermId(groupCode));
            ids.add(new AuthorizationGroupPermId(UserManager.createAdminGroupCode(groupCode)));
        }
        Map<String, Set<String>> usersByGroupId = new TreeMap<>();
        for (AuthorizationGroup group : v3api.getAuthorizationGroups(sessionToken, ids, groupFetchOptions).values())
        {
            usersByGroupId.put(group.getCode(), group.getUsers().stream().map(Person::getUserId).collect(Collectors.toSet()));
        }
        return usersByGroupId;
    }

    private void assertUsers(String sessionToken)
    {
        Set<String> unknownUserIds = unknownUsers.stream().map(Principal::getUserId).collect(Collectors.toSet());
        List<PersonPermId> allUsers = homeSpacesByUserId.keySet().stream()
                .map(userId -> new PersonPermId(userId)).collect(Collectors.toList());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, allUsers, fetchOptions);
        Function<IPersonId, PersonPermId> mapper = id -> (PersonPermId) id;
        List<String> expectedUsers = extractedSortedPermIds(allUsers);
        List<String> actualUsers = extractedSortedPermIds(persons.keySet().stream().map(mapper).collect(Collectors.toSet()));
        assertEquals(actualUsers.toString(), expectedUsers.toString(), "Users");
        for (PersonPermId id : allUsers)
        {
            Person person = persons.get(id);
            assertEquals(person.getUserId(), id.getPermId());
            Boolean active = unknownUserIds.contains(id.getPermId()) == false;
            assertEquals(person.isActive(), active, "Active flag of " + person);
            assertEquals(person.getEmail(), "franz-josef.elmer@systemsx.ch", "Wrong email of " + person);
            String homeSpaceCode = person.getSpace() == null ? null : person.getSpace().getCode();
            assertEquals(homeSpaceCode, homeSpacesByUserId.get(person.getUserId()), "Wrong home space of " + person);
        }
    }

    private void assertAuthorizationOfGlobalSpaces()
    {
        if (globalSpaces.isEmpty() == false)
        {
            AuthorizationExpectations expectations = new AuthorizationExpectations(v3api, sessionManager, testService);
            for (String user : users)
            {
                globalSpaces.forEach(space -> expectations.expect(user, space, AuthorizationLevel.SPACE_OBSERVER));
            }
            expectations.assertExpectations();
        }
    }

    private static final class AuthorizationExpectations
    {
        private Map<String, Map<AuthorizationLevel, Set<String>>> spacesByLevelsByUsers = new TreeMap<>();

        private IApplicationServerInternalApi v3api;

        private IOpenBisSessionManager sessionManager;

        private UserManagerTestService testService;

        public AuthorizationExpectations(IApplicationServerInternalApi v3api, IOpenBisSessionManager sessionManager,
                UserManagerTestService testService)
        {
            this.v3api = v3api;
            this.sessionManager = sessionManager;
            this.testService = testService;
        }

        void expect(String userId, String space, AuthorizationLevel level)
        {
            Map<AuthorizationLevel, Set<String>> spacesByLevels = spacesByLevelsByUsers.get(userId);
            if (spacesByLevels == null)
            {
                spacesByLevels = new TreeMap<>();
                spacesByLevelsByUsers.put(userId, spacesByLevels);
            }
            Set<String> spaces = spacesByLevels.get(level);
            if (spaces == null)
            {
                spaces = new TreeSet<>();
                spacesByLevels.put(level, spaces);
            }
            spaces.add(space);
        }

        void assertExpectations()
        {
            StringBuilder builder = new StringBuilder();
            for (Entry<String, Map<AuthorizationLevel, Set<String>>> entry : spacesByLevelsByUsers.entrySet())
            {
                String userId = entry.getKey();
                String sessionToken = v3api.login(userId, PASSWORD);
                int count = 0;
                try
                {
                    Session session = sessionManager.getSession(sessionToken);
                    IOperationContext context = new OperationContext(session);
                    for (Entry<AuthorizationLevel, Set<String>> entry2 : entry.getValue().entrySet())
                    {
                        AuthorizationLevel level = entry2.getKey();
                        for (String space : entry2.getValue())
                        {
                            AuthorizationLevel actualLevel = UserManagerExpectationsBuilder.getActualLevel(context, this.testService, space);
                            if (level.equals(actualLevel) == false)
                            {
                                builder.append("Authorization level for user ").append(userId).append(" on space ").append(space);
                                builder.append(". Expected: ").append(level).append(", but found: ").append(actualLevel).append("\n");
                            }
                            count++;
                        }
                    }
                } finally
                {
                    v3api.logout(sessionToken);
                }
                System.out.println(count + " authorization expectations tested for user " + userId);
            }
            if (builder.length() > 0)
            {
                fail(builder.toString().trim());
            }
        }
    }

    private List<String> extractedSortedPermIds(Collection<? extends ObjectPermId> ids)
    {
        List<String> result = ids.stream().map(ObjectPermId::getPermId).collect(Collectors.toList());
        Collections.sort(result);
        return result;
    }

    private List<String> getUserIds(Principal... users)
    {
        return getUserIds(useEmailAsUserId, users);
    }

    static List<String> getUserIds(boolean useEmailAsUserId, Principal... users)
    {
        Stream<String> stream;
        if (useEmailAsUserId)
        {
            stream = Arrays.asList(users).stream().map(Principal::getEmail).map(em -> ServerUtils.escapeEmail(em));
        } else
        {
            stream = Arrays.asList(users).stream().map(Principal::getUserId);
        }
        return stream.collect(Collectors.toList());
    }

    private static String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
    }

    private static AuthorizationLevel getActualLevel(IOperationContext context, UserManagerTestService testService, String spaceCode)
    {
        SpacePE space = new SpacePE();
        space.setCode(spaceCode);
        AuthorizationLevel previousLevel = null;
        for (AuthorizationLevel level : AuthorizationLevel.values())
        {
            try
            {
                level.probe(testService, context, space);
            } catch (Exception e)
            {
                break;
            }
            previousLevel = level;
        }
        return previousLevel;
    }

    static class UserManagerAuthorizationExpectationsBuilder
    {
        private Map<AuthorizationLevel, Set<String>> usersByLevel;

        private UserManagerExpectationsBuilder userManagerExpectationsBuilder;

        private boolean useEmailAsUserId;

        public UserManagerAuthorizationExpectationsBuilder(Map<AuthorizationLevel, Set<String>> usersByLevel,
                UserManagerExpectationsBuilder userManagerExpectationsBuilder, boolean useEmailAsUserId)
        {
            this.usersByLevel = usersByLevel;
            this.userManagerExpectationsBuilder = userManagerExpectationsBuilder;
            this.useEmailAsUserId = useEmailAsUserId;
        }

        UserManagerAuthorizationExpectationsBuilder non(Principal... users)
        {
            addUsers(AuthorizationLevel.NON, users);
            return this;
        }

        UserManagerAuthorizationExpectationsBuilder observer(Principal... users)
        {
            addUsers(AuthorizationLevel.SPACE_OBSERVER, users);
            return this;
        }

        UserManagerAuthorizationExpectationsBuilder user(Principal... users)
        {
            addUsers(AuthorizationLevel.SPACE_USER, users);
            return this;
        }

        UserManagerAuthorizationExpectationsBuilder admin(Principal... users)
        {
            addUsers(AuthorizationLevel.SPACE_ADMIN, users);
            return this;
        }

        UserManagerAuthorizationExpectationsBuilder instanceAdmin(Principal... users)
        {
            addUsers(AuthorizationLevel.INSTANCE_ADMIN, users);
            return this;
        }
        
        private void addUsers(AuthorizationLevel level, Principal... users)
        {
            Set<String> set = usersByLevel.get(level);
            if (set == null)
            {
                set = new TreeSet<>();
                usersByLevel.put(level, set);
            }
            for (Principal user : users)
            {
                set.add(getUserIds(useEmailAsUserId, user).get(0));
            }
        }

        void assertExpectations()
        {
            userManagerExpectationsBuilder.assertExpectations();
        }
    }

}