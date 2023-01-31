/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.server.IServletPropertiesManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo.DemoProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo.DemoReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SimpleFileContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Tests for {@link PluginTaskInfoProvider} class.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ PluginTaskInfoProvider.class, PluginTaskFactory.class })
public class PluginTaskParametersTest extends AbstractFileSystemTestCase
{
    private static final File STORE_ROOT = new File("../datastore_server/resource/test-data/"
            + PluginTaskParametersTest.class.getSimpleName());

    private Mockery context;

    private IHierarchicalContentProvider contentProvider;

    private IServletPropertiesManager servletPropertiesManager;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        servletPropertiesManager = context.mock(IServletPropertiesManager.class);
        final File root = STORE_ROOT;
        contentProvider = new SimpleFileContentProvider(root);
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);

        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("hierarchical-content-provider");
                    will(returnValue(contentProvider));
                }
            });

    }

    @AfterMethod
    public void afterTest()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateReportingPluginsFactories() throws Exception
    {
        Properties props = new Properties();
        String plugin1 = "plugin1";
        String plugin2 = "plugin2";

        props.setProperty(Constants.REPORTING_PLUGIN_NAMES, plugin1 + ", " + plugin2);
        String pluginLabel1 = "Demo Reporting 1";
        String datasetCodes1 = "MZXML, EICML";
        setPluginProperties(props, plugin1, pluginLabel1, datasetCodes1, DemoReportingPlugin.class);

        final String pluginLabel2 = "Demo Reporting 2";
        String datasetCodes2 = "EICML";
        setPluginProperties(props, plugin2, pluginLabel2, datasetCodes2, DemoReportingPlugin.class);
        setPluginProperty(props, plugin2, PluginTaskFactory.SERVLETS_PROPERTY_NAME,
                "s1, s2");
        setPluginProperty(props, plugin2, "s1.a", "alpha");
        setPluginProperty(props, plugin2, "s2.b", "beta");
        final RecordingMatcher<SectionProperties[]> sectionPropertiesMatcher =
                new RecordingMatcher<SectionProperties[]>();
        context.checking(new Expectations()
            {
                {
                    one(servletPropertiesManager).addServletsProperties(with(pluginLabel2 + ", "),
                            with(sectionPropertiesMatcher));
                }
            });

        PluginTaskProvider<IReportingPluginTask> factories = createReportingPluginsFactories(props);

        factories.check(false);
        factories.logConfigurations();
        IReportingPluginTask pluginInstance1 = factories.getPluginInstance(plugin1);
        pluginInstance1.createReport(createDatasetDescriptions(), new DataSetProcessingContext(
                null, null, null, null, "test-user", null));

        SectionProperties[] sectionProperties = sectionPropertiesMatcher.recordedObject();
        Arrays.sort(sectionProperties, new Comparator<SectionProperties>()
            {
                @Override
                public int compare(SectionProperties sp1, SectionProperties sp2)
                {
                    return sp1.getKey().compareTo(sp2.getKey());
                }
            });
        assertEquals("s1", sectionProperties[0].getKey());
        assertEquals("{a=alpha}", sectionProperties[0].getProperties().toString());
        assertEquals("s2", sectionProperties[1].getKey());
        assertEquals("{b=beta}", sectionProperties[1].getProperties().toString());
        assertEquals(2, sectionProperties.length);
        List<DatastoreServiceDescription> descriptions = factories.getPluginDescriptions();
        assertEquals(2, descriptions.size());
        for (DatastoreServiceDescription desc : descriptions)
        {
            String key = desc.getKey();
            if (key.equals(plugin2))
            {
                assertEquals(pluginLabel2, desc.getLabel());
                assertEquals(1, desc.getDatasetTypeCodes().length);
                assertEquals(datasetCodes2, desc.getDatasetTypeCodes()[0]);
            } else
            {
                assertEquals(plugin1, key);
            }
        }
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testMissingPluginSpecFails() throws Exception
    {
        Properties props = new Properties();
        props.setProperty(Constants.REPORTING_PLUGIN_NAMES, "plugin1");
        createReportingPluginsFactories(props);
    }

    private PluginTaskProvider<IReportingPluginTask> createReportingPluginsFactories(
            Properties props)
    {
        return PluginTaskInfoProvider.createReportingPluginsFactories(props,
                servletPropertiesManager, "dss", STORE_ROOT);
    }

    private PluginTaskProvider<IProcessingPluginTask> createProcessingPluginsFactories(
            Properties props)
    {
        return PluginTaskInfoProvider.createProcessingPluginsFactories(props,
                servletPropertiesManager, "dss", STORE_ROOT);
    }

    @Test
    public void testUnspecifiedPluginsParameters() throws Exception
    {
        Properties props = new Properties();
        PluginTaskProvider<IProcessingPluginTask> processing =
                createProcessingPluginsFactories(props);
        assertEquals(0, processing.getPluginDescriptions().size());

        PluginTaskProvider<IProcessingPluginTask> reporting =
                createProcessingPluginsFactories(props);
        assertEquals(0, reporting.getPluginDescriptions().size());

    }

    @Test
    public void testCreateProcessingPluginsFactories() throws Exception
    {
        Properties props = new Properties();
        String plugin1 = "plugin1";

        props.setProperty(Constants.PROCESSING_PLUGIN_NAMES, plugin1);
        setPluginProperties(props, plugin1, "pluginLabel1", "datasetCodes1",
                DemoProcessingPlugin.class);
        setPluginProperty(props, plugin1, PluginTaskFactory.SERVLET_PROPERTY_NAME + ".a",
                "alpha");
        context.checking(new Expectations()
            {
                {
                    Properties properties = new Properties();
                    properties.setProperty("a", "alpha");
                    one(servletPropertiesManager).addServletProperties("pluginLabel1", properties);
                }
            });

        PluginTaskProvider<IProcessingPluginTask> factories =
                createProcessingPluginsFactories(props);

        factories.check(true);
        factories.logConfigurations();
        IProcessingPluginTask pluginInstance1 = factories.getPluginInstance(plugin1);
        pluginInstance1.process(createDatasetDescriptions(), null);
        context.assertIsSatisfied();
    }

    private static List<DatasetDescription> createDatasetDescriptions()
    {
        DatasetDescription description = new DatasetDescription();
        final String dataSetCode = "dataset-1";
        description.setDataSetCode(dataSetCode);
        description.setDataSetLocation(dataSetCode);
        description.setSampleCode("sampleCode");
        description.setSpaceCode("groupCode");
        description.setProjectCode("projCode");
        description.setExperimentCode("expCode");
        return Arrays.asList(description);
    }

    private void setPluginProperties(Properties props, String pluginId, String pluginLabel,
            String datasetCodes, Class<?> pluginClass) throws IOException, FileNotFoundException
    {
        setPluginProperty(props, pluginId, PluginTaskFactory.LABEL_PROPERTY_NAME,
                pluginLabel);
        setPluginProperty(props, pluginId, PluginTaskFactory.DATASET_CODES_PROPERTY_NAME,
                datasetCodes);
        setPluginProperty(props, pluginId, PluginTaskFactory.CLASS_PROPERTY_NAME,
                pluginClass.getName());
        String[] pluginParams = new String[]
        { "param1 = X", "param2 = Y" };
        File paramsFile = createPluginPropertiesFile(pluginParams);
        setPluginProperty(props, pluginId,
                PluginTaskFactory.PARAMS_FILE_PATH_PROPERTY_NAME, paramsFile.getPath());
    }

    private File createPluginPropertiesFile(String... lines) throws IOException,
            FileNotFoundException
    {
        File paramsFile = new File(workingDirectory, "pluginParams.properties");
        IOUtils.writeLines(Arrays.asList(lines), "\n", new OutputStreamWriter(new FileOutputStream(
                paramsFile)));
        return paramsFile;
    }

    private static void setPluginProperty(Properties props, String pluginKey, String propertyName,
            String value)
    {
        props.setProperty(pluginKey + "." + propertyName, value);

    }
}
