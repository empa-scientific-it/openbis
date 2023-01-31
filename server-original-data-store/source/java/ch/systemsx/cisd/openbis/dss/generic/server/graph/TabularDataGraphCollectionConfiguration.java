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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphConfiguration.GraphType;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CsvFileReaderHelper.ICsvFileReaderConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphCollectionConfiguration implements ICsvFileReaderConfiguration
{
    private static final String CODE_POSTFIX = ".code";

    private static final String LABEL_POSTFIX = ".label";

    public static final String SEPARATOR_PROPERTY_KEY = "separator";

    private static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";

    // the full width of the graphs when requested
    private static final String IMAGE_WIDTH_KEY = "full-width";

    // the full height of the graphs when requested
    private static final String IMAGE_HEIGHT_KEY = "full-height";

    // the width of the thumbnails shown in the report
    private static final String THUMBNAIL_WIDTH_KEY = "column-width";

    // the height of the thumbnails shown in the report
    private static final String THUMBNAIL_HEIGHT_KEY = "column-height";

    // the graphs to display -- each one is shown in a column.
    public static final String GRAPHS_KEY = "graphs";

    // the type of graph. See @{link GraphType} for valid types.
    public static final String GRAPHS_TYPES_KEY = "graph-type";

    // keys for the different kinds of graphs
    public static final String TITLE_KEY = "title";

    public static final String X_AXIS_KEY = "x-axis";

    public static final String Y_AXIS_KEY = "y-axis";

    public static final String COLUMN_KEY = "column";

    public static final String NUMBER_OF_BINS_KEY = "number-of-bins";

    private final char columnDelimiter;

    private final boolean ignoreComments;

    // the comment marker in the file
    private final char comment;

    private final int imageWidth;

    private final int imageHeight;

    private final int thumbnailWidth;

    private final int thumbnailHeight;

    private final ArrayList<String> graphNames;

    private final HashMap<String, TabularDataGraphConfiguration> graphTypeMap;

    /**
     * Class used to dynamically initialize a TabularDataGraphCollectionConfiguration;
     * 
     * @author cramakri
     */
    public static class DynamicTabularDataGraphCollectionConfiguration
    {
        public static String DYNAMIC_GRAPH_NAME = "dynamic";

        private char columnDelimiter = ';';

        private boolean ignoreComments = true;

        private int imageWidth = 800;

        private int imageHeight = 600;

        private int thumbnailWidth = 300;

        private int thumbnailHeight = 200;

        private Properties properties = new Properties();

        public char getColumnDelimiter()
        {
            return columnDelimiter;
        }

        public void setColumnDelimiter(char columnDelimiter)
        {
            this.columnDelimiter = columnDelimiter;
        }

        public boolean isIgnoreComments()
        {
            return ignoreComments;
        }

        public void setIgnoreComments(boolean ignoreComments)
        {
            this.ignoreComments = ignoreComments;
        }

        public int getImageWidth()
        {
            return imageWidth;
        }

        public void setImageWidth(int imageWidth)
        {
            this.imageWidth = imageWidth;
        }

        public int getImageHeight()
        {
            return imageHeight;
        }

        public void setImageHeight(int imageHeight)
        {
            this.imageHeight = imageHeight;
        }

        public int getThumbnailWidth()
        {
            return thumbnailWidth;
        }

        public void setThumbnailWidth(int thumbnailWidth)
        {
            this.thumbnailWidth = thumbnailWidth;
        }

        public int getThumbnailHeight()
        {
            return thumbnailHeight;
        }

        public void setThumbnailHeight(int thumbnailHeight)
        {
            this.thumbnailHeight = thumbnailHeight;
        }

        public Properties getProperties()
        {
            return properties;
        }

        public void setProperties(Properties properties)
        {
            this.properties = properties;
        }

    }

    /**
     * Create a configuration from the properties file located at path.
     * 
     * @param path Path to the properties file.
     */
    public static TabularDataGraphCollectionConfiguration getConfiguration(String path)
            throws EnvironmentFailureException
    {
        Properties configurationProps = new Properties();
        try
        {
            configurationProps.load(new FileInputStream(path));
        } catch (FileNotFoundException ex)
        {
            throw new EnvironmentFailureException("Could not find the configuration file "
                    + new File(path).getAbsolutePath());
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Could not read the configuration file " + path);
        }

        return new TabularDataGraphCollectionConfiguration(configurationProps);
    }

    public static TabularDataGraphCollectionConfiguration getConfiguration(
            DynamicTabularDataGraphCollectionConfiguration config)
            throws EnvironmentFailureException
    {
        return new TabularDataGraphCollectionConfiguration(config);
    }

    /**
     * Initialize the configuration based on the properties object.
     */
    private TabularDataGraphCollectionConfiguration(Properties properties)
    {
        comment = '#';

        this.columnDelimiter = PropertyUtils.getChar(properties, SEPARATOR_PROPERTY_KEY, ',');
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);

        imageWidth = PropertyUtils.getInt(properties, IMAGE_WIDTH_KEY, 800);

        imageHeight = PropertyUtils.getInt(properties, IMAGE_HEIGHT_KEY, 600);

        thumbnailWidth = PropertyUtils.getInt(properties, THUMBNAIL_WIDTH_KEY, 300);

        thumbnailHeight = PropertyUtils.getInt(properties, THUMBNAIL_HEIGHT_KEY, 200);

        graphNames = new ArrayList<String>();
        initializeGraphTypeCodes(properties);
        graphTypeMap = new HashMap<String, TabularDataGraphConfiguration>();
        initialzeGraphTypeMap(properties);

    }

    /**
     * Initialize the configuration based on the configuration object.
     */
    private TabularDataGraphCollectionConfiguration(
            DynamicTabularDataGraphCollectionConfiguration config)
    {
        comment = '#';

        this.columnDelimiter = config.getColumnDelimiter();
        this.ignoreComments = config.isIgnoreComments();
        imageWidth = config.getImageWidth();
        imageHeight = config.getImageHeight();
        thumbnailWidth = config.getThumbnailWidth();
        thumbnailHeight = config.getThumbnailHeight();

        graphNames = new ArrayList<String>();
        graphNames.add(DynamicTabularDataGraphCollectionConfiguration.DYNAMIC_GRAPH_NAME);
        graphTypeMap = new HashMap<String, TabularDataGraphConfiguration>();
        initialzeGraphTypeMap(config.getProperties());
    }

    private void initializeGraphTypeCodes(Properties properties)
    {
        String graphTypeCodesString = properties.getProperty(GRAPHS_KEY, "");
        String[] typeCodeArray = graphTypeCodesString.split(",");
        for (String typeCode : typeCodeArray)
        {
            graphNames.add(typeCode.trim());
        }
    }

    private void initialzeGraphTypeMap(Properties properties)
    {
        SectionProperties[] pluginServicesProperties =
                PropertyParametersUtil.extractSectionProperties(properties, GRAPHS_KEY, false);

        for (SectionProperties sectionProp : pluginServicesProperties)
        {
            TabularDataGraphConfiguration config = getConfiguration(sectionProp);
            graphTypeMap.put(sectionProp.getKey(), config);
        }
    }

    private TabularDataGraphConfiguration getConfiguration(SectionProperties sectionProp)
    {
        Properties props = sectionProp.getProperties();
        String graphTypeValue = PropertyUtils.getMandatoryProperty(props, GRAPHS_TYPES_KEY);
        GraphType type = GraphType.valueOf(graphTypeValue.toUpperCase());
        String title = props.getProperty(TITLE_KEY, sectionProp.getKey());
        switch (type)
        {
            case HEATMAP:
                // Default the Row and Column header names to the standard ones if no override is
                // specified.
                CodeAndLabel xAxis = getCodeAndLabelWithDefault(props, X_AXIS_KEY, "Row");
                CodeAndLabel yAxis = getCodeAndLabelWithDefault(props, Y_AXIS_KEY, "Column");
                CodeAndLabel zAxis = getCodeAndLabel(props, COLUMN_KEY);
                if (xAxis.equals(yAxis))
                {
                    return new TabularDataHeatmapConfiguration(title, xAxis, zAxis,
                            getThumbnailWidth(), getThumbnailHeight());
                } else
                {
                    return new TabularDataHeatmapConfiguration(title, xAxis, yAxis, zAxis,
                            getThumbnailWidth(), getThumbnailHeight());
                }
            case HISTOGRAM:
                return new TabularDataHistogramConfiguration(title, getCodeAndLabel(props,
                        COLUMN_KEY), getThumbnailWidth(), getThumbnailHeight(),
                        PropertyUtils.getInt(props, NUMBER_OF_BINS_KEY, 10));
            case SCATTERPLOT:
                xAxis = getCodeAndLabel(props, X_AXIS_KEY);
                yAxis = getCodeAndLabel(props, Y_AXIS_KEY);
                return new TabularDataScatterplotConfiguration(title, xAxis, yAxis,
                        getThumbnailWidth(), getThumbnailHeight());
        }

        // should never get here
        return null;
    }

    private CodeAndLabel getCodeAndLabel(Properties properties, String key)
    {
        String labelWithOptionalCode = properties.getProperty(key);
        if (labelWithOptionalCode != null)
        {
            return CodeAndLabelUtil.create(labelWithOptionalCode);
        }
        String labelKey = key + LABEL_POSTFIX;
        String label = properties.getProperty(labelKey);
        String codeKey = key + CODE_POSTFIX;
        String code = properties.getProperty(codeKey);
        if (label == null && code == null)
        {
            throw new IllegalArgumentException("Missing one of the following properties: " + key
                    + ", " + codeKey + ", " + labelKey);
        }
        if (code == null)
        {
            return CodeAndLabelUtil.create(label);
        }
        return CodeNormalizer.create(code, label);
    }

    private CodeAndLabel getCodeAndLabelWithDefault(Properties properties, String key,
            String defaultLabel)
    {
        String labelWithOptionalCode = properties.getProperty(key);
        if (labelWithOptionalCode != null)
        {
            return CodeAndLabelUtil.create(labelWithOptionalCode);
        }
        String labelKey = key + LABEL_POSTFIX;
        String label = properties.getProperty(labelKey);
        String codeKey = key + CODE_POSTFIX;
        String code = properties.getProperty(codeKey);
        if (label == null && code == null)
        {
            label = defaultLabel;
            code = CodeNormalizer.normalize(label);
        }
        if (code == null)
        {
            return CodeAndLabelUtil.create(label);
        }
        return CodeNormalizer.create(code, label);
    }

    /**
     * Return the graph configuration associated with the graphTypeCode.
     * 
     * @param graphName The name of the graph type
     */
    public TabularDataGraphConfiguration getGraphConfiguration(String graphName)
    {
        TabularDataGraphConfiguration config = graphTypeMap.get(graphName);
        if (null == config)
        {
            throw new IllegalArgumentException("No graph associated with code " + graphName);
        }
        return config;
    }

    /**
     * Return the graph generator associated with the graphTypeCode, initialized by the fileLines and out.
     * 
     * @param graphName The name of the graph type
     * @param fileLines The data to generate a graph from
     * @param out The stream to write the graph to
     */
    public ITabularDataGraph getGraph(String graphName, ITabularData fileLines, OutputStream out)
    {
        TabularDataGraphConfiguration config = graphTypeMap.get(graphName);
        if (null == config)
        {
            throw new IllegalArgumentException("No graph associated with code " + graphName);
        }
        GraphType type = config.getGraphType();
        switch (type)
        {
            case HEATMAP:
                return new TabularDataHeatmap((TabularDataHeatmapConfiguration) config, fileLines,
                        out);
            case HISTOGRAM:
                return new TabularDataHistogram((TabularDataHistogramConfiguration) config,
                        fileLines, out);
            case SCATTERPLOT:
                return new TabularDataScatterplot((TabularDataScatterplotConfiguration) config,
                        fileLines, out);

        }

        // should never get here
        return null;
    }

    @Override
    public char getColumnDelimiter()
    {
        return columnDelimiter;
    }

    @Override
    public boolean isIgnoreComments()
    {
        return ignoreComments;
    }

    @Override
    public char getCommentDelimiter()
    {
        return comment;
    }

    @Override
    public boolean isSkipEmptyRecords()
    {
        return true;
    }

    public int getImageWidth()
    {
        return imageWidth;
    }

    public int getImageHeight()
    {
        return imageHeight;
    }

    public int getThumbnailWidth()
    {
        return thumbnailWidth;
    }

    public int getThumbnailHeight()
    {
        return thumbnailHeight;
    }

    public List<String> getGraphNames()
    {
        return graphNames;
    }

}
