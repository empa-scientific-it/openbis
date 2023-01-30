package ch.systemsx.cisd.openbis.generic.server.authorization.project;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.IObject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.Object;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.role.IRole;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.IObjectsProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.IRolesProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.IUserProvider;

public class ProjectAuthorizationTest extends Assert
{

    private static final String USER_ID = "testUser";

    private static final String PROJECT_PROVIDER = "projectProvider";

    private static final String ORIGINAL_OBJECT_A = "A";

    private static final String ORIGINAL_OBJECT_B = "B";

    private static final IProject PROJECT_X = new TestProject(1L, "permIdX", "identifierX");

    private static final IProject PROJECT_Y = new TestProject(2L, "permIdY", "identifierY");

    private static final IProject PROJECT_OTHER = new TestProject(3L, "permIdOther", "identifierOther");

    private static final IRole ROLE_WITHOUT_PROJECT = new TestRole(null);

    private static final IRole ROLE_WITH_PROJECT_X = new TestRole(PROJECT_X);

    private static final IRole ROLE_WITH_PROJECT_Y = new TestRole(PROJECT_Y);

    private static final IRole ROLE_WITH_OTHER_PROJECT = new TestRole(PROJECT_OTHER);

    private Mockery context;

    private IAuthorizationDataProvider dataProvider;

    private IObjectsProvider<String> objectsProvider;

    private IUserProvider userProvider;

