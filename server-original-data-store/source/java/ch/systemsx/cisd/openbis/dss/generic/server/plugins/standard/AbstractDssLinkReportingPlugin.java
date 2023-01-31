/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DssLinkTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * The abstract superclass for plug-ins of type DSS_LINK.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDssLinkReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final String DATA_SET_HEADER = "Data Set";

    private static final long serialVersionUID = 1L;

    protected AbstractDssLinkReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public ReportingPluginType getReportingPluginType()
    {
        return ReportingPluginType.DSS_LINK;
    }

    /**
     * Generate a report using containing links to each of the data sets.
     */
    @Override
    public TableModel createReport(List<DatasetDescription> datasets, DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(DATA_SET_HEADER);
        for (DatasetDescription dataSet : datasets)
        {
            IRowBuilder rowBuilder = builder.addRow();
            LinkModel linkModel = createLink(dataSet);
            String text = dataSet.getDataSetCode();

            rowBuilder.setCell(DATA_SET_HEADER, new DssLinkTableCell(text, linkModel));
        }
        return builder.getTableModel();
    }

    @Override
    public TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context)
    {
        throw new IllegalArgumentException(
                "The method createAggregationReport is not supported by DSS_LINK tasks");
    }
}
