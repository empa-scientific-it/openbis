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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.UpdateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.UpdatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.DeleteRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * @author Franz-Josef Elmer
 */
public class UserManager
{
    private static final String ADMIN_POSTFIX = "_ADMIN";

    private static final String GLOBAL_AUTHORIZATION_GROUP_CODE = "ALL_GROUPS";

    private static final AuthorizationGroupPermId GLOBAL_AUTHORIZATION_GROUP_ID = new AuthorizationGroupPermId(GLOBAL_AUTHORIZATION_GROUP_CODE);

    private final IAuthenticationService authenticationService;

    private final IApplicationServerInternalApi service;

    private final ISimpleLogger logger;

    private final UserManagerReport report;

    private final Map<String, UserInfo> userInfosByUserId = new TreeMap<>();

    private final Map<String, Map<String, Principal>> usersByGroupCode = new LinkedHashMap<>();

    private final Map<String, UserGroup> groupsByCode = new LinkedHashMap<>();

    private List<String> globalSpaces = new ArrayList<>();

    private Map<Role, List<String>> commonSpacesByRole = new HashMap<>();

    private Map<String, String> commonSamples = new HashMap<>();

    private List<Map<String, String>> commonExperiments;

    private List<String> instanceAdmins;

    private Map<String, HomeSpaceRequest> requestedHomeSpaceByUserId = new TreeMap<>();

    private File shareIdsMappingFileOrNull;

    private List<MappingAttributes> mappingAttributesList = new ArrayList<>();

    private boolean deactivateUnknownUsers;

    private boolean reuseHomeSpace;

    public UserManager(IAuthenticationService authenticationService, IApplicationServerInternalApi service,
            File shareIdsMappingFileOrNull, ISimpleLogger logger, UserManagerReport report)
    {
        this.authenticationService = authenticationService;
        this.service = service;
        this.shareIdsMappingFileOrNull = shareIdsMappingFileOrNull;
        this.logger = logger;
        this.report = report;
    }

    public void setGlobalSpaces(List<String> globalSpaces)
    {
        this.globalSpaces = globalSpaces;
    }

    public void setInstanceAdmins(List<String> instanceAdmins)
    {
        this.instanceAdmins = instanceAdmins;
    }

    public void setCommon(Map<Role, List<String>> commonSpacesByRole, Map<String, String> commonSamples,
                          List<Map<String, String>> commonExperiments)
    {
        this.commonSpacesByRole = commonSpacesByRole;
        this.commonSamples = commonSamples;
        this.commonExperiments = commonExperiments;
        Set<String> commonSpaces = new HashSet<>();
        commonSpacesByRole.values().forEach(spaces -> commonSpaces.addAll(spaces));
        checkIdentifierTemplates(commonSamples, commonSpaces, "sample", "<common space code>/<common sample code>");
        checkIdentifierTemplates(commonExperiments, commonSpaces, "experiment",
                "<common space code>/<common project code>/<common experiment code>");
    }

    private void checkIdentifierTemplates(List<Map<String, String>> commonEntities, Set<String> commonSpaces,
                                          String entityKind, String templateSchema)
    {
        for (Map<String, String> templateModel : commonEntities)
        {
            String identifierTemplate = templateModel.get("identifierTemplate");
            String[] parts = identifierTemplate.split("/");
            if (commonSpaces.contains(parts[0]) == false)
            {
                throw createConfigException(identifierTemplate, templateSchema, "No common space for common " + entityKind);
            }
            if (parts.length != templateSchema.split("/").length)
            {
                throw createConfigException(identifierTemplate, templateSchema, "");
            }
        }
    }

    private void checkIdentifierTemplates(Map<String, String> commonEntities, Set<String> commonSpaces,
            String entityKind, String templateSchema)
    {
        for (String identifierTemplate : commonEntities.keySet())
        {
            String[] parts = identifierTemplate.split("/");
            if (commonSpaces.contains(parts[0]) == false)
            {
                throw createConfigException(identifierTemplate, templateSchema, "No common space for common " + entityKind);
            }
            if (parts.length != templateSchema.split("/").length)
            {
                throw createConfigException(identifierTemplate, templateSchema, "");
            }
        }
    }

    private ConfigurationFailureException createConfigException(String identifierTemplate, String templateSchema, String message)
    {
        return new ConfigurationFailureException("Identifier template '" + identifierTemplate + "' is invalid"
                + (StringUtils.isBlank(message) ? ". " : " (reason: " + message + "). ") + "Template schema: " + templateSchema);
    }

    public void setDeactivateUnknownUsers(boolean deactivateUnknownUsers)
    {
        this.deactivateUnknownUsers = deactivateUnknownUsers;
    }

    public void addGroup(UserGroup group, Map<String, Principal> principalsByUserId)
    {
        String groupCode = group.getKey().toUpperCase();
        usersByGroupCode.put(groupCode, group.isEnabled() ? principalsByUserId : new HashMap<>());
        groupsByCode.put(groupCode, group);
        mappingAttributesList.add(new MappingAttributes(groupCode, group.getShareIds()));
        Set<String> admins = asSet(group.getAdmins());
        if (group.isEnabled())
        {
            for (Principal principal : principalsByUserId.values())
            {
                String userId = getUserId(principal, group.isUseEmailAsUserId());
                UserInfo userInfo = userInfosByUserId.get(userId);
                if (userInfo == null)
                {
                    userInfo = new UserInfo(principal);
                    userInfosByUserId.put(userId, userInfo);
                }
                userInfo.addGroupInfo(new GroupInfo(groupCode, admins.contains(userId)));
            }
        }
        logger.log(LogLevel.INFO, principalsByUserId.size() + " users for " + (group.isEnabled() ? "" : "disabled ") + "group " + groupCode);
    }

    public void manage(Set<String> knownUsers, Set<String> usersToBeIgnored)
    {
        String sessionToken = null;
        try
        {
            sessionToken = service.loginAsSystem();

            List<AuthorizationGroup> groupsToBeRemoved = getGroupsToBeRemoved(sessionToken);
            updateMappingFile();
            manageGlobalSpaces(sessionToken, report);
            if (deactivateUnknownUsers)
            {
                revokeUnknownUsers(sessionToken, knownUsers, report);
            }
            CurrentState currentState = loadCurrentState(sessionToken, service);
            manageInstanceAdmins(sessionToken, currentState, report);
            removeGroups(sessionToken, currentState, groupsToBeRemoved, usersToBeIgnored, report);
            for (Entry<String, Map<String, Principal>> entry : usersByGroupCode.entrySet())
            {
                String groupCode = entry.getKey();
                Map<String, Principal> users = entry.getValue();
                manageGroup(sessionToken, groupCode, users, usersToBeIgnored, currentState, report);
            }
            updateHomeSpaces(sessionToken, currentState, report);
            removeUsersFromGlobalGroup(sessionToken, currentState, report);
        } catch (Throwable e)
        {
            report.addErrorMessage("Error: " + e.toString());
            logger.log(LogLevel.ERROR, "", e);
        } finally
        {
            try
            {
                service.logout(sessionToken);
            } catch (Throwable e)
            {
                report.addErrorMessage("Error: " + e.toString());
                logger.log(LogLevel.ERROR, "", e);
            }
        }
    }