    private IRolesProvider rolesProvider;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dataProvider = context.mock(IAuthorizationDataProvider.class);
        objectsProvider = context.mock(IObjectsProvider.class);
        userProvider = context.mock(IUserProvider.class);
        rolesProvider = context.mock(IRolesProvider.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testDisabledAtGlobalAndUserLevels()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(objectsProvider).getOriginalObjects();
                    will(returnValue(Arrays.asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledAtGlobalLevelOnly()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, false)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(objectsProvider).getOriginalObjects();
                    will(returnValue(Arrays.asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledAtGlobalAndUserLevels()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> object = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(object)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X)));
                }
            });

        assertResults(Arrays.<String> asList(ORIGINAL_OBJECT_A), Arrays.<String> asList());
    }

    @Test
    public void testEnabledAtUserLevelOnly()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, true)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(objectsProvider).getOriginalObjects();
                    will(returnValue(Arrays.asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledWithNullObjects()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(null));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X, ROLE_WITH_PROJECT_Y)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList());
    }

    @Test
    public void testEnabledWithEmptyObjects()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList()));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X, ROLE_WITH_PROJECT_Y)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList());
    }

    @Test
    public void testEnabledWithNullRoles()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(null));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledWithEmptyRoles()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList()));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledWithObjectWithoutProject()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectWithoutProject = new Object<String>(ORIGINAL_OBJECT_A, null);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectWithoutProject)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X, ROLE_WITH_PROJECT_Y)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A));
    }

    @Test(dataProvider = PROJECT_PROVIDER)
    public void testEnabledWithObjectWithProject(final IProject objectProject, final IProject roleProject, boolean matching)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectWithProject = new Object<String>(ORIGINAL_OBJECT_A, objectProject);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectWithProject)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(new TestRole(roleProject))));
                }
            });

        if (matching)
        {
            assertResults(Arrays.<String> asList(ORIGINAL_OBJECT_A), Arrays.<String> asList());
        } else
        {
            assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A));
        }
    }

    @Test
    public void testEnabledWithObjectWithOtherProject()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectWithOtherProject = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_OTHER);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectWithOtherProject)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X, ROLE_WITH_PROJECT_Y)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A));
    }

    @Test
    public void testEnabledWithRoleWithoutProject()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITHOUT_PROJECT)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledWithRoleWithOtherProject()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_OTHER_PROJECT)));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledWithAccessToAllObjects()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X, ROLE_WITH_PROJECT_Y)));
                }
            });

        assertResults(Arrays.<String> asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B), Arrays.<String> asList());
    }

    @Test
    public void testEnabledWithAccessToSomeObjects()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList(ROLE_WITH_PROJECT_X)));
                }
            });

        assertResults(Arrays.<String> asList(ORIGINAL_OBJECT_A), Arrays.<String> asList(ORIGINAL_OBJECT_B));
    }

    @Test
    public void testEnabledWithAccessToZeroObjects()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(true, true)));

                    IObject<String> objectA = new Object<String>(ORIGINAL_OBJECT_A, PROJECT_X);
                    IObject<String> objectB = new Object<String>(ORIGINAL_OBJECT_B, PROJECT_Y);

                    allowing(objectsProvider).getObjects(dataProvider);
                    will(returnValue(Arrays.asList(objectA, objectB)));

                    allowing(userProvider).getUserId();
                    will(returnValue(USER_ID));

                    allowing(rolesProvider).getRoles(dataProvider);
                    will(returnValue(Arrays.asList()));
                }
            });

        assertResults(Arrays.<String> asList(), Arrays.<String> asList(ORIGINAL_OBJECT_A, ORIGINAL_OBJECT_B));
    }

    private void assertResults(List<String> expectedWithAccess, List<String> expectedWithoutAccess)
    {
        IProjectAuthorization<String> pa = new ProjectAuthorizationBuilder<String>()
                .withData(dataProvider)
                .withObjects(objectsProvider)
                .withUser(userProvider)
                .withRoles(rolesProvider).build();

        Collection<String> withAccess = pa.getObjectsWithAccess();
        assertEquals(withAccess, expectedWithAccess);

        Collection<String> withoutAccess = pa.getObjectsWithoutAccess();
        assertEquals(withoutAccess, expectedWithoutAccess);
    }

    @DataProvider(name = PROJECT_PROVIDER)
    protected java.lang.Object[][] provideProjects()
    {
        return new java.lang.Object[][] {
                // ALL nulls
                { new TestProject(null, null, null), new TestProject(null, null, null), false },
                // ALL same
                { new TestProject(1L, "p", "i"), new TestProject(1L, "p", "i"), true },
                // ALL different
                { new TestProject(1L, "p1", "i1"), new TestProject(2L, "p2", "i2"), false },
                // ID same
                { new TestProject(1L, null, null), new TestProject(1L, null, null), true },
                // ID different
                { new TestProject(1L, null, null), new TestProject(2L, null, null), false },
                // PERM_ID same
                { new TestProject(null, "p", null), new TestProject(null, "p", null), true },
                // PERM_ID different
                { new TestProject(null, "p1", null), new TestProject(null, "p2", null), false },
                // IDENTIFIER same
                { new TestProject(null, null, "i"), new TestProject(null, null, "i"), true },
                // IDENTIFIER different
                { new TestProject(null, null, "i1"), new TestProject(null, null, "i2"), false },

                // ID same, PERM_ID same
                { new TestProject(1L, "p", null), new TestProject(1L, "p", null), true },
                // ID same, PERM_ID different - weird situation
                { new TestProject(1L, "p1", null), new TestProject(1L, "p2", null), false },
                // ID different, PERM_ID same - weird situation
                { new TestProject(1L, "p", null), new TestProject(2L, "p", null), false },

                // ID same, IDENTIFIER same
                { new TestProject(1L, null, "i"), new TestProject(1L, null, "i"), true },
                // ID same, IDENTIFIER different
                { new TestProject(1L, null, "i1"), new TestProject(1L, null, "i2"), true },
                // ID different, IDENTIFIER same
                { new TestProject(1L, null, "i"), new TestProject(2L, null, "i"), false },

                // PERM_ID same, IDENTIFIER same
                { new TestProject(null, "p", "i"), new TestProject(null, "p", "i"), true },
                // PERM_ID same, IDENTIFIER different
                { new TestProject(null, "p", "i1"), new TestProject(null, "p", "i2"), true },
                // PERM_ID different, IDENTIFIER same
                { new TestProject(null, "p1", "i"), new TestProject(null, "p2", "i"), false },
        };
    }

}
