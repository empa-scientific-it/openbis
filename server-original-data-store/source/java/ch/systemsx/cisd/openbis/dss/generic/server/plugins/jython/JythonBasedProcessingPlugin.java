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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin which delegates the processing to a Jython script.
 * 
 * @author Piotr Buczek
 */
public class JythonBasedProcessingPlugin implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonBasedProcessingPlugin.class);

    private static final String SCRIPT_PATH = "script-path";

    private final IPluginScriptRunnerFactory scriptRunnerFactory;

    transient IHierarchicalContentProvider hierarchicalContentProvider;

    public static String getScriptPathProperty(Properties properties)
    {
        return PropertyUtils.getMandatoryProperty(properties, SCRIPT_PATH);
    }

    public JythonBasedProcessingPlugin(Properties properties, File storeRoot)
    {
        this(new PluginScriptRunnerFactory(getScriptPathProperty(properties)), null);
    }

    // for tests
    protected JythonBasedProcessingPlugin(IPluginScriptRunnerFactory scriptRunnerFactory,
            IHierarchicalContentProvider contentProvider)
    {
        this.scriptRunnerFactory = scriptRunnerFactory;
        this.hierarchicalContentProvider = contentProvider;
    }

    private IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null)
        {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> dataSets,
            DataSetProcessingContext context)
    {
        operationLog.info("Processing of the following datasets has been requested: " + dataSets);
        final IProcessingPluginScriptRunner scriptRunner =
                scriptRunnerFactory.createProcessingPluginRunner(context);
        final IHierarchicalContentProvider contentProvider = getHierarchicalContentProvider();
        final List<IDataSet> iDataSets = JythonBasedPluginUtils.convert(dataSets, contentProvider);
        try
        {
            ProcessingStatus result = new ProcessingStatus();
            for (IDataSet dataSet : iDataSets)
            {
                result.addDatasetStatus(dataSet.getDataSetCode(),
                        delegateProcessing(scriptRunner, dataSet));
            }
            operationLog.info("Processing done.");
            return result;
        } finally
        {
            JythonBasedPluginUtils.closeContent(iDataSets);
            scriptRunner.releaseResources();
        }
    }

    private Status delegateProcessing(IProcessingPluginScriptRunner scriptRunner, IDataSet dataSet)
    {
        return scriptRunner.process(dataSet);
    }

}