    /*
     * Get groups to be removed by the following heuristics: 
     * 1. Find all groups which ends with <code>_ADMIN</code>. 
     * 2. Get for each admin group the
     * corresponding group. 
     * 3. Take it as a deleted if it isn't specific in the list of added groups as specified in configuration 
     *   AND if the users of the admin group are also in the group.
     */
    private List<AuthorizationGroup> getGroupsToBeRemoved(String sessionToken)
    {
        Map<String, AuthorizationGroup> adminGroupsByGroupId = getAdminGroups(sessionToken);
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        searchCriteria.withCodes().setFieldValue(adminGroupsByGroupId.keySet());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        fetchOptions.withRegistrator();
        List<AuthorizationGroup> groups = service.searchAuthorizationGroups(sessionToken, searchCriteria,
                fetchOptions).getObjects();
        List<AuthorizationGroup> removedGroups = new ArrayList<>();
        for (AuthorizationGroup group : groups)
        {
            AuthorizationGroup adminGroup = adminGroupsByGroupId.get(group.getCode());
            if (groupsByCode.containsKey(group.getCode()) == false && adminGroup != null)
            {
                Set<String> users = extractUserIds(group);
                if (users.containsAll(extractUserIds(adminGroup)))
                {
                    /*\
                     * The maintenance task should only remove groups created by itself
                     */
                    if (group.getRegistrator().getUserId().equals("system"))
                    {
                        removedGroups.add(group);
                    }
                }
            }
        }
        return removedGroups;
    }

    private Map<String, AuthorizationGroup> getAdminGroups(String sessionToken)
    {
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        searchCriteria.withCode().thatEndsWith(ADMIN_POSTFIX);
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        fetchOptions.withRegistrator();
        List<AuthorizationGroup> adminGroups = service.searchAuthorizationGroups(sessionToken, searchCriteria,
                fetchOptions).getObjects();
        Map<String, AuthorizationGroup> adminGroupsByGroupId = new HashMap<>();
        for (AuthorizationGroup adminGroup : adminGroups)
        {
            adminGroupsByGroupId.put(adminGroup.getCode().split(ADMIN_POSTFIX)[0], adminGroup);
        }
        return adminGroupsByGroupId;
    }

    private Set<String> extractUserIds(AuthorizationGroup group)
    {
        return group.getUsers().stream().map(Person::getUserId).collect(Collectors.toSet());
    }

    private void removeGroups(String sessionToken, CurrentState currentState, List<AuthorizationGroup> groups,
            Set<String> usersToBeIgnored, UserManagerReport report)
    {
        List<IAuthorizationGroupId> groupIds = new ArrayList<>();
        Context context = new Context(sessionToken, service, currentState, report);
        for (AuthorizationGroup group : groups)
        {
            Set<String> users = extractUserIds(group);
            users.removeAll(usersToBeIgnored);
            removeUsersFromGroup(context, group.getCode(), users);
            groupIds.add(group.getPermId());
            report.removeGroup(group.getCode());
            String adminGroupCode = group.getCode() + ADMIN_POSTFIX;
            groupIds.add(new AuthorizationGroupPermId(adminGroupCode));
            report.removeGroup(adminGroupCode);
        }
        context.executeOperations();
        AuthorizationGroupDeletionOptions deletionOptions = new AuthorizationGroupDeletionOptions();
        deletionOptions.setReason("Deletion of groups " + groupIds);
        service.deleteAuthorizationGroups(sessionToken, groupIds, deletionOptions);
    }

    private void updateMappingFile()
    {
        if (shareIdsMappingFileOrNull != null)
        {
            File parentFile = shareIdsMappingFileOrNull.getParentFile();
            parentFile.mkdirs();
            File newFile = new File(parentFile, shareIdsMappingFileOrNull.getName() + ".new");
            PrintWriter printWriter = null;
            try
            {
                printWriter = new PrintWriter(newFile);
                printWriter.println("Identifier\tShare IDs\tArchive Folder");
                for (MappingAttributes attributes : mappingAttributesList)
                {
                    CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
                    List<String> shareIds = attributes.getShareIds();
                    if (shareIds != null && shareIds.isEmpty() == false)
                    {
                        shareIds.forEach(id -> builder.append(id));
                        printWriter.println(String.format("/%s_.*\t%s\t", attributes.getGroupCode(), builder.toString()));
                    }
                }
            } catch (IOException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            } finally
            {
                IOUtils.closeQuietly(printWriter);
            }
            newFile.renameTo(shareIdsMappingFileOrNull);
        }
    }

    private void updateHomeSpaces(String sessionToken, CurrentState currentState, UserManagerReport report)
    {
        List<PersonUpdate> updates = new ArrayList<>();
        for (Entry<String, HomeSpaceRequest> entry : requestedHomeSpaceByUserId.entrySet())
        {
            String userId = entry.getKey();
            HomeSpaceRequest request = entry.getValue();
            SpacePermId requestedHomeSpace = request.getHomeSpace();
            if (requestedHomeSpace != null)
            {
                updates.add(createPersonUpdate(userId, requestedHomeSpace, report));
            }
        }
        if (updates.isEmpty() == false)
        {
            service.updatePersons(sessionToken, updates);
        }
    }

    private PersonUpdate createPersonUpdate(String userId, SpacePermId spacePermId, UserManagerReport report)
    {
        IPersonId personId = new PersonPermId(userId);
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(spacePermId);
        report.assignHomeSpace(userId, spacePermId);
        return personUpdate;
    }

    private void removeUsersFromGlobalGroup(String sessionToken, CurrentState currentState, UserManagerReport report)
    {
        AuthorizationGroup globalGroup = currentState.getGlobalGroup();
        if (globalGroup != null)
        {
            Context context = new Context(sessionToken, service, currentState, report);

            for (String userId : currentState.getUsersToBeRemovedFromGlobalGroup())
            {
                removePersonFromAuthorizationGroup(context, globalGroup.getCode(), userId);
            }
            context.executeOperations();
        }
    }

