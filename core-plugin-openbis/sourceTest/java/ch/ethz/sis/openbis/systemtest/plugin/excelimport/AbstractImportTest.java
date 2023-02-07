package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

public class AbstractImportTest extends AbstractTransactionalTestNGSpringContextTests
{

    private static final String VERSIONING_JSON = "./versioning.json";

    private static final String XLS_VERSIONING_DIR = "xls-import.version-data-file";

    protected static final String SYSTEM_USER = "system";

    protected static final String TEST_USER = "test";

    protected static final String PASSWORD = "password";

    @Autowired
    protected IApplicationServerInternalApi v3api;

    protected String sessionToken;

    protected String FILES_DIR;

    @BeforeSuite
    public void setupSuite()
    {
        System.setProperty(XLS_VERSIONING_DIR, VERSIONING_JSON);
        System.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY, "dist/core-plugins");
        System.setProperty(Constants.ENABLED_MODULES_KEY, "xls-import");
        TestInitializer.initEmptyDbNoIndex();
    }

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @AfterMethod
    public void afterTest()
    {
        File f = new File(VERSIONING_JSON);
        f.delete();
        v3api.logout(sessionToken);
    }

}
