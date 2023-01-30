/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.AsCorePluginPaths;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.IMasterDataScriptRegistrationRunner;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationException;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataTransactionErrors;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.ICorePluginResourceLoader;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Kaloyan Enimanev
 */
public class CorePluginTableTest extends AbstractBOTest
{

    private IMasterDataScriptRegistrationRunner scriptRunner;

    private ICorePluginResourceLoader pluginResourceLoader;

    private RecordingMatcher<List<CorePluginPE>> createdPluginsMatcher;

    private CorePluginTable pluginTable;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder();
        scriptRunner = context.mock(IMasterDataScriptRegistrationRunner.class);
        pluginResourceLoader = context.mock(ICorePluginResourceLoader.class);
        pluginTable =
                new CorePluginTable(daoFactory, EXAMPLE_SESSION, scriptRunner,
                        managedPropertyEvaluatorFactory, null, null);
        createdPluginsMatcher = new RecordingMatcher<List<CorePluginPE>>();
    }

    @Test
    public void testPluginAlreadyDeployed()
    {
        CorePlugin plugin = new CorePlugin("A", 2);
        prepareVersionAvailable("A", 1, 2);
        // plugin already deployed, nothing will happen
        pluginTable.registerPlugin(plugin, pluginResourceLoader);

    }

    @Test
    public void testNewVersionDetectedNoMasterDataScript()
    {
        final CorePlugin plugin = new CorePlugin("A", 2);
        prepareVersionAvailable("A", 1);
        context.checking(new Expectations()
            {
                {
                    one(pluginResourceLoader).tryLoadToString(plugin,
                            AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
                    will(returnValue(null));

                    one(pluginResourceLoader).getPath(plugin,
                            AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
                    will(returnValue(null));
                }
            });

        pluginTable.registerPlugin(plugin, pluginResourceLoader);

        assertEquals("No 'initialize-master-data.py' script found for "
                + "'Core Plugin[name='A', version='2']'. Skipping..", logRecorder.getLogContent());
    }

    @Test
    public void testNewVersionDetectedWithMasterDataScript()
    {
        final CorePlugin plugin = new CorePlugin("A", 2);
        final String mockScriptValue = "mockScriptValue";
        prepareFirstTimeDetected(plugin.getName());
        context.checking(new Expectations()
            {
                {
                    one(pluginResourceLoader).tryLoadToString(plugin,
                            AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
                    will(returnValue(mockScriptValue));

                    one(pluginResourceLoader).getPath(plugin,
                            AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
                    will(returnValue("/test-dir/mockScript.py"));

                    one(scriptRunner).executeScript(mockScriptValue, new String[] { "/test-dir" });
                    one(corePluginDAO).createCorePlugins(with(createdPluginsMatcher));
                }
            });

        pluginTable.registerPlugin(plugin, pluginResourceLoader);

        assertEquals(1, createdPluginsMatcher.getRecordedObjects().size());
        assertEquals(1, createdPluginsMatcher.recordedObject().size());
        CorePluginPE createdPluginPE = createdPluginsMatcher.recordedObject().get(0);
        assertEquals(plugin.getName(), createdPluginPE.getName());
        assertEquals(plugin.getVersion(), createdPluginPE.getVersion());
        assertEquals(mockScriptValue, createdPluginPE.getMasterDataRegistrationScript());
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testExceptionsPropagated()
    {
        final CorePlugin plugin = new CorePlugin("A", 5);
        prepareFirstTimeDetected(plugin.getName());
        context.checking(new Expectations()
            {
                {
                    final String mockScriptValue = "mockScriptValue";

                    one(pluginResourceLoader).tryLoadToString(plugin,
                            AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
                    will(returnValue(mockScriptValue));

                    one(pluginResourceLoader).getPath(plugin,
                            AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
                    will(returnValue("/test-dir/mockScript.py"));

                    one(scriptRunner).executeScript(mockScriptValue, new String[] { "/test-dir" });
                    will(throwException(new MasterDataRegistrationException(null, Collections
                            .<MasterDataTransactionErrors> emptyList())));
                }
            });
        pluginTable.registerPlugin(plugin, pluginResourceLoader);
    }

    private void prepareFirstTimeDetected(final String pluginName)
    {
        prepareVersionAvailable(pluginName, new int[0]);
    }

    private void prepareVersionAvailable(final String pluginName, final int... versions)
    {
        context.checking(new Expectations()
            {
                {
                    ArrayList<CorePluginPE> existingVersions = new ArrayList<CorePluginPE>();
                    for (int i = 0; i < versions.length; i++)
                    {
                        CorePluginPE pluginPE = new CorePluginPE();
                        pluginPE.setName(pluginName);
                        pluginPE.setVersion(versions[i]);
                        existingVersions.add(pluginPE);
                    }

                    one(corePluginDAO).listCorePluginsByName(pluginName);
                    will(returnValue(existingVersions));
                }
            });
    }

}