    private void manageInstanceAdmins(String sessionToken, CurrentState currentState, UserManagerReport report)
    {
        if (instanceAdmins != null)
        {
            Context context = new Context(sessionToken, service, currentState, report);
            for (String instanceAdmin : instanceAdmins)
            {
                if (context.getCurrentState().userExists(instanceAdmin) == false)
                {
                    createUser(context, instanceAdmin);
                }
                PersonPermId userId = new PersonPermId(instanceAdmin);
                if (isInstanceAdmin(sessionToken, userId) == false)
                {
                    RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
                    roleCreation.setRole(Role.ADMIN);
                    roleCreation.setUserId(userId);
                    context.add(roleCreation);
                    context.getReport().assignRoleTo(instanceAdmin, Role.ADMIN, null);
                }
            }
            context.executeOperations();
        }
    }

    private boolean isInstanceAdmin(String sessionToken, PersonPermId userId)
    {
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRoleAssignments().withRegistrator();
        Person user = service.getPersons(sessionToken, Arrays.asList(userId), fetchOptions).get(userId);
        if (user == null)
        {
            return false;
        }
        List<RoleAssignment> instanceAdminRole = user.getRoleAssignments().stream().filter(
                ra -> ra.getRoleLevel() == RoleLevel.INSTANCE && ra.getSpace() == null)
                .collect(Collectors.toList());
        return instanceAdminRole.isEmpty() == false;
    }

    private void manageGlobalSpaces(String sessionToken, UserManagerReport report)
    {
        if (globalSpaces != null && globalSpaces.isEmpty() == false)
        {
            createGlobalSpaces(sessionToken, report);
            Set<String> knownGlobalSpaces = createGlobalGroupAndGetKnownSpaces(sessionToken, GLOBAL_AUTHORIZATION_GROUP_ID, report);
            createGlobalRoleAssignments(sessionToken, GLOBAL_AUTHORIZATION_GROUP_ID, knownGlobalSpaces, report);
        }
    }

    private void createGlobalSpaces(String sessionToken, UserManagerReport report)
    {
        List<SpacePermId> spaceIds = globalSpaces.stream().map(SpacePermId::new).collect(Collectors.toList());
        Set<ISpaceId> knownSpaces = service.getSpaces(sessionToken, spaceIds, new SpaceFetchOptions()).keySet();
        List<SpaceCreation> spaceCreations = new ArrayList<>();
        for (SpacePermId spaceId : spaceIds)
        {
            if (knownSpaces.contains(spaceId) == false)
            {
                SpaceCreation spaceCreation = new SpaceCreation();
                spaceCreation.setCode(spaceId.getPermId());
                spaceCreations.add(spaceCreation);
            }
        }
        if (spaceCreations.isEmpty() == false)
        {
            service.createSpaces(sessionToken, spaceCreations);
            report.addSpaces(spaceCreations);
        }
    }

    private Set<String> createGlobalGroupAndGetKnownSpaces(String sessionToken, AuthorizationGroupPermId groupId, UserManagerReport report)
    {
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRoleAssignments().withRegistrator();
        fetchOptions.withRegistrator();
        AuthorizationGroup group = service.getAuthorizationGroups(sessionToken, Arrays.asList(groupId), fetchOptions).get(groupId);
        Set<String> knownGlobalSpaces = new TreeSet<>();
        if (group == null)
        {
            AuthorizationGroupCreation groupCreation = new AuthorizationGroupCreation();
            groupCreation.setCode(GLOBAL_AUTHORIZATION_GROUP_CODE);
            groupCreation.setDescription("Authorization group for all users of all groups");
            service.createAuthorizationGroups(sessionToken, Arrays.asList(groupCreation));
            report.addGroup(GLOBAL_AUTHORIZATION_GROUP_CODE);
        } else
        {
            for (RoleAssignment roleAssignment : group.getRoleAssignments())
            {
                if (RoleLevel.SPACE.equals(roleAssignment.getRoleLevel()) && Role.OBSERVER.equals(roleAssignment.getRole()))
                {
                    knownGlobalSpaces.add(roleAssignment.getSpace().getCode());
                }
            }
        }
        return knownGlobalSpaces;
    }

    private void createGlobalRoleAssignments(String sessionToken, AuthorizationGroupPermId groupId, Set<String> knownGlobalSpaces,
            UserManagerReport report)
    {
        List<RoleAssignmentCreation> assignmentCreations = new ArrayList<>();
        for (String spaceCode : globalSpaces)
        {
            if (knownGlobalSpaces.contains(spaceCode) == false)
            {
                RoleAssignmentCreation assignmentCreation = new RoleAssignmentCreation();
                assignmentCreation.setAuthorizationGroupId(groupId);
                assignmentCreation.setRole(Role.OBSERVER);
                SpacePermId spaceId = new SpacePermId(spaceCode);
                assignmentCreation.setSpaceId(spaceId);
                assignmentCreations.add(assignmentCreation);
                report.assignRoleTo(groupId, assignmentCreation.getRole(), spaceId);
            }
        }
        if (assignmentCreations.isEmpty() == false)
        {
            service.createRoleAssignments(sessionToken, assignmentCreations);
        }
    }

    private void revokeUnknownUsers(String sessionToken, Set<String> knownUsers, UserManagerReport report)
    {
        List<PersonUpdate> updates = new ArrayList<>();
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRegistrator();
        List<Person> persons = service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
        for (Person person : persons)
        {
            /*\
             * The maintenance task should only disable users created by itself
             */
            if (person.isActive() && person.getRegistrator() != null // user 'system' has no registrator
                    && isKnownUser(knownUsers, person) == false
                    && person.getRegistrator().getUserId().equals("system"))
            {
                PersonUpdate update = new PersonUpdate();
                update.setUserId(person.getPermId());
                update.deactivate();
                updates.add(update);
                report.deactivateUser(person.getUserId());
            }
        }
        if (updates.isEmpty() == false)
        {
            service.updatePersons(sessionToken, updates);
        }
    }

