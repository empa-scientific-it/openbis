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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.TableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which can be used for demonstration purposes. Shows last modification time of
 * main datasets directories.
 * 
 * @author Tomasz Pylak
 */
public class DatasetModificationReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    public DatasetModificationReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        TableModelBuilder builder = new TableModelBuilder();
        builder.addHeader("File", TableModelColumnType.TEXT);
        builder.addHeader("Modification date", TableModelColumnType.DATE);
        for (DatasetDescription dataset : datasets)
        {
            File file = getOriginalDir(dataset);
            String datasetCode = dataset.getDatasetCode();
            List<String> row = Arrays.asList(datasetCode, new Date(file.lastModified()).toString());
            builder.addRow(row);
        }
        return builder.getTableModel();
    }
}
