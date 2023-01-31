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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IterativeTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin that concatenates column tabular files of all data sets, using a merge column to identify matching rows. Per data set there must
 * be one file to consider for merging, which can be defined by include and exclude patterns.
 * <p>
 * Properties:
 * <ul>
 * <li><code>row-id-column-header</code> - The header of the merge column (default: <code>id</code>)</li>
 * <li><code>file-include-pattern</code> - The regular expression pattern to use for defining what files to consider (default: <i>not defined</i>)</li>
 * <li><code>file-exclude-pattern</code> - The regular expression pattern to use for defining what files <i>not</i> to consider (default:
 * <code>.*\.tsv</code>)</li>
 * <li><code>sub-directory-name</code> - The name of the sub directory in the data set to start the search for an appropriate file (default:
 * <code>original</code>)</li>
 * </ul>
 * 
 * @author Bernd Rinn
 */
public class MergedColumnDataReportingPlugin extends AbstractDataMergingReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final String ROW_ID_COLUMN_HEADER = "row-id-column-header";

    private final String rowIdentifierColumnHeader;

    public MergedColumnDataReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        final String rowIdColumnHeaderOrNull = properties.getProperty(ROW_ID_COLUMN_HEADER);
        rowIdentifierColumnHeader =
                (rowIdColumnHeaderOrNull == null) ? "id" : rowIdColumnHeaderOrNull;
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        final IterativeTableModelBuilder builder =
                new IterativeTableModelBuilder(rowIdentifierColumnHeader);
        for (DatasetDescription dataset : datasets)
        {
            File dir = getDataSubDir(context.getDirectoryProvider(), dataset);
            final DatasetFileLines lines = loadFromDirectory(dataset, dir);
            builder.addFile(lines);
        }
        return builder.getTableModel();
    }

}
