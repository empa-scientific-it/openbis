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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.AutoResolveUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Shows the data as a table if one 'main data set' file exists, matches the selected dataset pattern and is a 'tsv' file.
 * 
 * @author Izabela Adamczyk
 */
public class TSVViewReportingPlugin extends AbstractFileTableReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final String TRANSPOSE_KEY = "transpose";

    private final boolean transpose;

    public TSVViewReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, TAB_SEPARATOR);
        this.transpose = PropertyUtils.getBoolean(properties, TRANSPOSE_KEY, false);
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        assureOnlyOneDataSetSelected(datasets);
        DatasetDescription dataset = datasets.get(0);
        IHierarchicalContent root =
                getDatasetDir(context.getHierarchicalContentProvider(), dataset);

        String pattern = properties.getProperty("data-set-regex");
        String path = properties.getProperty("data-set-path");

        if (pattern == null)
        {
            pattern = dataset.getMainDataSetPattern();
        }

        if (path == null)
        {
            path = dataset.getMainDataSetPath();
        }

        IHierarchicalContentNode fileToOpenOrNull = tryFindFileToOpen(pattern, path, root);

        if (fileToOpenOrNull != null && false == fileToOpenOrNull.isDirectory()
                && fileToOpenOrNull.exists())
        {
            DatasetFileLines lines = loadFromFile(dataset, fileToOpenOrNull.getFile());
            return transpose ? createTransposedTableModel(lines) : createTableModel(lines);
        }
        throw UserFailureException.fromTemplate("Main TSV file could not be found.");
    }

    private IHierarchicalContentNode tryFindFileToOpen(String pattern, String path,
            IHierarchicalContent root)
    {
        List<IHierarchicalContentNode> patternMatchinFiles =
                AutoResolveUtils.findSomeMatchingFiles(root, path, pattern);
        IHierarchicalContentNode mainDataSetFile = null;
        if (patternMatchinFiles.size() == 1)
        {
            mainDataSetFile = patternMatchinFiles.get(0);
        }
        return mainDataSetFile;
    }

    private void assureOnlyOneDataSetSelected(List<DatasetDescription> datasets)
    {
        if (datasets.size() != 1)
        {
            throw UserFailureException.fromTemplate(
                    "Chosen plugin works with exactly one data set. %s data sets selected.",
                    datasets.size());
        }
    }
}
