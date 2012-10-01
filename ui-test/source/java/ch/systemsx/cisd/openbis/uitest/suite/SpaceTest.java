package ch.systemsx.cisd.openbis.uitest.suite;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.Space;

@Test(groups =
    { "login-admin" })
public class SpaceTest extends SeleniumTest
{

    @Test
    public void newSpaceIsListedInSpaceBrowser() throws Exception
    {
        Space space = create(aSpace());

        assertThat(browserEntryOf(space), exists());
    }

    @Test
    public void deletedSpaceIsRemovedFromSpaceBrowser() throws Exception
    {
        Space space = create(aSpace());

        delete(space);

        assertThat(browserEntryOf(space), doesNotExist());
    }
}
