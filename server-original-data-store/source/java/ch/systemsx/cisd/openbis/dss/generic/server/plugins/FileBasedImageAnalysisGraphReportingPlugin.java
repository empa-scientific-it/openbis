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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractTabularDataGraphServlet;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDataMergingReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GeneratedImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Reporting plugin that returns a table in which each column contains a graph. The number and format of the graphs can be configured in a properties
 * file.
 * <p>
 * This plugin reads data from a file. The ImageAnalysisGraphReportingPlugin (in the screening project) reads from the imaging db.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileBasedImageAnalysisGraphReportingPlugin extends AbstractDataMergingReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private final String graphServletPath;

    private final TabularDataGraphCollectionConfiguration configuration;

    // Keys for the properties
    private final String SERVLET_PATH_PROP = "servlet-path";

    private final static String PROPERTIES_FILE_KEY = "properties-file";

    public FileBasedImageAnalysisGraphReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, SEMICOLON_SEPARATOR);
        graphServletPath = properties.getProperty(SERVLET_PATH_PROP, "datastore_server_graph/");
        String propertiesFilePath = properties.getProperty(PROPERTIES_FILE_KEY);
        if (propertiesFilePath == null)
        {
            throw new EnvironmentFailureException(
                    "FileBasedImageAnalysisGraphReportingPlugin requires a properties file (specified with the "
                            + PROPERTIES_FILE_KEY + "key).");
        }

        configuration =
                TabularDataGraphCollectionConfiguration.getConfiguration(propertiesFilePath);
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        addHeaders(builder);
        if (datasets.isEmpty())
        {
            return builder.getTableModel();
        }
        for (DatasetDescription dataset : datasets)
        {
            final File dir = getDataSubDir(context.getDirectoryProvider(), dataset);
            List<File> matchingFiles = findMatchingFiles(dataset, dir);
            if (matchingFiles.size() != 1)
            {
                throw UserFailureException.fromTemplate(
                        "Expected exactly one matching file in the dataset %s but found %d", dataset
                                .getDataSetCode(), matchingFiles.size());
            }
            builder.addRow(createRow(dataset, matchingFiles.get(0)));
        }
        return builder.getTableModel();
    }

    /**
     * Add the headers to the table -- these depend on the configuration
     */
    private void addHeaders(SimpleTableModelBuilder builder)
    {
        builder.addHeader("Data Set Code");
        builder.addHeader("Sample Code");
        int width = getThumbnailWidth();
        for (String graphTypeCode : getGraphTypeCodes())
        {
            TabularDataGraphConfiguration graphConfig =
                    configuration.getGraphConfiguration(graphTypeCode);
            builder.addHeader(graphConfig.getTitle(), width);
        }
    }

    private List<ISerializableComparable> createRow(DatasetDescription dataset, File file)
    {
        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();

        // The data set and sample code
        row.add(SimpleTableModelBuilder.asText(dataset.getDataSetCode()));
        row.add(SimpleTableModelBuilder.asText(dataset.getSampleCode()));

        for (String graphTypeCode : getGraphTypeCodes())
        {

            GeneratedImageTableCell imageCell =
                    new GeneratedImageTableCell(graphServletPath, getImageWidth(),
                            getImageHeight(), getThumbnailWidth(), getThumbnailHeight());
            imageCell.addParameter(AbstractTabularDataGraphServlet.DATASET_CODE_PARAM,
                    dataset.getDataSetCode());
            imageCell.addParameter(AbstractTabularDataGraphServlet.FILE_PATH_PARAM,
                    file.getAbsolutePath());
            imageCell.addParameter(AbstractTabularDataGraphServlet.GRAPH_TYPE_CODE, graphTypeCode);

            row.add(imageCell);
        }

        return row;
    }

    private List<String> getGraphTypeCodes()
    {
        return configuration.getGraphNames();
    }

    private int getImageWidth()
    {
        return configuration.getImageWidth();
    }

    private int getImageHeight()
    {
        return configuration.getImageHeight();
    }

    private int getThumbnailWidth()
    {
        return configuration.getThumbnailWidth();
    }

    private int getThumbnailHeight()
    {
        return configuration.getThumbnailHeight();
    }
}
