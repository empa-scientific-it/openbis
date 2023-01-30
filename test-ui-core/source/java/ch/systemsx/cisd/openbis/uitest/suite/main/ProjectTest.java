package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.Project;

public class ProjectTest extends MainSuite
{

    @Test
    public void newProjectIsListedInProjectBrowser() throws Exception
    {
        Project project = create(aProject());

        assertThat(browserEntryOf(project), exists());
    }

}
