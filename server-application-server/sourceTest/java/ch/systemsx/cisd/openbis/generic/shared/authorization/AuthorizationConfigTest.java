package ch.systemsx.cisd.openbis.generic.shared.authorization;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AuthorizationConfigTest extends Assert
{

    private Mockery context;

    private IAuthorizationConfigProperties properties;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        properties = context.mock(IAuthorizationConfigProperties.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testWithAuthorizationEnabledPropertySetToTrue()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue("true"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelEnabled(true);
    }

    @Test
    public void testWithAuthorizationEnabledPropertySetToFalse()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue("false"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelEnabled(false);
    }

    @Test
    public void testWithAuthorizationEnabledPropertySetToNonBooleanValue()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue("thisiswrong"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelEnabled(false);
    }

    @Test
    public void testWithAuthorizationUsersPropertySetToMatchAllExpression()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue(".*"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelUser("user1", true);
        assertProjectLevelUser("user2", true);
    }

    @Test
    public void testWithAuthorizationUsersPropertySetToMatchSomeExpression()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue("project\\_level\\_.*"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelUser("project_level_admin", true);
        assertProjectLevelUser("project_level_observer", true);
        assertProjectLevelUser("other_user", false);
    }

    @Test
    public void testWithAuthorizationUsersPropertySetToSpecificUser()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue("user1"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelUser("user1", true);
        assertProjectLevelUser("user2", false);
    }

    @Test
    public void testWithAuthorizationUsersPropertySetToMultipleUsers()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_USERS_PROPERTY_NAME);
                    will(returnValue("user1|user3"));

                    allowing(properties).getProperty(AuthorizationConfig.PROJECT_LEVEL_AUTHORIZATION_ENABLED_PROPERTY_NAME);
                    will(returnValue(null));
                }
            });

        assertProjectLevelUser("user1", true);
        assertProjectLevelUser("user2", false);
        assertProjectLevelUser("user3", true);
    }

    private void assertProjectLevelEnabled(boolean expected)
    {
        AuthorizationConfig config = new AuthorizationConfig();
        config.setProperties(properties);
        config.init();
        assertEquals(config.isProjectLevelEnabled(), expected);
    }

    private void assertProjectLevelUser(String user, boolean expected)
    {
        AuthorizationConfig config = new AuthorizationConfig();
        config.setProperties(properties);
        config.init();
        assertEquals(config.isProjectLevelUser(user), expected);
    }

}
