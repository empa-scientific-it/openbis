package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;

public class ExperimentTypeTest extends MainSuite
{

    @Test
    public void newExperimentTypeIsListedInExperimentTypeBrowser() throws Exception
    {
        ExperimentType type = create(anExperimentType());

        assertThat(browserEntryOf(type), exists());
    }

}
