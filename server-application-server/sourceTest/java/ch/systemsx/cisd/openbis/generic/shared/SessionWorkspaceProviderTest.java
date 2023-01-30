package ch.systemsx.cisd.openbis.generic.shared;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;

public class SessionWorkspaceProviderTest extends AbstractFileSystemTestCase
{

    private Mockery mockery;

    private IPersonalAccessTokenConverter converter;

    @BeforeMethod
    public void beforeMethod()
    {
        mockery = new Mockery();
        converter = mockery.mock(IPersonalAccessTokenConverter.class);

        mockery.checking(new Expectations()
        {
            {
                allowing(converter).convert(with(any(String.class)));
                will(new CustomAction("return unchanged")
                {
                    @Override public Object invoke(Invocation invocation) throws Throwable
                    {
                        return invocation.getParameter(0);
                    }
                });
            }
        });
    }

    @AfterMethod
    public void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testGetSessionWorkspace() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        SessionWorkspaceProvider provider = new SessionWorkspaceProvider(properties, converter);
        provider.init();

        Map<String, File> sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());

        File sessionWorkspace = provider.getSessionWorkspace("token");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token]", sessionWorkspaces.keySet().toString());

        assertEquals(true, sessionWorkspace.exists());
        assertEquals(workingDirectory, sessionWorkspace.getParentFile());
    }

    @Test
    public void testGetSessionWorkspaces() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        SessionWorkspaceProvider provider = new SessionWorkspaceProvider(properties, converter);
        provider.init();

        Map<String, File> sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());

        provider.getSessionWorkspace("token1");
        provider.getSessionWorkspace("token2");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token1, token2]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token1");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token2]", sessionWorkspaces.keySet().toString());
    }

    @Test
    public void testDeleteSessionWorkspace() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        SessionWorkspaceProvider provider = new SessionWorkspaceProvider(properties, converter);
        provider.init();

        File workspace1 = provider.getSessionWorkspace("token1");
        File workspace2 = provider.getSessionWorkspace("token2");

        FileUtils.writeStringToFile(new File(workspace1, "file1A"), "1A");
        FileUtils.writeStringToFile(new File(workspace1, "file1B"), "1B");
        FileUtils.writeStringToFile(new File(workspace2, "file2"), "2");

        Map<String, File> sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token1, token2]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token1");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token2]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token2");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token3");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());
    }

}
