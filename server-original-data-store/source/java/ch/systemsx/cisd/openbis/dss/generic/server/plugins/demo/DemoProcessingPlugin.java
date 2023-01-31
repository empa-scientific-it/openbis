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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentTraverseUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentNodeVisitor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin which can be used for demonstration purposes.
 * 
 * @author Tomasz Pylak
 */
public class DemoProcessingPlugin implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DemoProcessingPlugin.class);

    public DemoProcessingPlugin(Properties properties, File storeRoot)
    {
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        operationLog.info("Processing of the following datasets has been requested: " + datasets);
        if (context != null)
        {
            Map<String, String> parameterBindings = context.getParameterBindings();
            if (parameterBindings != null && parameterBindings.isEmpty() == false)
            {
                for (Entry<String, String> entry : parameterBindings.entrySet())
                {
                    operationLog.info("Parameter: " + entry);
                }
            }
        }
        IHierarchicalContentProvider contentProvider =
                ServiceProvider.getHierarchicalContentProvider();

        for (DatasetDescription dataset : datasets)
        {
            String dataSetCode = dataset.getDataSetCode();
            IHierarchicalContentNodeVisitor printingVisitor = createPrintingVisitor(dataSetCode);
            HierarchicalContentTraverseUtil.traverse(contentProvider, dataSetCode, printingVisitor);
        }
        operationLog.info("Processing done.");
        return null;
    }

    private IHierarchicalContentNodeVisitor createPrintingVisitor(final String datasetCode)
    {
        return new IHierarchicalContentNodeVisitor()
            {
                @Override
                public void visit(IHierarchicalContentNode node)
                {
                    String relativePath = node.getRelativePath();
                    String fullPath = datasetCode + "/" + relativePath;
                    operationLog.info("Processing " + fullPath);
                }
            };
    }
}
