/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AggregationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Pawel Glyzewski
 */
public class FeatureListsAggregationServicePlugin extends AggregationService
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public FeatureListsAggregationServicePlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, "feature_lists");
    }

    @Override
    public TableModel createAggregationReport(Map<String, Object> parameters,
            DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("name");
        builder.addHeader("value");

        Object datasetDescriptionObject = parameters.get("data-set");
        if (datasetDescriptionObject == null)
        {
            throw new IllegalArgumentException();
        } else if (false == datasetDescriptionObject instanceof DatasetDescription)
        {
            throw new IllegalArgumentException();
        }

        DatasetDescription datasetDescription = (DatasetDescription) datasetDescriptionObject;

        try
        {
            IHierarchicalContentNode subDirNode =
                    getDataSubDir(context.getHierarchicalContentProvider(), datasetDescription);

            IRowBuilder row = null;
            if (subDirNode.exists() && subDirNode.isDirectory())
            {
                for (IHierarchicalContentNode contentNode : subDirNode.getChildNodes())
                {
                    if (false == contentNode.isDirectory())
                    {
                        row = builder.addRow();
                        readSingleFeaturesList(row, contentNode);
                    }
                }
            }
        } catch (IllegalArgumentException e)
        {
            // sub directory doesn't exist
        }

        return builder.getTableModel();
    }

    private void readSingleFeaturesList(IRowBuilder row, IHierarchicalContentNode contentNode)
    {
        InputStream is = null;
        try
        {
            is = contentNode.getInputStream();
            String value = IOUtils.toString(is);
            row.setCell("name", contentNode.getName());
            row.setCell("value", value);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }
}
