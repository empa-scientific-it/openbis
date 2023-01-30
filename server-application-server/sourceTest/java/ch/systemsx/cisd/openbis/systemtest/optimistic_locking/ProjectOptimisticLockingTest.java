/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectOptimisticLockingTest extends OptimisticLockingTestCase
{
    @Test
    public void testCreateProject()
    {
        Project project = new Project();
        project.setCode("POLT-1");
        String identifier = "/" + toolBox.space1.getCode() + "/POLT-1";
        project.setIdentifier(identifier);
        project.setDescription("ProjectOptimisticLockingTest test");
        project.setSpace(toolBox.space1);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(2000);

        commonClientService.registerProject(SESSION_KEY, project);

        Project p =
                commonServer.getProjectInfo(systemSessionToken,
                        toolBox.createProjectIdentifier(identifier));
        assertEquals(project.getDescription(), p.getDescription());
        assertEquals("test", p.getRegistrator().getUserId());
        assertEquals("test", p.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(p.getRegistrationDate());
        timeIntervalChecker.assertDateInInterval(p.getModificationDate());
    }

    @Test
    public void testUpdateProjectAndCheckModificationDateAndModifier()
    {
        ProjectIdentifier projectIdentifier =
                toolBox.createProjectIdentifier(toolBox.project1.getIdentifier());
        Project p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        ProjectUpdates updates = new ProjectUpdates();
        updates.setVersion(p.getVersion());
        updates.setTechId(new TechId(p));
        updates.setDescription(p.getDescription() + " 2");
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setAttachmentSessionKey(SESSION_KEY);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        commonClientService.updateProject(updates);

        p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        assertEquals(toolBox.project1.getDescription() + " 2", p.getDescription());
        assertEquals("system", p.getRegistrator().getUserId());
        assertEquals("test", p.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(p.getModificationDate());
    }

    @Test
    public void testUpdateProjectWithOldVersion()
    {
        ProjectIdentifier projectIdentifier =
                toolBox.createProjectIdentifier(toolBox.project1.getIdentifier());
        Project currentProject = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        ProjectUpdates updates = new ProjectUpdates();
        updates.setVersion(currentProject.getVersion());
        updates.setTechId(new TechId(currentProject));
        updates.setDescription(currentProject.getDescription() + " 1");
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        updates.setAttachmentSessionKey(SESSION_KEY);
        logIntoCommonClientService();
        commonClientService.updateProject(updates);

        try
        {
            commonClientService.updateProject(updates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Project has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testRegisterExperimentAndCheckModificationDateAndModifierOfProject()
    {
        String sessionToken = logIntoCommonClientService().getSessionID();
        NewExperiment experiment =
                new NewExperiment(toolBox.project1.getIdentifier() + "/POLT-1",
                        ToolBox.EXPERIMENT_TYPE_CODE);
        experiment.setAttachments(Collections.<NewAttachment> emptyList());
        experiment.setProperties(new IEntityProperty[]
        { new PropertyBuilder("DESCRIPTION").value("hello").getProperty() });
        assertEquals("system", toolBox.project1.getModifier().getUserId());
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.registerExperiment(sessionToken, experiment,
                Collections.<NewAttachment> emptyList());

        toolBox.checkModifierAndModificationDateOfProject1(timeIntervalChecker);
    }

    @Test
    public void testRegisterExperiments()
    {
        assertEquals("system", toolBox.project1.getModifier().getUserId());
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(ToolBox.USER_ID);
        builder.experiment(toolBox.experiment(1)).experiment(toolBox.experiment(2));
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        List<Experiment> experiments =
                commonServer.listExperiments(systemSessionToken,
                        new ExperimentTypeBuilder().code(ToolBox.EXPERIMENT_TYPE_CODE)
                                .getExperimentType(), toolBox
                                .createProjectIdentifier(toolBox.project1.getIdentifier()));
        assertEquals("[OLT-E1, OLT-E2]", toolBox.extractCodes(experiments).toString());
        toolBox.checkModifierAndModificationDateOfProject1(timeIntervalChecker, ToolBox.USER_ID);
    }

    @Test
    public void testAddAttachment()
    {
        ProjectIdentifier projectIdentifier =
                toolBox.createProjectIdentifier(toolBox.project1.getIdentifier());
        Project p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        ProjectUpdatesDTO updates = new ProjectUpdatesDTO();
        updates.setVersion(p.getVersion());
        updates.setTechId(new TechId(p));
        updates.setDescription(p.getDescription());
        NewAttachment attachment = new NewAttachment();
        attachment.setFilePath("greetings.txt");
        attachment.setTitle("greetings");
        attachment.setContent("hello world".getBytes());
        updates.setAttachments(Arrays.asList(attachment));
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        commonServer.updateProject(sessionToken, updates);

        p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        assertEquals("system", p.getRegistrator().getUserId());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, p, "test");
        List<Attachment> attachments =
                commonServer.listProjectAttachments(systemSessionToken, new TechId(p));
        assertEquals("greetings.txt", attachments.get(0).getFileName());
        assertEquals(1, attachments.size());
    }
}