    private boolean isKnownUser(Set<String> knownUsers, Person person)
    {
        String userId = person.getUserId();
        if (knownUsers.contains(userId))
        {
            return true;
        }
        try
        {
            UserInfo userInfo = userInfosByUserId.get(userId);
            if (userInfo != null)
            {
                userId = userInfo.principal.getUserId();
            }
            authenticationService.getPrincipal(userId);
            return true;
        } catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    private CurrentState loadCurrentState(String sessionToken, IApplicationServerInternalApi service)
    {
        List<AuthorizationGroup> authorizationGroups = getAllAuthorizationGroups(sessionToken, service);
        List<Person> users = getAllUsers(sessionToken, service);
        List<Space> spaces = getAllSpaces(sessionToken, service);
        List<AuthorizationGroupPermId> ids = Arrays.asList(GLOBAL_AUTHORIZATION_GROUP_ID);
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRoleAssignments().withRegistrator();
        fetchOptions.withUsers();
        fetchOptions.withRegistrator();
        AuthorizationGroup group = service.getAuthorizationGroups(sessionToken, ids, fetchOptions).get(GLOBAL_AUTHORIZATION_GROUP_ID);
        return new CurrentState(authorizationGroups, group, spaces, users);
    }

    private List<AuthorizationGroup> getAllAuthorizationGroups(String sessionToken, IApplicationServerInternalApi service)
    {
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withUsers().withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRoleAssignments().withRegistrator();
        return service.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private List<Person> getAllUsers(String sessionToken, IApplicationServerInternalApi service)
    {
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRoleAssignments().withRegistrator();
        fetchOptions.withSpace();
        return service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private List<Space> getAllSpaces(String sessionToken, IApplicationServerInternalApi service)
    {
        SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        return service.searchSpaces(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private void manageGroup(String sessionToken, String groupCode, Map<String, Principal> groupUsers,
            Set<String> usersToBeIgnored, CurrentState currentState, UserManagerReport report)
    {
        try
        {
            Context context = new Context(sessionToken, service, currentState, report);
            if (currentState.groupExists(groupCode))
            {
                manageKnownGroup(context, groupCode, groupUsers, usersToBeIgnored);
            } else
            {
                manageNewGroup(context, groupCode, groupUsers, usersToBeIgnored);
            }
            createSamples(context, groupCode);
            createExperiments(context, groupCode);
            context.executeOperations();
        } catch (Exception e)
        {
            String message = String.format("Couldn't manage group '%s' because of the following error: %s",
                    groupCode, e);
            report.addErrorMessage(message);
            logger.log(LogLevel.ERROR, message, e);
        }
    }

    private void createSamples(Context context, String groupCode)
    {
        if (commonSamples.isEmpty() == false)
        {
            Set<SampleIdentifier> sampleIdentifiers = new LinkedHashSet<>();
            String sessionToken = context.getSessionToken();
            for (Entry<String, String> entry : commonSamples.entrySet())
            {
                String sampleType = entry.getValue();
                String[] identifierTemplateParts = entry.getKey().split("/");
                String spaceCode = createCommonSpaceCode(groupCode, identifierTemplateParts[0]);
                String sampleCode = createCommonSpaceCode(groupCode, identifierTemplateParts[1]);
                SampleIdentifier sampleId = new SampleIdentifier(spaceCode, null, sampleCode);
                sampleIdentifiers.add(sampleId);
                if (service.getSamples(sessionToken, Arrays.asList(sampleId), new SampleFetchOptions()).isEmpty())
                {
                    SampleCreation sampleCreation = new SampleCreation();
                    sampleCreation.setCode(sampleCode);
                    sampleCreation.setTypeId(new EntityTypePermId(sampleType));
                    sampleCreation.setSpaceId(new SpacePermId(spaceCode));
                    if (sampleType.equals("GENERAL_ELN_SETTINGS")) {
                        sampleCreation.setProperty("$ELN_SETTINGS", getCommonSpacesConfiguration(groupCode, commonSpacesByRole));
                    }
                    context.add(sampleCreation);
                    context.getReport().addSample(sampleId);
                }
            }
        }
    }

    private String getCommonSpacesConfiguration(String groupCode, Map<Role, List<String>> commonSpacesByRole) {
        String  jsonConfiguration = "";
                jsonConfiguration += "{";
                jsonConfiguration += "\"inventorySpaces\":";
                jsonConfiguration += "[";
                boolean isFirstInventorySpace = true;
                for (Role role:commonSpacesByRole.keySet()) {
                    if (role != Role.OBSERVER) {
                        List<String> spaceCodes = commonSpacesByRole.get(role);
                        for (String spaceCode:spaceCodes) {
                            String groupSpaceCode = groupCode + "_" + spaceCode;
                            if (isFirstInventorySpace) {
                                isFirstInventorySpace = false;
                            } else {
                                jsonConfiguration += ",";
                            }
                            jsonConfiguration += "\"" + groupSpaceCode + "\"";
                        }
                    }
                }
                jsonConfiguration += "],";
                jsonConfiguration += "\"inventorySpacesReadOnly\":";
                jsonConfiguration += "[";
                boolean isFirstinventorySpacesReadOnly = true;
                List<String> spaceCodes = commonSpacesByRole.get(Role.OBSERVER);
                for (String spaceCode:spaceCodes) {
                    String groupSpaceCode = groupCode + "_" + spaceCode;
                    if (isFirstinventorySpacesReadOnly) {
                        isFirstinventorySpacesReadOnly = false;
                    } else {
                        jsonConfiguration += ",";
                    }
                    jsonConfiguration += "\"" + groupSpaceCode + "\"";
                }
                jsonConfiguration += "]";
                jsonConfiguration += "}";
        return jsonConfiguration;
    }

    private void createExperiments(Context context, String groupCode)
    {
        if (commonExperiments.isEmpty() == false)
        {
            Set<ProjectIdentifier> projectIdentifiers = new LinkedHashSet<>();
            for(Map<String, String> experimentTemplateModel:commonExperiments) {
                String[] identifierTemplateParts = experimentTemplateModel.get("identifierTemplate").split("/");
                String spaceCode = createCommonSpaceCode(groupCode, identifierTemplateParts[0]);
                String projectCode = createCommonSpaceCode(groupCode, identifierTemplateParts[1]);
                projectIdentifiers.add(new ProjectIdentifier(spaceCode, projectCode));
            }
            String sessionToken = context.getSessionToken();
            Set<IProjectId> existingProjects =
                    service.getProjects(sessionToken, new ArrayList<>(projectIdentifiers), new ProjectFetchOptions()).keySet();
            projectIdentifiers.removeAll(existingProjects);
            for (ProjectIdentifier identifier : projectIdentifiers)
            {
                ProjectCreation projectCreation = new ProjectCreation();
                String[] spaceCodeAndProjectCode = identifier.getIdentifier().split("/");
                projectCreation.setSpaceId(new SpacePermId(spaceCodeAndProjectCode[1]));
                projectCreation.setCode(spaceCodeAndProjectCode[2]);
                context.add(projectCreation);
                context.getReport().addProject(identifier);
            }

            for(Map<String, String> experimentTemplateModel:commonExperiments)
            {
                String experimentType = experimentTemplateModel.get("experimentType");
                String[] identifierTemplateParts = experimentTemplateModel.get("identifierTemplate").split("/");
                String spaceCode = createCommonSpaceCode(groupCode, identifierTemplateParts[0]);
                String projectCode = createCommonSpaceCode(groupCode, identifierTemplateParts[1]);
                String experimentCode = createCommonSpaceCode(groupCode, identifierTemplateParts[2]);
                ExperimentIdentifier identifier = new ExperimentIdentifier(spaceCode, projectCode, experimentCode);
                if (service.getExperiments(sessionToken, Arrays.asList(identifier), new ExperimentFetchOptions()).isEmpty())
                {
                    ExperimentCreation experimentCreation = new ExperimentCreation();
                    experimentCreation.setProjectId(new ProjectIdentifier(spaceCode, projectCode));
                    experimentCreation.setCode(experimentCode);
                    experimentCreation.setTypeId(new EntityTypePermId(experimentType));
                    for (String key: experimentTemplateModel.keySet()) { // Sets any properties available
                        if (key.equals("experimentType") == false && key.equals("identifierTemplate") == false && experimentTemplateModel.get(key) != null) {
                            experimentCreation.setProperty(key, experimentTemplateModel.get(key));
                        }
                    }
                    context.add(experimentCreation);
                    context.getReport().addExperiment(identifier);
                }
            }
        }
    }

    private void manageKnownGroup(Context context, String groupCode, Map<String, Principal> groupUsers, Set<String> usersToBeIgnored)
    {
        createCommonSpaces(context, groupCode);
        manageUsers(context, groupCode, groupUsers, usersToBeIgnored);
    }

    private void manageNewGroup(Context context, String groupCode, Map<String, Principal> groupUsers, Set<String> usersToBeIgnored)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);

        createAuthorizationGroup(context, groupCode);
        createAuthorizationGroup(context, adminGroupCode);

        createCommonSpaces(context, groupCode);

        manageUsers(context, groupCode, groupUsers, usersToBeIgnored);
    }

    private void createCommonSpaces(Context context, String groupCode)
    {
        for (Entry<Role, List<String>> entry : commonSpacesByRole.entrySet())
        {
            Role role = entry.getKey();
            for (String commonSpaceCode : entry.getValue())
            {
                String spaceCode = createCommonSpaceCode(groupCode, commonSpaceCode);
                Space space = context.getCurrentState().getSpace(spaceCode);
                ISpaceId spaceId = space != null ? space.getId() : createSpace(context, spaceCode);
                createRoleAssignment(context, new AuthorizationGroupPermId(groupCode), role, spaceId, spaceCode);
                createRoleAssignment(context, new AuthorizationGroupPermId(createAdminGroupCode(groupCode)), 
                        Role.ADMIN, spaceId, spaceCode);
            }
        }
    }

    private void manageUsers(Context context, String groupCode, Map<String, Principal> groupUsers, Set<String> usersToBeIgnored)
    {
        UserGroup group = groupsByCode.get(groupCode);
        Map<String, Person> currentUsersOfGroup = context.getCurrentState().getCurrentUsersOfGroup(groupCode);
        Set<String> usersToBeRemoved = new TreeSet<>(currentUsersOfGroup.keySet());
        usersToBeRemoved.removeAll(usersToBeIgnored);
        AuthorizationGroup globalGroup = context.getCurrentState().getGlobalGroup();
        String adminGroupCode = createAdminGroupCode(groupCode);
        boolean createUserSpace = group == null || group.isCreateUserSpace();
        boolean useEmailAsUserId = group != null && group.isUseEmailAsUserId();

        for (Principal user : groupUsers.values())
        {
            String userId = getUserId(user, useEmailAsUserId);
            usersToBeRemoved.remove(userId);
            PersonPermId personId = new PersonPermId(userId);
            if (currentUsersOfGroup.containsKey(userId) == false)
            {
                handleNewGroupUser(context, groupCode, createUserSpace, userId, personId);
            }
            addPersonToAuthorizationGroup(context, groupCode, userId);
            if (globalGroup != null)
            {
                context.getCurrentState().addPersonToGlobalGroup(userId);
                addPersonToAuthorizationGroup(context, globalGroup.getCode(), userId);
            }
            if (isAdmin(userId, groupCode))
            {
                addPersonToAuthorizationGroup(context, adminGroupCode, userId);
            } else
            {
                removePersonFromAuthorizationGroup(context, adminGroupCode, userId);
            }
        }
        removeUsersFromGroup(context, groupCode, usersToBeRemoved);
        handleRoleAssignmentForUserSpaces(context, groupCode);
    }

    private void handleRoleAssignmentForUserSpaces(Context context, String groupCode)
    {
        UserGroup group = groupsByCode.get(groupCode);
        Map<String, Person> currentUsersOfGroup = context.getCurrentState().getCurrentUsersOfGroup(groupCode);
        Set<String> allUserSpaces = getAllUserSpaces(context, groupCode, currentUsersOfGroup);
        Role userSpaceRole = group.getUserSpaceRole();
        AuthorizationGroup authorizationGroup = context.getCurrentState().groupsByCode.get(groupCode);
        AuthorizationGroupPermId groupId = new AuthorizationGroupPermId(groupCode);

        for (String spaceCode : allUserSpaces)
        {
            List<RoleAssignment> roleAssignments = authorizationGroup.getRoleAssignments().stream()
                    .filter(ra -> ra.getSpace() != null && ra.getSpace().getCode().equals(spaceCode))
                    .collect(Collectors.toList());
            for (RoleAssignment roleAssignment : roleAssignments)
            {
                Role role = roleAssignment.getRole();
                SpacePermId permId = roleAssignment.getSpace().getPermId();
                if (userSpaceRole != role)
                {
                    if (role != null)
                    {
                        /*\
                         * The maintenance task should only remove role assignments created by itself
                         */
                        if (roleAssignment.getRegistrator().getUserId().equals("system"))
                        {
                        context.delete(roleAssignment);
                        context.report.unassignRoleFrom(groupId, roleAssignment.getRole(), permId);
                        }
                    }
                    if (userSpaceRole != null)
                    {
                        createRoleAssignment(context, groupId, userSpaceRole, permId);
                    }
                }
            }
            if (roleAssignments.isEmpty() && userSpaceRole != null)
            {
                Space space = context.getCurrentState().getSpace(spaceCode);
                createRoleAssignment(context, groupId, userSpaceRole, space.getPermId());
            }
        }
    }

    private Set<String> getAllUserSpaces(Context context, String groupCode, Map<String, Person> currentUsersOfGroup)
    {
        String prefix = groupCode + "_";
        Set<String> allGroupSpaces = context.getCurrentState().spacesByCode.keySet().stream()
                .filter(space -> space.startsWith(prefix)).collect(Collectors.toSet());
        Set<String> allUserSpaces = new TreeSet<>();
        Set<String> users = currentUsersOfGroup.keySet().stream()
                .map(u -> u.toUpperCase()).collect(Collectors.toSet());
        for (String userId : users)
        {
            String space = prefix + userId;
            if (allGroupSpaces.remove(space))
            {
                allUserSpaces.add(space);
            }
        }
        // add also users spaces with code <group code>_<user id>_<number>
        Pattern pattern = Pattern.compile(prefix + "(.*?)(_[0-9]+)?$");
        for (String space : allGroupSpaces)
        {
            Matcher matcher = pattern.matcher(space);
            if (matcher.matches() && users.contains(matcher.group(1)))
            {
                allUserSpaces.add(space);
            }
        }
        return allUserSpaces;
    }

    private void handleNewGroupUser(Context context, String groupCode, boolean createUserSpace, String userId, PersonPermId personId)
    {
        SpacePermId userSpaceId = null;

        if (createUserSpace)
        {
            userSpaceId = createUserSpace(context, groupCode, userId);
        }

        Person knownUser = context.getCurrentState().getUser(userId);

        if (context.getCurrentState().userExists(userId) == false)
        {
            createUser(context, userId);
        } else if (knownUser != null && knownUser.isActive() == false)
        {
            PersonUpdate personUpdate = new PersonUpdate();
            personUpdate.setUserId(personId);
            personUpdate.activate();
            context.add(personUpdate);

            context.getReport().reuseUser(userId);
        }

        if (createUserSpace)
        {
            getHomeSpaceRequest(userId).setHomeSpace(userSpaceId);
            RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
            roleCreation.setUserId(personId);
            roleCreation.setRole(Role.ADMIN);
            roleCreation.setSpaceId(userSpaceId);
            context.add(roleCreation);
            context.getReport().assignRoleTo(userId, roleCreation.getRole(), roleCreation.getSpaceId());
            AuthorizationGroupPermId adminGroupId = new AuthorizationGroupPermId(createAdminGroupCode(groupCode));
            createRoleAssignment(context, adminGroupId, Role.ADMIN, userSpaceId);
            UserGroup group = groupsByCode.get(groupCode);
            Role userSpaceRole = group == null ? null : group.getUserSpaceRole();
            if (userSpaceRole != null)
            {
                createRoleAssignment(context, new AuthorizationGroupPermId(groupCode), userSpaceRole, userSpaceId);
            }
        }
    }

    private void createUser(Context context, String userId)
    {
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId(userId);
        context.add(personCreation);
        context.getCurrentState().addNewUser(userId);
        context.getReport().addUser(userId);
    }

    private void removeUsersFromGroup(Context context, String groupCode, Set<String> usersToBeRemoved)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        for (String userId : usersToBeRemoved)
        {
            removePersonFromAuthorizationGroup(context, groupCode, userId);
            removePersonFromAuthorizationGroup(context, adminGroupCode, userId);
            AuthorizationGroup globalGroup = context.getCurrentState().getGlobalGroup();
            if (globalGroup != null)
            {
                context.getCurrentState().removeUserFromGlobalGroup(userId);
            }
            Person user = context.getCurrentState().getUser(userId);
            for (RoleAssignment roleAssignment : user.getRoleAssignments())
            {
                Space space = roleAssignment.getSpace();
                String userSpace = createCommonSpaceCode(groupCode, userId.toUpperCase());
                if (space != null && space.getCode().startsWith(userSpace))
                {
                    /*\
                     * The maintenance task should only remove role assignments created by itself
                     */
                    if (roleAssignment.getRegistrator().getUserId().equals("system"))
                    {
                    context.delete(roleAssignment);
                    context.report.unassignRoleFrom(userId, roleAssignment.getRole(), space.getPermId());
                    }
                }
            }
        }
    }

    private String getUserId(Principal user, boolean useEmailAsUserId)
    {
        if (useEmailAsUserId && StringUtils.isNotBlank(user.getEmail()))
        {
            return  ServerUtils.escapeEmail(user.getEmail());
        }
        return user.getUserId();
    }

    private SpacePermId createUserSpace(Context context, String groupCode, String userId)
    {
        String userSpaceCode = createCommonSpaceCode(groupCode, userId.toUpperCase());
        int n = context.getCurrentState().getNumberOfSpacesStartingWith(userSpaceCode);
        if (n > 0) // Existing space, what to do depending on configuration
        {
            if(!reuseHomeSpace)
            {
                userSpaceCode += "_" + (n + 1);
                return createSpace(context, userSpaceCode);
            } else {
                return new SpacePermId(userSpaceCode);
            }
        } else {
            return createSpace(context, userSpaceCode);
        }
    }

    private HomeSpaceRequest getHomeSpaceRequest(String userId)
    {
        HomeSpaceRequest homeSpaceRequest = requestedHomeSpaceByUserId.get(userId);
        if (homeSpaceRequest == null)
        {
            homeSpaceRequest = new HomeSpaceRequest();
            requestedHomeSpaceByUserId.put(userId, homeSpaceRequest);
        }
        return homeSpaceRequest;
    }

    private boolean isAdmin(String userId, String groupCode)
    {
        UserInfo userInfo = userInfosByUserId.get(userId);
        if (userInfo == null)
        {
            return false;
        }
        GroupInfo groupInfo = userInfo.getGroupInfosByGroupKey().get(groupCode);
        return groupInfo != null && groupInfo.isAdmin();
    }

    private void addPersonToAuthorizationGroup(Context context, String groupCode, String userId)
    {
        if (context.getCurrentState().getCurrentUsersOfGroup(groupCode).keySet().contains(userId) == false)
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().add(new PersonPermId(userId));
            context.add(groupUpdate);
            context.getReport().addUserToGroup(groupCode, userId);
        }
    }

    private void removePersonFromAuthorizationGroup(Context context, String groupCode, String userId)
    {
        if (context.getCurrentState().getCurrentUsersOfGroup(groupCode).keySet().contains(userId))
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().remove(new PersonPermId(userId));
            context.add(groupUpdate);
            context.getReport().removeUserFromGroup(groupCode, userId);
        }
    }

