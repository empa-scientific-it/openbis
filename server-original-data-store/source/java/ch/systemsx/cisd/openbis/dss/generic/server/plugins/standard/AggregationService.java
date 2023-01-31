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

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
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
public abstract class AggregationService extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    protected AggregationService(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null);
    }

    protected AggregationService(Properties properties, File storeRoot, String subDirectory)
    {
        super(properties, storeRoot, subDirectory);
    }

    @Override
    public ReportingPluginType getReportingPluginType()
    {
        return ReportingPluginType.AGGREGATION_TABLE_MODEL;
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        throw createException();
    }

    @Override
    public LinkModel createLink(DatasetDescription dataset)
    {
        throw createException();
    }

    private IllegalArgumentException createException()
    {
        return new IllegalArgumentException("The method createReport is not supported by "
                + getReportingPluginType() + " tasks");
    }

    protected void logInvocationError(Map<String, Object> parameters, Throwable e)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Error producing aggregation report\n");
        sb.append("Class: ");
        sb.append(getClass().getName());
        sb.append("\n");
        sb.append("Parameters: ");
        sb.append(parameters.keySet());

        operationLog.error(sb.toString(), e);
    }

    protected TableModel errorTableModel(Map<String, Object> parameters, Throwable e)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("Parameters");
        builder.addHeader("Error");
        IRowBuilder row = builder.addRow();
        row.setCell("Parameters", parameters.toString());
        String message = e.getMessage();
        row.setCell("Error", StringUtils.isBlank(message) ? e.toString() : message);
        return builder.getTableModel();
    }

}
