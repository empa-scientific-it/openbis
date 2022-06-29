package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

public class GetSessionInformationTest extends AbstractTest
{
    @SuppressWarnings("null")
    @Test
    public void testGet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SessionInformation sessionInformation = v3api.getSessionInformation(sessionToken);
        assertTrue(sessionInformation != null);
        assertTrue(sessionInformation.getCreatorPerson() != null);
        assertTrue(sessionInformation.getPerson() != null);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, tokenCreation());

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.getSessionInformation(token.getHash());
            }
        }, "Personal access tokens cannot be used to get session information");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                    @Override
                    public void execute()
                    {
                        v3api.getSessionInformation(sessionToken);
                    }
                });
        } else
        {
            SessionInformation sessionInformation = v3api.getSessionInformation(sessionToken);
            assertEquals(sessionInformation.getPerson().getUserId(), user.getUserId());
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        v3api.getSessionInformation(sessionToken);

        assertAccessLog("get-session-information");
    }

}