    private AuthorizationGroupPermId createAuthorizationGroup(Context context, String groupCode)
    {
        AuthorizationGroupCreation creation = new AuthorizationGroupCreation();
        creation.setCode(groupCode);
        context.add(creation);
        context.getReport().addGroup(groupCode);
        return new AuthorizationGroupPermId(groupCode);
    }

    private void createRoleAssignment(Context context, AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId)
    {
        createRoleAssignment(context, groupId, role, spaceId, null);
    }

    // The space code is needed for logging because spaceId can be an instance of SpaceTechId
    private void createRoleAssignment(Context context, AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId, String spaceCodeOrNull)
    {
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withAuthorizationGroup().withId().thatEquals(groupId);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        fetchOptions.withRegistrator();
        List<RoleAssignment> roleAssignments = service.searchRoleAssignments(context.getSessionToken(), 
                searchCriteria, fetchOptions).getObjects();
        for (RoleAssignment roleAssignment : roleAssignments)
        {
            // Automatic role assignments are for spaces only
            if (roleAssignment.getRoleLevel() == RoleLevel.PROJECT) {
                continue;
            }
            if (roleAssignment.getRole().equals(role))
            {
                Space space = roleAssignment.getSpace();
                if ((space == null && spaceId == null) || space.getId().equals(spaceId) || space.getPermId().equals(spaceId))
                {
                    return;
                }
            }
        }
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setAuthorizationGroupId(groupId);
        roleCreation.setRole(role);
        roleCreation.setSpaceId(spaceId);
        context.add(roleCreation);
        if (spaceCodeOrNull != null)
        {
            spaceId = new SpacePermId(spaceCodeOrNull);
        }
        context.getReport().assignRoleTo(groupId, role, spaceId);
    }

