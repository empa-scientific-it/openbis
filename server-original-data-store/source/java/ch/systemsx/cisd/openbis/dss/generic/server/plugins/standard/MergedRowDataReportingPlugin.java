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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Reporting plugin that concatenates rows of tabular files of all data sets (stripping the header lines of all but the first file) and delivers the
 * result back in the table model. Each row has additional Data Set code column.
 * 
 * @author Piotr Buczek
 */
public class MergedRowDataReportingPlugin extends AbstractDataMergingReportingPlugin
{

    private static final long serialVersionUID = 1L;

    public MergedRowDataReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets, DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("Data Set Code");
        if (datasets.isEmpty() == false)
        {
            IDataSetDirectoryProvider directoryProvider = context.getDirectoryProvider();
            final DatasetDescription firstDataset = datasets.get(0);
            final String[] titles = getHeaderTitles(firstDataset, directoryProvider);
            for (String title : titles)
            {
                builder.addHeader(title);
            }
            for (DatasetDescription dataset : datasets)
            {
                final File dir = getDataSubDir(directoryProvider, dataset);
                final DatasetFileLines lines = loadFromDirectory(dataset, dir);
                if (Arrays.equals(titles, lines.getHeaderLabels()) == false)
                {
                    throw UserFailureException.fromTemplate(
                            "All Data Set files should have the same headers, "
                                    + "but file header of '%s': \n\t '%s' "
                                    + "is different than file header of '%s': \n\t '%s'.",
                            firstDataset.getDataSetCode(), StringUtils.join(titles, "\t"), dataset
                                    .getDataSetCode(), StringUtils.join(lines.getHeaderLabels(),
                                    "\t"));
                }
                addDataRows(builder, dataset, lines, false);
            }
        }

        return builder.getTableModel();
    }

}
