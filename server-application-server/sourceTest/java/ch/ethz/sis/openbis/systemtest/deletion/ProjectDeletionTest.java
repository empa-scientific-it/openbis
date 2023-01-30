package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class ProjectDeletionTest extends DeletionTest
{
    @Test
    @Rollback(false)
    public void deleteAttachment() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT3");

        AttachmentCreation attachment1 = new AttachmentCreation();
        attachment1.setTitle("A1");
        attachment1.setDescription("hello A");
        attachment1.setFileName("hello.txt");
        attachment1.setContent("hello world!".getBytes());
        AttachmentCreation attachment2 = new AttachmentCreation();
        attachment2.setFileName("hi.txt");
        attachment2.setContent("hi world!".getBytes());
        addAttachment(project, attachment1, attachment2);

        newTx();

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.getAttachments().remove(new AttachmentFileName("hello.txt"));
        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));

        assertAttachment("project/" + project.getPermId() + "/hello.txt(1)",
                set("OWNED = ATTACHMENT:" + project + "[PROJECT](user:test) <hello world!>"));

        newTx();

        delete(project);
        delete(space);
    }

    @Test
    @Rollback(false)
    public void deleteProjectWithAttachments() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT3");

        AttachmentCreation attachment1 = new AttachmentCreation();
        attachment1.setTitle("A1");
        attachment1.setDescription("hello A");
        attachment1.setFileName("hello.txt");
        attachment1.setContent("hello world!".getBytes());
        AttachmentCreation attachment2 = new AttachmentCreation();
        attachment2.setFileName("hi.txt");
        attachment2.setContent("hi world!".getBytes());
        addAttachment(project, attachment1, attachment2);

        newTx();

        delete(project);
        delete(space);

        assertAttachment("project/" + project.getPermId() + "/hello.txt(1)",
                set("OWNED = ATTACHMENT:" + project + "[PROJECT](user:test) <hello world!>"));
        assertAttachment("project/" + project.getPermId() + "/hi.txt(1)",
                set("OWNED = ATTACHMENT:" + project + "[PROJECT](user:test) <hi world!>"));
        assertHistory(project.getPermId(), "OWNER", attachmentSet("project/" + project.getPermId() + "/hello.txt(1)",
                "project/" + project.getPermId() + "/hi.txt(1)"));
    }

    @Test
    @Rollback(false)
    public void testAttributes() throws Exception
    {
        Date after = new Date();
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT3");

        newTx();
        Date before = new Date();

        delete(project);
        delete(space);

        HashMap<String, String> expectations = new HashMap<String, String>();
        expectations.put("CODE", "PROJECT3");
        expectations.put("DESCRIPTION", "description /SPACE/PROJECT3");
        expectations.put("REGISTRATOR", "test");
        assertAttributes(project.getPermId(), expectations);
        assertRegistrationTimestampAttribute(project.getPermId(), after, before);
    }

    @Test
    @Rollback(false)
    public void moveProjectToAnotherSpace() throws Exception
    {
        SpacePermId space1 = createSpace("SPACE1");
        SpacePermId space2 = createSpace("SPACE2");

        ProjectPermId project = createProject(space1, "PROJECT");

        newTx();
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.setSpaceId(space2);
        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));

        delete(project);
        delete(space1);
        delete(space2);

        assertHistory(project.getPermId(), "OWNED", spaceSet("SPACE1"), spaceSet("SPACE2"));
    }

    @Test
    @Rollback(false)
    public void assignExperimentsToProject() throws Exception
    {
        SpacePermId space = createSpace("SPACE");

        ProjectPermId project1 = createProject(space, "PROJECT1");
        ProjectPermId project2 = createProject(space, "PROJECT2");

        ExperimentPermId experiment1 = createExperiment(project1, "EXPERIMENT1");
        ExperimentPermId experiment2 = createExperiment(project1, "EXPERIMENT2");

        newTx();
        ExperimentUpdate experimentUpdate1 = new ExperimentUpdate();
        experimentUpdate1.setExperimentId(experiment1);
        experimentUpdate1.setProjectId(project2);
        ExperimentUpdate experimentUpdate2 = new ExperimentUpdate();
        experimentUpdate2.setExperimentId(experiment2);
        experimentUpdate2.setProjectId(project2);

        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate1, experimentUpdate2));

        delete(project1);
        delete(experiment1);
        delete(experiment2);
        delete(project2);
        delete(space);

        assertHistory(project1.getPermId(), "OWNER",
                experimentSet(experiment1.getPermId(), experiment2.getPermId()), set());
    }
}
