package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.common.test.AssertionUtil;
import junit.framework.Assert;

public class LoginTest extends AbstractTest
{

    @Test
    public void testLoginWithExistingUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Assert.assertNotNull(sessionToken);

        Map<IExperimentId, Experiment> experimentFromCisdSpace =
                v3api.getExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromCisdSpace.size());

        Map<IExperimentId, Experiment> experimentFromTestSpace =
                v3api.getExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("201206190940555-1032")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromTestSpace.size());
        v3api.logout(sessionToken);
    }

    @Test
    public void testLoginWithNotExistingUser()
    {
        assertUserFailureException(Void -> v3api.login(NOT_EXISTING_USER, PASSWORD),
                "User '" + NOT_EXISTING_USER + "' has no role assignments and thus is not permitted to login.");
    }

    @Test
    public void testLoginAsWithNotExistingUser()
    {
        assertUserFailureException(Void -> v3api.loginAs(NOT_EXISTING_USER, PASSWORD, TEST_USER),
                "User '" + NOT_EXISTING_USER + "' has no role assignments and thus is not permitted to login.");
    }

    @Test
    public void testLoginAsWithExistingUserAsNotExistingUser()
    {
        String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, NOT_EXISTING_USER);
        Assert.assertNull(sessionToken);
    }

    @Test
    public void testLoginAnonymousSucceeded()
    {
        String sessionToken = v3api.loginAsAnonymousUser();
        Assert.assertNotNull(sessionToken);
        AssertionUtil.assertContains("observer", sessionToken);

        SearchResult<Space> spaces = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
        ArrayList<String> codes = new ArrayList<String>();
        for (Space space : spaces.getObjects())
        {
            codes.add(space.getCode());
        }
        AssertionUtil.assertCollectionContainsOnly(codes, "TESTGROUP");
    }

    @Test
    public void testLoginAsWithInstanceAdminAsInstanceAdmin()
    {
        String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_USER);
        Assert.assertNotNull(sessionToken);

        Map<IExperimentId, Experiment> experimentFromCisdSpace =
                v3api.getExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromCisdSpace.size());

        Map<IExperimentId, Experiment> experimentFromTestSpace =
                v3api.getExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("201206190940555-1032")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromTestSpace.size());
        v3api.logout(sessionToken);
    }

    @Test
    public void testLoginAsWithInstanceAdminAsSpaceAdmin()
    {
        String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);
        Assert.assertNotNull(sessionToken);

        Map<IExperimentId, Experiment> experimentFromCisdSpace =
                v3api.getExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(0, experimentFromCisdSpace.size());

        Map<IExperimentId, Experiment> experimentFromTestSpace =
                v3api.getExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("201206190940555-1032")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromTestSpace.size());
        v3api.logout(sessionToken);
    }

    @Test
    public void testLoginAsWithSpaceAdminAsInstanceAdmin()
    {
        String sessionToken = v3api.loginAs(TEST_SPACE_USER, PASSWORD, TEST_USER);
        Assert.assertNull(sessionToken);
    }
}