    public void setReuseHomeSpace(boolean reuseHomeSpace)
    {
        this.reuseHomeSpace = reuseHomeSpace;
    }

    private static final class CurrentState
    {
        private Map<String, AuthorizationGroup> groupsByCode = new TreeMap<>();

        private Map<String, Space> spacesByCode = new TreeMap<>();

        private Map<String, Person> usersById = new TreeMap<>();

        private Set<String> newUsers = new TreeSet<>();

        private Set<String> usersAddedToGlobalGroup = new TreeSet<>();

        private Set<String> usersRemovedFromGlobalGroup = new TreeSet<>();

        private AuthorizationGroup globalGroup;

        CurrentState(List<AuthorizationGroup> authorizationGroups, AuthorizationGroup globalGroup, List<Space> spaces, List<Person> users)
        {
            this.globalGroup = globalGroup;
            authorizationGroups.forEach(group -> groupsByCode.put(group.getCode(), group));
            groupsByCode.put(GLOBAL_AUTHORIZATION_GROUP_CODE, globalGroup);
            spaces.forEach(space -> spacesByCode.put(space.getCode(), space));
            users.forEach(user -> usersById.put(user.getUserId(), user));
        }

        public void removeUserFromGlobalGroup(String userId)
        {
            usersRemovedFromGlobalGroup.add(userId);
        }

