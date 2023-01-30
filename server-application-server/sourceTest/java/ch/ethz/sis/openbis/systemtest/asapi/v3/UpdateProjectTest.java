/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingFlags;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class UpdateProjectTest extends AbstractTest
{
    private static final String PREFIX = "UPT-";

    @Test
    public void testUpdateWithProjectNull()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ProjectUpdate update = new ProjectUpdate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, "Project id cannot be null");
    }

    @Test
    public void testUpdateWithProjectUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, projectId);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setDescription("a new description");

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.updateProjects(sessionToken, Collections.singletonList(update));
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(projectId), new ProjectFetchOptions());
            Project project = map.get(projectId);

            Assert.assertEquals(project.getCode(), "TEST-PROJECT");
            Assert.assertEquals(project.getDescription(), null);

            v3api.updateProjects(sessionToken, Arrays.asList(update));

            map = v3api.getProjects(sessionToken, Arrays.asList(projectId), new ProjectFetchOptions());
            project = map.get(projectId);

            Assert.assertEquals(project.getCode(), "TEST-PROJECT");
            Assert.assertEquals(project.getDescription(), update.getDescription().getValue());
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.updateProjects(sessionToken, Collections.singletonList(update));
                    }
                }, projectId);
        }
    }

    @Test
    public void testUpdateWithProjectNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectPermId("IDONTEXIST");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, projectId);
    }

    @Test
    public void testUpdateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ISpaceId spaceId = new SpacePermId("CISD");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceNull()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, "Space id cannot be null");
    }

    @Test
    public void testUpdateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateProjects(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceWhenSamplesExist()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IProjectId projectId = new ProjectPermId("20120814110011738-105");
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withSpace();
        projectFetchOptions.withExperiments().withSamples().withSpace();

        Map<IProjectId, Project> projectMap = v3api.getProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);

        Set<String> beforeExperimentPermIds = new HashSet<String>();
        for (Project project : projectMap.values())
        {
            Assert.assertEquals(project.getSpace().getCode(), "TEST-SPACE");

            for (Experiment experiment : project.getExperiments())
            {
                beforeExperimentPermIds.add(experiment.getPermId().getPermId());

                for (Sample sample : experiment.getSamples())
                {
                    Assert.assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
                }
            }
        }
        assertTrue(beforeExperimentPermIds.size() > 0);

        final ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setSpaceId(new SpacePermId("CISD"));

        v3api.updateProjects(sessionToken, Arrays.asList(update));

        projectMap = v3api.getProjects(sessionToken, Arrays.asList(projectId), projectFetchOptions);

        Set<String> afterExperimentPermIds = new HashSet<String>();
        for (Project project : projectMap.values())
        {
            Assert.assertEquals(project.getSpace().getCode(), "CISD");

            for (Experiment experiment : project.getExperiments())
            {
                afterExperimentPermIds.add(experiment.getPermId().getPermId());

                for (Sample sample : experiment.getSamples())
                {
                    Assert.assertEquals(sample.getSpace().getCode(), "CISD");
                }
            }
        }
        assertTrue(afterExperimentPermIds.size() > 0);
        assertEquals(beforeExperimentPermIds, afterExperimentPermIds);
        assertExperimentsExists(beforeExperimentPermIds.toArray(new String[] {}));
    }

    @Test
    public void testUpdateWithAttachments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("PROJECT_ATTACHMENTS_TEST");
        projectCreation.setSpaceId(new SpacePermId("CISD"));

        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withAttachments().withContent();

        List<ProjectPermId> projectPermIds = v3api.createProjects(sessionToken, Arrays.asList(projectCreation));

        Map<IProjectId, Project> projectMap = v3api.getProjects(sessionToken, projectPermIds, projectFetchOptions);
        Project project = projectMap.values().iterator().next();

        assertAttachments(project.getAttachments());

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(projectPermIds.get(0));
        projectUpdate.getAttachments().add(attachmentCreation);

        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));

        projectMap = v3api.getProjects(sessionToken, projectPermIds, projectFetchOptions);
        project = projectMap.values().iterator().next();

        assertAttachments(project.getAttachments(), attachmentCreation);
    }

    @Test
    public void testUpdateWithDescription()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IProjectId projectId1 = new ProjectIdentifier("/CISD/NEMO");
        final IProjectId projectId2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(projectId1, projectId2), new ProjectFetchOptions());

        Project project1 = map.get(projectId1);
        Project project2 = map.get(projectId2);

        Assert.assertEquals(project1.getCode(), "NEMO");
        Assert.assertEquals(project1.getDescription(), "nemo description");
        Assert.assertEquals(project2.getCode(), "TEST-PROJECT");
        Assert.assertNull(project2.getDescription());

        ProjectUpdate update1 = new ProjectUpdate();
        update1.setProjectId(projectId1);
        update1.setDescription("a new description 1");

        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProjectId(projectId2);
        update2.setDescription("a new description 2");

        v3api.updateProjects(sessionToken, Arrays.asList(update1, update2));

        map = v3api.getProjects(sessionToken, Arrays.asList(projectId1, projectId2), new ProjectFetchOptions());
        project1 = map.get(projectId1);
        project2 = map.get(projectId2);

        Assert.assertEquals(project1.getCode(), "NEMO");
        Assert.assertEquals(project1.getDescription(), update1.getDescription().getValue());
        Assert.assertEquals(project2.getCode(), "TEST-PROJECT");
        Assert.assertEquals(project2.getDescription(), update2.getDescription().getValue());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));

        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProjectId(new ProjectPermId("20120814110011738-101"));

        v3api.updateProjects(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-projects  PROJECT_UPDATES('[ProjectUpdate[projectId=/TEST-SPACE/TEST-PROJECT], ProjectUpdate[projectId=20120814110011738-101]]')");
    }

    @Test
    public void testFreezeForExperiments()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final IProjectId projectId1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectUpdate update1 = new ProjectUpdate();
        update1.setProjectId(projectId1);
        update1.freeze();
        final IProjectId projectId2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProjectId(projectId2);
        update2.freezeForExperiments();

        // When
        v3api.updateProjects(sessionToken, Arrays.asList(update1, update2));

        // Then
        Map<IProjectId, Project> projects = v3api.getProjects(sessionToken, Arrays.asList(projectId1, projectId2), new ProjectFetchOptions());
        Project project1 = projects.get(projectId1);
        assertEquals(project1.getIdentifier().getIdentifier(), projectId1.toString());
        assertEquals(project1.isFrozen(), true);
        assertEquals(project1.isFrozenForExperiments(), false);
        assertEquals(project1.isFrozenForSamples(), false);
        assertFreezingEvent(TEST_USER, project1.getIdentifier().getIdentifier(), EntityType.PROJECT, new FreezingFlags().freeze());
        Project project2 = projects.get(projectId2);
        assertEquals(project2.getIdentifier().getIdentifier(), projectId2.toString());
        assertEquals(project2.isFrozen(), true);
        assertEquals(project2.isFrozenForExperiments(), true);
        assertEquals(project2.isFrozenForSamples(), false);
        assertFreezingEvent(TEST_USER, project2.getIdentifier().getIdentifier(), EntityType.PROJECT,
                new FreezingFlags().freeze().freezeForExperiments());
    }

    @Test
    public void testFreezeForSamples()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final IProjectId projectId1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectUpdate update1 = new ProjectUpdate();
        update1.setProjectId(projectId1);
        update1.freeze();
        final IProjectId projectId2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProjectId(projectId2);
        update2.freezeForSamples();

        // When
        v3api.updateProjects(sessionToken, Arrays.asList(update1, update2));

        // Then
        Map<IProjectId, Project> projects = v3api.getProjects(sessionToken, Arrays.asList(projectId1, projectId2), new ProjectFetchOptions());
        Project project1 = projects.get(projectId1);
        assertEquals(project1.getIdentifier().getIdentifier(), projectId1.toString());
        assertEquals(project1.isFrozen(), true);
        assertEquals(project1.isFrozenForExperiments(), false);
        assertEquals(project1.isFrozenForSamples(), false);
        assertFreezingEvent(TEST_USER, project1.getIdentifier().getIdentifier(), EntityType.PROJECT, new FreezingFlags().freeze());
        Project project2 = projects.get(projectId2);
        assertEquals(project2.getIdentifier().getIdentifier(), projectId2.toString());
        assertEquals(project2.isFrozen(), true);
        assertEquals(project2.isFrozenForExperiments(), false);
        assertEquals(project2.isFrozenForSamples(), true);
        assertFreezingEvent(TEST_USER, project2.getIdentifier().getIdentifier(), EntityType.PROJECT,
                new FreezingFlags().freeze().freezeForSamples());
    }

    @Test
    public void testFreezing()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.freeze();
        v3api.updateProjects(sessionToken, Arrays.asList(update));
        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProjectId(projectId);
        update2.setDescription("new description");

        // When
        assertUserFailureException(Void -> v3api.updateProjects(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation UPDATE is not allowed because project NEMO is frozen.");
    }

    @Test
    public void testFreezingForExperimentCreations()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.freezeForExperiments();
        v3api.updateProjects(sessionToken, Arrays.asList(update));
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setProjectId(projectId);
        experimentCreation.setTypeId(new EntityTypePermId("DELETION_TEST", EntityKind.EXPERIMENT));
        experimentCreation.setCode(PREFIX + "E1");

        // When
        assertUserFailureException(Void -> v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation)),
                // Then
                "ERROR: Operation SET PROJECT is not allowed because project NEMO is frozen for experiment UPT-E1.");
    }

    @Test
    public void testFreezingForExperimentDeletions()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setProjectId(projectId);
        experimentCreation.setTypeId(new EntityTypePermId("DELETION_TEST", EntityKind.EXPERIMENT));
        experimentCreation.setCode(PREFIX + "E1");
        ExperimentPermId experimentPermId = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation)).get(0);
        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.freezeForExperiments();
        v3api.updateProjects(sessionToken, Arrays.asList(update));
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteExperiments(sessionToken, Arrays.asList(experimentPermId), deletionOptions),
                // Then
                "ERROR: Operation DELETE EXPERIMENT is not allowed because project NEMO is frozen.");
    }

    @Test(dataProvider = "freezeMethods")
    public void testUnauthorizedFreezing(MethodWrapper freezeMethod) throws Exception
    {
        // Given
        RoleAssignmentCreation roleAssignmentCreation = new RoleAssignmentCreation();
        roleAssignmentCreation.setRole(Role.ADMIN);
        roleAssignmentCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        roleAssignmentCreation.setUserId(new PersonPermId(TEST_POWER_USER_CISD));
        v3api.createRoleAssignments(systemSessionToken, Arrays.asList(roleAssignmentCreation));
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);
        IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        freezeMethod.method.invoke(update);

        // When
        assertAuthorizationFailureException(Void -> v3api.updateProjects(sessionToken, Arrays.asList(update)), null);
    }

    @DataProvider(name = "freezeMethods")
    public static Object[][] freezeMethods()
    {
        return asCartesianProduct(getFreezingMethods(ProjectUpdate.class));
    }

}
