/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetProjectTest extends AbstractTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId1 = new ProjectPermId("20120814110011738-103");
        ProjectPermId permId2 = new ProjectPermId("20120814110011738-105");

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(permId1, permId2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectIdentifier identifier1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(identifier1, identifier2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdentifierCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectIdentifier identifier1 = new ProjectIdentifier("/cisD/NeMo");

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(identifier1), new ProjectFetchOptions());

        assertEquals(1, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);

        assertEquals(map.get(identifier1).getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(map.get(new ProjectIdentifier("/CISD/NEMO")).getIdentifier().getIdentifier(), "/CISD/NEMO");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId1 = new ProjectPermId("IDONTEXIST");
        ProjectIdentifier identifier1 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectPermId permId2 = new ProjectPermId("20120814110011738-103");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/IDONT/EXIST");

        Map<IProjectId, Project> map =
                v3api.getProjects(sessionToken, Arrays.asList(permId1, identifier1, permId2, identifier2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId1 = new ProjectPermId("20120814110011738-103");
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/NEMO");
        ProjectPermId permId2 = new ProjectPermId("20120814110011738-103");

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(permId1, identifier, permId2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier).getIdentifier(), identifier);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        ProjectPermId permId = new ProjectPermId("20120814110011738-101");
        ProjectIdentifier identifier1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        List<? extends IProjectId> ids = Arrays.asList(permId, identifier1, identifier2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, ids, new ProjectFetchOptions());

        assertEquals(map.size(), 3);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.getProjects(sessionToken, ids, new ProjectFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/NEMO");
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(identifier), fetchOptions);

        Project project = map.get(identifier);

        assertEquals(project.getPermId(), new ProjectPermId("20120814110011738-103"));
        assertEquals(project.getIdentifier(), identifier);
        assertEquals(project.getCode(), "NEMO");
        assertEquals(project.getDescription(), "nemo description");
        assertEqualsDate(project.getRegistrationDate(), "2008-11-05 09:21:43");
        assertEqualsDate(project.getModificationDate(), "2009-04-03 15:56:37");

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testGetByIdsWithExperiments()
    {
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/DEFAULT");

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withExperiments();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(identifier), fetchOptions);

        Project project = map.get(identifier);
        List<Experiment> experiments = project.getExperiments();

        assertExperimentIdentifiers(experiments, "/CISD/DEFAULT/EXP-REUSE", "/CISD/DEFAULT/EXP-WELLS", "/CISD/DEFAULT/EXP-Y");

        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testGetByIdsWithSpace()
    {
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/DEFAULT");

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(identifier), fetchOptions);

        Project project = map.get(identifier);

        Assert.assertEquals(project.getSpace().getCode(), "CISD");

        assertExperimentsNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testGetByIdsWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        ProjectCreation creation = new ProjectCreation();
        creation.setCode("TEST_REGISTRATOR");
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Arrays.asList(creation));

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withRegistrator();

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, permIds, fetchOptions);
        Project project = map.values().iterator().next();

        Assert.assertEquals(project.getRegistrator().getUserId(), TEST_SPACE_USER);

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testGetByIdsWithModifier()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        ProjectIdentifier projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withModifier();

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        Project project = map.get(projectId);

        Assert.assertEquals(project.getModifier().getUserId(), TEST_USER);

        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);

        v3api.updateProjects(sessionToken, Arrays.asList(update));

        map = v3api.getProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        project = map.get(projectId);

        Assert.assertEquals(project.getModifier().getUserId(), TEST_SPACE_USER);

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testGetByIdsWithLeader()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        ProjectIdentifier projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withLeader();

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        Project project = map.get(projectId);

        assertEquals(project.getLeader().getUserId(), SYSTEM_USER);

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testGetWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation1 = new AttachmentCreation();
        attachmentCreation1.setFileName("test.txt");
        attachmentCreation1.setDescription("test description 1");
        attachmentCreation1.setTitle("test title 1");
        attachmentCreation1.setContent("test content 1".getBytes());

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("PROJECT_WITH_ATTACHMENT");
        projectCreation.setSpaceId(new SpacePermId("CISD"));
        projectCreation.setAttachments(Arrays.asList(attachmentCreation1));

        List<ProjectPermId> projectPermIds = v3api.createProjects(sessionToken, Arrays.asList(projectCreation));

        AttachmentCreation attachmentCreation2 = new AttachmentCreation();
        attachmentCreation2.setFileName("test.txt");
        attachmentCreation2.setDescription("test description 2");
        attachmentCreation2.setTitle("test title 2");
        attachmentCreation2.setContent("test content 2".getBytes());

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(projectPermIds.get(0));
        projectUpdate.getAttachments().add(attachmentCreation2);

        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withRegistrator();
        fetchOptions.withAttachments().withPreviousVersion().withContent();

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, projectPermIds, fetchOptions);

        assertEquals(1, map.size());
        Project project = map.get(projectPermIds.get(0));

        List<Attachment> attachments = project.getAttachments();
        assertEquals(attachments.size(), 1);

        Attachment attachment = attachments.get(0);
        assertEquals(attachment.getFileName(), attachmentCreation2.getFileName());
        assertEquals(attachment.getDescription(), attachmentCreation2.getDescription());
        assertEquals(attachment.getTitle(), attachmentCreation2.getTitle());
        assertEquals(attachment.getContent(), attachmentCreation2.getContent());
        assertEquals(attachment.getVersion(), Integer.valueOf(2));
        assertEquals(attachment.getRegistrator().getUserId(), TEST_USER);
        assertToday(attachment.getRegistrationDate());
        assertEquals(attachment.getLatestVersionPermlink(),
                "http://localhost/openbis/index.html?viewMode=SIMPLE#action=DOWNLOAD_ATTACHMENT&file=test.txt&entity=PROJECT&code=PROJECT_WITH_ATTACHMENT&space=CISD");
        assertEquals(
                attachment.getPermlink(),
                "http://localhost/openbis/index.html?viewMode=SIMPLE#action=DOWNLOAD_ATTACHMENT&file=test.txt&version=2&entity=PROJECT&code=PROJECT_WITH_ATTACHMENT&space=CISD");

        Attachment attachmentPrevious = attachment.getPreviousVersion();
        assertEquals(attachmentPrevious.getFileName(), attachmentCreation1.getFileName());
        assertEquals(attachmentPrevious.getDescription(), attachmentCreation1.getDescription());
        assertEquals(attachmentPrevious.getTitle(), attachmentCreation1.getTitle());
        assertEquals(attachmentPrevious.getContent(), attachmentCreation1.getContent());
        assertEquals(attachmentPrevious.getVersion(), Integer.valueOf(1));
        assertToday(attachmentPrevious.getRegistrationDate());
        assertEquals(attachmentPrevious.getLatestVersionPermlink(),
                "http://localhost/openbis/index.html?viewMode=SIMPLE#action=DOWNLOAD_ATTACHMENT&file=test.txt&entity=PROJECT&code=PROJECT_WITH_ATTACHMENT&space=CISD");
        assertEquals(
                attachmentPrevious.getPermlink(),
                "http://localhost/openbis/index.html?viewMode=SIMPLE#action=DOWNLOAD_ATTACHMENT&file=test.txt&version=1&entity=PROJECT&code=PROJECT_WITH_ATTACHMENT&space=CISD");

        assertRegistratorNotFetched(attachmentPrevious);
        assertPreviousAttachmentNotFetched(attachmentPrevious);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithHistoryNoChanges()
    {
        ProjectCreation creation = new ProjectCreation();
        creation.setCode("PROJECT_WITH_EMPTY_HISTORY");
        creation.setSpaceId(new SpacePermId("CISD"));

        Project project = testGetWithHistory(creation, null);
        List<HistoryEntry> history = project.getHistory();

        assertEquals(history.size(), 1);

        assertRelationshipHistory(history.get(0), new SpacePermId("CISD"), ProjectRelationType.SPACE, project.getRegistrationDate(), null);
    }

    @Test
    public void testGetWithHistorySpace()
    {
        ProjectCreation creation = new ProjectCreation();
        creation.setCode("PROJECT_WITH_SPACE_HISTORY");
        creation.setSpaceId(new SpacePermId("CISD"));

        ProjectUpdate update = new ProjectUpdate();
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        Project project = testGetWithHistory(creation, update);
        List<HistoryEntry> history = project.getHistory();

        assertEquals(history.size(), 2);

        assertRelationshipHistory(history.get(0), new SpacePermId("CISD"), ProjectRelationType.SPACE, project.getRegistrationDate(),
                project.getModificationDate());
        assertRelationshipHistory(history.get(1), new SpacePermId("TEST-SPACE"), ProjectRelationType.SPACE, project.getModificationDate(), null);
    }

    @Test
    public void testGetWithHistoryExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("PROJECT_WITH_EXPERIMENT_HISTORY");
        projectCreation.setSpaceId(new SpacePermId("CISD"));

        ProjectCreation projectCreation2 = new ProjectCreation();
        projectCreation2.setCode("PROJECT_WITH_EXPERIMENT_HISTORY_2");
        projectCreation2.setSpaceId(new SpacePermId("CISD"));

        List<ProjectPermId> projectPermIds = v3api.createProjects(sessionToken, Arrays.asList(projectCreation, projectCreation2));

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setCode("EXPERIMENT_WITH_PROJECT_HISTORY");
        experimentCreation.setProperty("DESCRIPTION", "description");
        experimentCreation.setProjectId(projectPermIds.get(0));

        List<ExperimentPermId> experimentPermIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));

        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experimentPermIds.get(0));
        experimentUpdate.setProjectId(new ProjectIdentifier("/CISD/PROJECT_WITH_EXPERIMENT_HISTORY_2"));

        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate));

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withHistory();

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, projectPermIds, fetchOptions);
        assertEquals(map.size(), 2);

        Project project = map.get(projectPermIds.get(0));

        List<HistoryEntry> history = project.getHistory();
        assertEquals(history.size(), 2);

        assertRelationshipHistory(history.get(0), experimentPermIds.get(0), ProjectRelationType.EXPERIMENT);
        assertRelationshipHistory(history.get(1), new SpacePermId("CISD"), ProjectRelationType.SPACE, project.getRegistrationDate(), null);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        ProjectIdentifier identifier1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        List<? extends IProjectId> ids = Arrays.asList(identifier1, identifier2);

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.getProjects(sessionToken, ids, projectFetchOptionsFull());
                }
            });
        } else
        {
            Map<IProjectId, Project> map = v3api.getProjects(sessionToken, ids, projectFetchOptionsFull());

            if (user.isInstanceUser())
            {
                assertEquals(map.size(), 2);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEquals(map.size(), 1);
                assertEquals(map.get(identifier2).getIdentifier(), identifier2);
            } else
            {
                assertEquals(map.size(), 0);
            }
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();
        fo.withExperiments();

        v3api.getProjects(sessionToken, Arrays.asList(new ProjectIdentifier("/CISD/NEMO"), new ProjectPermId("20120814110011738-105")), fo);

        assertAccessLog(
                "get-projects  PROJECT_IDS('[/CISD/NEMO, 20120814110011738-105]') FETCH_OPTIONS('Project\n    with Experiments\n    with Space\n')");
    }

    private Project testGetWithHistory(ProjectCreation creation, ProjectUpdate update)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Arrays.asList(creation));

        if (update != null)
        {
            update.setProjectId(permIds.get(0));
            v3api.updateProjects(sessionToken, Arrays.asList(update));
        }

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withHistory();

        Map<IProjectId, Project> map = v3api.getProjects(sessionToken, permIds, fetchOptions);

        assertEquals(map.size(), 1);
        Project project = map.get(permIds.get(0));

        v3api.logout(sessionToken);

        return project;
    }

}