        public void addPersonToGlobalGroup(String userId)
        {
            usersAddedToGlobalGroup.add(userId);
        }

        public Set<String> getUsersToBeRemovedFromGlobalGroup()
        {
            usersRemovedFromGlobalGroup.removeAll(usersAddedToGlobalGroup);
            return usersRemovedFromGlobalGroup;
        }

        public Map<String, Person> getCurrentUsersOfGroup(String groupCode)
        {
            Map<String, Person> result = new TreeMap<>();
            AuthorizationGroup group = groupsByCode.get(groupCode);
            if (group != null)
            {
                group.getUsers().forEach(user -> result.put(user.getUserId(), user));
            }
            return result;
        }

        public AuthorizationGroup getGlobalGroup()
        {
            return globalGroup;
        }

        public boolean userExists(String userId)
        {
            return newUsers.contains(userId) || usersById.containsKey(userId);
        }

        public Space getSpace(String spaceCode)
        {
            return spacesByCode.get(spaceCode);
        }

        public int getNumberOfSpacesStartingWith(String userSpaceCode)
        {
            Predicate<String> predicate = code -> code.startsWith(userSpaceCode);
            return spacesByCode.keySet().stream().filter(predicate).collect(Collectors.counting()).intValue();
        }

        public Person getUser(String userId)
        {
            return usersById.get(userId);
        }

        public boolean groupExists(String groupCode)
        {
            boolean groupExists = groupsByCode.containsKey(groupCode);
            String adminGroupCode = createAdminGroupCode(groupCode);
            boolean adminGroupExists = groupsByCode.containsKey(adminGroupCode);
            if (groupExists)
            {
                if (adminGroupExists == false)
                {
                    throw new IllegalArgumentException("Group " + groupCode + " exists but not " + adminGroupCode);
                }
                return true;
            }
            if (adminGroupExists)
            {
                throw new IllegalArgumentException("Group " + groupCode + " does not exist but " + adminGroupCode);
            }
            return false;
        }

        public void addNewUser(String userId)
        {
            newUsers.add(userId);
        }
    }

