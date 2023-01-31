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
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.csvreader.CsvWriter;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Abstract superclass for all reporting plugins that are of type TABLE_MODEL.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractTableModelReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{

    private static final long serialVersionUID = 1L;

    /**
     * Inherited constructor.
     * 
     * @param properties
     * @param storeRoot
     */
    protected AbstractTableModelReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public ReportingPluginType getReportingPluginType()
    {
        return ReportingPluginType.TABLE_MODEL;
    }

    @Override
    public LinkModel createLink(DatasetDescription dataset)
    {
        throw new IllegalArgumentException(
                "The method createLink is not supported by TABLE_MODEL tasks");
    }

    @Override
    public TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context)
    {
        throw new IllegalArgumentException(
                "The method createAggregationReport is not supported by TABLE_MODEL tasks");
    }

    public static String convertTableToCsvString(TableModel table) throws IOException
    {
        StringWriter writer = new StringWriter();
        CsvWriter csvWriter = new CsvWriter(writer, ',');
        List<TableModelColumnHeader> headers = table.getHeader();
        String[] stringArray = new String[headers.size()];
        for (int i = 0; i < stringArray.length; i++)
        {
            stringArray[i] = headers.get(i).getTitle();
        }
        csvWriter.writeRecord(stringArray);
        List<TableModelRow> rows = table.getRows();
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            for (int i = 0; i < stringArray.length; i++)
            {
                stringArray[i] = values.get(i).toString();
            }
            csvWriter.writeRecord(stringArray);
        }
        return writer.toString();
    }

}
