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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;

/**
 * @author Piotr Buczek
 */
public class DatasetImageOverviewConfiguration
{
    private static final String PLUGIN_SERVICE_CLASS_KEY = "class";

    private static final String PLUGIN_SERVICE_DEFAULT_KEY = "default";

    private static final String PLUGIN_SERVICE_DATASET_TYPE_PATTERNS_KEY = "dataset-types";

    public static DatasetImageOverviewConfiguration createConfiguration(Properties properties)
    {
        DatasetImageOverviewConfiguration configuration = new DatasetImageOverviewConfiguration();

        SectionProperties[] pluginServicesProperties =
                PropertyParametersUtil.extractSectionProperties(properties,
                        Constants.OVERVIEW_PLUGINS_SERVICES_LIST_KEY, false);
        for (SectionProperties sectionProperties : pluginServicesProperties)
        {
            Properties props = sectionProperties.getProperties();
            String pluginClassName =
                    PropertyUtils.getMandatoryProperty(props, PLUGIN_SERVICE_CLASS_KEY);
            boolean isDefault = PropertyUtils.getBoolean(props, PLUGIN_SERVICE_DEFAULT_KEY, false);
            if (isDefault)
            {
                configuration.setDefaultPluginService(pluginClassName, props);
            } else
            {
                List<String> dataSetTypePatterns =
                        PropertyUtils.getMandatoryList(props,
                                PLUGIN_SERVICE_DATASET_TYPE_PATTERNS_KEY);
                configuration.addPluginService(pluginClassName, dataSetTypePatterns, props);
            }
        }

        return configuration;
    }

    private Map<String, IDatasetImageOverviewPlugin> pluginsByDataSetTypePattern =
            new HashMap<String, IDatasetImageOverviewPlugin>();

    private IDatasetImageOverviewPlugin defaultPluginOrNull;

    public IDatasetImageOverviewPlugin getDatasetImageOverviewPlugin(String datasetTypeCode)
    {
        IDatasetImageOverviewPlugin plugin = tryFindPlugin(datasetTypeCode);
        if (plugin == null)
        {
            if (defaultPluginOrNull == null)
            {
                throw new ConfigurationFailureException(String.format(
                        "No image overview plugin is configured for data set type %s "
                                + "and there is no default plugin configured either.",
                        datasetTypeCode));
            } else
            {
                plugin = defaultPluginOrNull;
            }
        }
        return plugin;
    }

    private IDatasetImageOverviewPlugin tryFindPlugin(String datasetTypeCode)
    {
        for (Entry<String, IDatasetImageOverviewPlugin> entry : pluginsByDataSetTypePattern
                .entrySet())
        {
            String datasetTypePattern = entry.getKey();
            if (datasetTypeCode.matches(datasetTypePattern))
            {
                return entry.getValue();
            }
        }
        return null;
    }

    private void addPluginService(String pluginClass, List<String> dataSetTypePatterns,
            Properties pluginProperties)
    {
        for (String dataSetTypePattern : dataSetTypePatterns)
        {
            addPluginService(dataSetTypePattern, pluginClass, pluginProperties);
        }
    }

    private void addPluginService(String dataSetTypePattern, String pluginClassName,
            Properties pluginProperties)
    {
        String normalizedTypePattern = dataSetTypePattern.toUpperCase();
        IDatasetImageOverviewPlugin oldPluginOrNull = tryFindPlugin(normalizedTypePattern);
        if (oldPluginOrNull != null)
        {
            throw new ConfigurationFailureException(
                    String.format(
                            "More than one image overview plugin is configured for data set type %s (plugin classes: %s, %s)",
                            oldPluginOrNull.getClass().getName(), pluginClassName));
        }
        IDatasetImageOverviewPlugin plugin = createPlugin(pluginClassName, pluginProperties);
        pluginsByDataSetTypePattern.put(normalizedTypePattern, plugin);
    }

    private void setDefaultPluginService(String pluginClassName, Properties pluginProperties)
    {
        if (defaultPluginOrNull != null)
        {
            throw new ConfigurationFailureException(
                    String.format(
                            "Default image overview plugin is configured more than once (plugin classes: %s, %s)",
                            defaultPluginOrNull.getClass().getName(), pluginClassName));
        }
        IDatasetImageOverviewPlugin plugin = createPlugin(pluginClassName, pluginProperties);
        defaultPluginOrNull = plugin;
    }

    private IDatasetImageOverviewPlugin createPlugin(String pluginClassName,
            Properties pluginProperties)
    {
        try
        {
            return ClassUtils.create(IDatasetImageOverviewPlugin.class, pluginClassName,
                    pluginProperties);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the plugin class '"
                    + pluginClassName + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

}