    private SpacePermId createSpace(Context context, String spaceCode)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(spaceCode);
        context.add(spaceCreation);
        SpacePermId spaceId = new SpacePermId(spaceCode);
        context.getReport().addSpace(spaceId);
        return spaceId;
    }

    private String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
    }

    public static String createAdminGroupCode(String groupCode)
    {
        return groupCode + ADMIN_POSTFIX;
    }

    private Map<ISpaceId, Space> getSpaces(String sessionToken, Collection<String> spaceCodes)
    {
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        return service.getSpaces(sessionToken, spaceCodes.stream().map(SpacePermId::new).collect(Collectors.toList()), fetchOptions);
    }

    private Set<String> asSet(List<String> users)
    {
        return users == null ? Collections.emptySet() : new TreeSet<>(users);
    }

    private static class UserInfo
    {
        private Principal principal;

        private Map<String, GroupInfo> groupInfosByGroupKey = new TreeMap<>();

        public UserInfo(Principal principal)
        {
            this.principal = principal;
        }

        public void addGroupInfo(GroupInfo groupInfo)
        {
            groupInfosByGroupKey.put(groupInfo.getKey(), groupInfo);
        }

        public Map<String, GroupInfo> getGroupInfosByGroupKey()
        {
            return groupInfosByGroupKey;
        }

        @Override
        public String toString()
        {
            return principal.getUserId() + " " + groupInfosByGroupKey.values();
        }
    }

    private static class GroupInfo
    {
        private String key;

        private boolean admin;

        GroupInfo(String key, boolean admin)
        {
            this.key = key;
            this.admin = admin;
        }

        public String getKey()
        {
            return key;
        }

        public boolean isAdmin()
        {
            return admin;
        }

        @Override
        public String toString()
        {
            return key + (admin ? "*" : "");
        }
    }

    private static final class MappingAttributes
    {
        private String groupCode;

        private List<String> shareIds;

        public MappingAttributes(String groupCode, List<String> shareIds)
        {
            this.groupCode = groupCode;
            this.shareIds = shareIds;
        }

        public String getGroupCode()
        {
            return groupCode;
        }

        public List<String> getShareIds()
        {
            return shareIds;
        }
    }

    private static final class HomeSpaceRequest
    {
        private SpacePermId homeSpace;

        public SpacePermId getHomeSpace()
        {
            return homeSpace;
        }

        public void setHomeSpace(SpacePermId homeSpace)
        {
            if (this.homeSpace == null)
            {
                this.homeSpace = homeSpace;
            }
        }
    }

    private static final class Context
    {
        private String sessionToken;

        private Map<String, PersonCreation> personCreations = new LinkedMap<>();

        private Map<IPersonId, PersonUpdate> personUpdates = new LinkedMap<>();

        private List<SpaceCreation> spaceCreations = new ArrayList<>();

        private List<ProjectCreation> projectCreations = new ArrayList<>();

        private List<SampleCreation> sampleCreations = new ArrayList<>();

        private List<ExperimentCreation> experimentCreations = new ArrayList<>();

        private List<AuthorizationGroupCreation> groupCreations = new ArrayList<>();

        private List<AuthorizationGroupUpdate> groupUpdates = new ArrayList<>();

        private List<RoleAssignmentCreation> roleCreations = new ArrayList<>();

        private List<IRoleAssignmentId> roleDeletions = new ArrayList<>();

        private IApplicationServerInternalApi service;

        private CurrentState currentState;

        private UserManagerReport report;

        Context(String sessionToken, IApplicationServerInternalApi service, CurrentState currentState, UserManagerReport report)
        {
            this.sessionToken = sessionToken;
            this.service = service;
            this.currentState = currentState;
            this.report = report;
        }

        public String getSessionToken()
        {
            return sessionToken;
        }

        public CurrentState getCurrentState()
        {
            return currentState;
        }

        public UserManagerReport getReport()
        {
            return report;
        }

        public void add(PersonCreation personCreation)
        {
            personCreations.put(personCreation.getUserId(), personCreation);
        }

        public void add(PersonUpdate personUpdate)
        {
            personUpdates.put(personUpdate.getUserId(), personUpdate);
        }

        public void add(SpaceCreation spaceCreation)
        {
            spaceCreations.add(spaceCreation);
        }

        public void add(ProjectCreation projectCreation)
        {
            projectCreations.add(projectCreation);
        }

        public void add(SampleCreation sampleCreation)
        {
            sampleCreations.add(sampleCreation);
        }

        public void add(ExperimentCreation experimentCreation)
        {
            experimentCreations.add(experimentCreation);
        }

        public void add(AuthorizationGroupCreation creation)
        {
            groupCreations.add(creation);
        }

        public void add(RoleAssignmentCreation roleCreation)
        {
            roleCreations.add(roleCreation);
        }

        public void add(AuthorizationGroupUpdate groupUpdate)
        {
            groupUpdates.add(groupUpdate);
        }

        public void delete(RoleAssignment roleAssignment)
        {
            roleDeletions.add(roleAssignment.getId());
        }

        public void executeOperations()
        {
            List<IOperation> operations = new ArrayList<>();
            if (personCreations.isEmpty() == false)
            {
                operations.add(new CreatePersonsOperation(new ArrayList<>(personCreations.values())));
            }
            if (personUpdates.isEmpty() == false)
            {
                operations.add(new UpdatePersonsOperation(new ArrayList<>(personUpdates.values())));
            }
            if (spaceCreations.isEmpty() == false)
            {
                operations.add(new CreateSpacesOperation(spaceCreations));
            }
            if (projectCreations.isEmpty() == false)
            {
                operations.add(new CreateProjectsOperation(projectCreations));
            }
            if (sampleCreations.isEmpty() == false)
            {
                operations.add(new CreateSamplesOperation(sampleCreations));
            }
            if (experimentCreations.isEmpty() == false)
            {
                operations.add(new CreateExperimentsOperation(experimentCreations));
            }
            if (groupCreations.isEmpty() == false)
            {
                operations.add(new CreateAuthorizationGroupsOperation(groupCreations));
            }
            if (groupUpdates.isEmpty() == false)
            {
                operations.add(new UpdateAuthorizationGroupsOperation(groupUpdates));
            }
            if (roleCreations.isEmpty() == false)
            {
                // Filter out already existing roles to not repeat creations
                // This is to manage a corner case when a user role has been added manually unnecessarily.
                List<RoleAssignmentCreation> filteredRoleCreations = new ArrayList<>();
                for (RoleAssignmentCreation roleAssignmentCreationToCheck : roleCreations)
                {
                    IPersonId userId = roleAssignmentCreationToCheck.getUserId();
                    IAuthorizationGroupId groupId = roleAssignmentCreationToCheck.getAuthorizationGroupId();
                    ISpaceId spaceId = roleAssignmentCreationToCheck.getSpaceId();
                    RoleAssignmentSearchCriteria roleAssignmentSearchCriteria =
                            new RoleAssignmentSearchCriteria();
                    if (userId != null)
                    {
                        roleAssignmentSearchCriteria.withUser().withId()
                                .thatEquals(userId);
                    }
                    if (groupId != null)
                    {
                        roleAssignmentSearchCriteria.withAuthorizationGroup().withId()
                                .thatEquals(groupId);
                    }
                    if (spaceId != null)
                    {
                        roleAssignmentSearchCriteria.withSpace().withId()
                                .thatEquals(spaceId);
                    }

                    SearchResult<RoleAssignment> roleAssignmentSearchResult =
                            service.searchRoleAssignments(sessionToken,
                                    roleAssignmentSearchCriteria, new RoleAssignmentFetchOptions());

                    boolean found = false;
                    if (!roleAssignmentSearchResult.getObjects().isEmpty())
                    {
                        Role role = roleAssignmentCreationToCheck.getRole();
                        for (RoleAssignment roleAssignment : roleAssignmentSearchResult.getObjects())
                        {
                            if (roleAssignment.getRole().equals(role))
                            {
                                found = true;
                            }
                        }
                    }
                    if (!found)
                    {
                        filteredRoleCreations.add(roleAssignmentCreationToCheck);
                    }
                }
                roleCreations = filteredRoleCreations;
                operations.add(new CreateRoleAssignmentsOperation(roleCreations));
            }
            if (roleDeletions.isEmpty() == false)
            {
                RoleAssignmentDeletionOptions options = new RoleAssignmentDeletionOptions();
                options.setReason("Users removed from a group");
                operations.add(new DeleteRoleAssignmentsOperation(roleDeletions, options));
            }
            if (operations.isEmpty())
            {
                return;
            }
            SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
            service.executeOperations(sessionToken, operations, options);
        }
    }
}
