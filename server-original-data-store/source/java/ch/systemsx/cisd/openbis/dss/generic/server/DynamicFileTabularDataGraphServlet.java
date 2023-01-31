/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration.DynamicTabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphConfiguration;

/**
 * @author cramakri
 */
public class DynamicFileTabularDataGraphServlet extends FileTabularDataGraphServlet
{

    private static final String DYNAMIC_GRAPH_NAME = "dynamic";

    private static final String PARAM_TITLE = "title";

    private static final String PARAM_X_AXIS_COLUMN = "col-x";

    private static final String PARAM_Y_AXIS_COLUMN = "col-y";

    private static final String PARAM_X_AXIS_LABEL = "label-x";

    private static final String PARAM_Y_AXIS_LABEL = "label-y";

    private static final String PARAM_IMAGE_HEIGHT = "image-height";

    private static final String PARAM_IMAGE_WIDTH = "image-width";

    private static final String PARAM_GRAPH_TYPE = "graph-type";

    private static final String PARAM_GRAPH_NAME = "graph-name";

    private static final String PARAM_DELIMITER = "delimiter";

    private static final String PARAM_Z_AXIS_COLUMN = "col-z";

    private static final String PARAM_BINS = "bins";

    private static final long serialVersionUID = 1L;

    private TabularDataGraphCollectionConfiguration commonConfig;

    @Override
    protected TabularDataGraphCollectionConfiguration getConfiguration(HttpServletRequest request)
    {

        String name = request.getParameter(PARAM_GRAPH_NAME);

        DynamicTabularDataGraphCollectionConfiguration config =
                new DynamicTabularDataGraphCollectionConfiguration();

        Properties props = new Properties();

        if (name != null)
        {
            TabularDataGraphConfiguration graphConfig = commonConfig.getGraphConfiguration(name);
            props.setProperty(TabularDataGraphCollectionConfiguration.TITLE_KEY, graphConfig
                    .getTitle());
            props.setProperty(TabularDataGraphCollectionConfiguration.X_AXIS_KEY, graphConfig
                    .getTitle());
            props.setProperty(TabularDataGraphCollectionConfiguration.Y_AXIS_KEY, graphConfig
                    .getTitle());
            props.setProperty(TabularDataGraphCollectionConfiguration.GRAPHS_TYPES_KEY, graphConfig
                    .getGraphType().toString());

            config.setImageHeight(graphConfig.getImageHeight());
            config.setImageWidth(graphConfig.getImageWidth());
            config.setColumnDelimiter(commonConfig.getColumnDelimiter());
        } else
        {
            name = DYNAMIC_GRAPH_NAME;
        }

        String delimiter = request.getParameter(PARAM_DELIMITER);
        if (delimiter != null && delimiter.toCharArray().length > 0)
        {
            config.setColumnDelimiter(delimiter.toCharArray()[0]);
        }

        props.setProperty(TabularDataGraphCollectionConfiguration.GRAPHS_KEY, name);

        propertyOverride(TabularDataGraphCollectionConfiguration.TITLE_KEY, PARAM_TITLE,
                props, name, request);

        String xColumn =
                propertyOverride(TabularDataGraphCollectionConfiguration.X_AXIS_KEY,
                        PARAM_X_AXIS_COLUMN,
                        props, name, request);

        if (xColumn != null && xColumn.indexOf("<") == -1)
        {
            propertyOverride(TabularDataGraphCollectionConfiguration.X_AXIS_KEY,
                    props, name, "<" + xColumn + ">" + request.getParameter(PARAM_X_AXIS_LABEL));
        }

        String yColumn =
                propertyOverride(TabularDataGraphCollectionConfiguration.Y_AXIS_KEY,
                        PARAM_Y_AXIS_COLUMN,
                        props, name, request);

        if (yColumn != null && yColumn.indexOf("<") == -1)
        {
            propertyOverride(TabularDataGraphCollectionConfiguration.Y_AXIS_KEY,
                    props, name, "<" + yColumn + ">" + request.getParameter(PARAM_Y_AXIS_LABEL));
        }

        propertyOverride(TabularDataGraphCollectionConfiguration.GRAPHS_TYPES_KEY,
                PARAM_GRAPH_TYPE,
                props, name, request);

        int width =
                parsePositiveInt(propertyOverride(PARAM_IMAGE_WIDTH, PARAM_IMAGE_WIDTH, props,
                        name, request));
        int height =
                parsePositiveInt(propertyOverride(PARAM_IMAGE_HEIGHT, PARAM_IMAGE_HEIGHT, props,
                        name, request));

        if (width > 0)
        {
            config.setImageWidth(width);
        }
        if (height > 0)
        {
            config.setImageHeight(height);
        }

        propertyOverride(TabularDataGraphCollectionConfiguration.COLUMN_KEY, PARAM_Z_AXIS_COLUMN,
                props, name, request);
        propertyOverride(TabularDataGraphCollectionConfiguration.NUMBER_OF_BINS_KEY, PARAM_BINS,
                props, name, request);

        config.setProperties(props);

        return TabularDataGraphCollectionConfiguration.getConfiguration(config);
    }

    private String propertyOverride(String propertyName, String parameterName, Properties props,
            String name, HttpServletRequest request)
    {
        String value = request.getParameter(parameterName);
        return propertyOverride(propertyName, props, name, value);
    }

    private String propertyOverride(String propertyName, Properties props,
            String name, String value)
    {
        if (value != null && value.trim().isEmpty() == false)
        {
            props.setProperty(name + "." + propertyName, value);
            return value;
        }
        return props.getProperty(name + "." + propertyName);
    }

    private int parsePositiveInt(String string)
    {
        if (string == null || string.isEmpty())
        {
            return -1;
        }
        try
        {
            int value = Integer.parseInt(string);
            if (value < 1)
            {
                return -1;
            }
            return value;
        } catch (NumberFormatException e)
        {
            return -1;
        }
    }

    @Override
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {

        if (commonConfig == null)
        {
            String propertiesFilePath = servletConfig.getInitParameter(PROPERTIES_FILE_KEY);
            if (propertiesFilePath != null)
            {
                commonConfig =
                        TabularDataGraphCollectionConfiguration
                                .getConfiguration(propertiesFilePath);
            }
        }
    }

}
