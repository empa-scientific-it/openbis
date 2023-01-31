/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.ITableModelTransformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Decorator of a {@link IReportingPluginTask} (of type table model) by a {@link ITableModelTransformation}.
 * 
 * @author Franz-Josef Elmer
 */
public class DecoratingTableModelReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final String REPORTING_PLUGIN_KEY = "reporting-plugin";

    private static final String TRANSFORMATION_KEY = "transformation";

    private final IReportingPluginTask reportingPlugin;

    private ITableModelTransformation transformation;

    public DecoratingTableModelReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        Properties pluginProperties =
                ExtendedProperties.getSubset(properties, REPORTING_PLUGIN_KEY + ".", true);
        reportingPlugin = createReportingPlugin(pluginProperties);
        if (reportingPlugin.getReportingPluginType().equals(ReportingPluginType.TABLE_MODEL) == false)
        {
            throw new ConfigurationFailureException(
                    "Only reporting plugins of type table model can be decorated.");
        }
        Properties transformationProperties =
                ExtendedProperties.getSubset(properties, TRANSFORMATION_KEY + ".", true);
        transformation = createTransformation(transformationProperties);
    }

    private IReportingPluginTask createReportingPlugin(Properties pluginProperties)
    {
        String className =
                PropertyUtils.getMandatoryProperty(pluginProperties,
                        PluginTaskFactory.CLASS_PROPERTY_NAME);
        try
        {
            return ClassUtils.create(IReportingPluginTask.class, className, pluginProperties,
                    storeRoot);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot create the plugin class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    private ITableModelTransformation createTransformation(Properties transformationProperties)
    {
        String className =
                PropertyUtils.getMandatoryProperty(transformationProperties,
                        PluginTaskFactory.CLASS_PROPERTY_NAME);
        try
        {
            return ClassUtils.create(ITableModelTransformation.class, className,
                    transformationProperties);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot create the transformation class '"
                    + className + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        TableModel report = reportingPlugin.createReport(datasets, context);
        return transformation.transform(report);
    }

}
