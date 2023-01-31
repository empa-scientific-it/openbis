/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.spring.WhiteAndBlackListCodebaseAwareObjectInputStream;
import ch.systemsx.cisd.openbis.dss.generic.server.IServletPropertiesManager;
import ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases.AbstractSearchDomainService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;

/**
 * Factory of plugin tasks.
 * 
 * @author Tomasz Pylak
 */
public class PluginTaskFactory<T>
{
    /** Property name which stores a plugin label. */
    @Private
    public final static String LABEL_PROPERTY_NAME = "label";

    /** Property name which stores a list of dataset type codes. */
    @Private
    public final static String DATASET_CODES_PROPERTY_NAME = "dataset-types";

    /** Property name which stores a plugin class name. */
    public final static String CLASS_PROPERTY_NAME = "class";

    @Private
    public static final String SERVLET_PROPERTY_NAME = "servlet";

    @Private
    public static final String SERVLETS_PROPERTY_NAME = "servlets";

    /**
     * Property name which stores a file path. The file should contain properties which are plugin parameters.
     */
    @Private
    public final static String PARAMS_FILE_PATH_PROPERTY_NAME = "properties-file";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PluginTaskFactory.class);

    private final DatastoreServiceDescription description;

    private final String className;

    private final Properties instanceParameters;

    private final T pluginInstance;

    private final String pluginTaskName;

    public PluginTaskFactory(IServletPropertiesManager servletPropertiesManager,
            SectionProperties sectionProperties, String datastoreCode, Class<T> clazz, String pluginTaskName,
            File storeRoot)
    {
        this.pluginTaskName = pluginTaskName;
        Properties pluginProperties = sectionProperties.getProperties();
        WhiteAndBlackListCodebaseAwareObjectInputStream.populateWhiteAndBlackListOfApiParameterClasses(pluginProperties);
        String label = PropertyUtils.getMandatoryProperty(pluginProperties, LABEL_PROPERTY_NAME);
        Properties props =
                ExtendedProperties.getSubset(pluginProperties, SERVLET_PROPERTY_NAME + ".", true);
        if (props.isEmpty())
        {
            SectionProperties[] servletsProperties =
                    PropertyParametersUtil.extractSectionProperties(pluginProperties,
                            SERVLETS_PROPERTY_NAME, false);
            if (servletsProperties.length > 0)
            {
                servletPropertiesManager.addServletsProperties(label + ", ", servletsProperties);
            }
        } else
        {
            servletPropertiesManager.addServletProperties(label, props);
        }
        String pluginKey = sectionProperties.getKey();

        this.className = PropertyUtils.getMandatoryProperty(pluginProperties, CLASS_PROPERTY_NAME);
        this.instanceParameters = extractInstanceParameters(pluginProperties);
        this.pluginInstance = createPluginInstance(clazz, storeRoot);

        // Reporting plugins needs to add some additional information to the description
        if (pluginInstance instanceof IReportingPluginTask)
        {
            ReportingPluginType type =
                    ((IReportingPluginTask) pluginInstance).getReportingPluginType();
            String[] datasetCodes =
                    type == ReportingPluginType.AGGREGATION_TABLE_MODEL ? new String[0]
                            : extractDatasetCodes(pluginProperties);
            this.description =
                    DatastoreServiceDescription.reporting(pluginKey, label, datasetCodes,
                            datastoreCode, type);
        } else if (pluginInstance instanceof ISearchDomainService)
        {
            this.description =
                    DatastoreServiceDescription.processing(pluginKey, label, new String[0],
                            datastoreCode);
            if (this.pluginInstance instanceof AbstractSearchDomainService)
            {
                ((AbstractSearchDomainService) this.pluginInstance).setName(pluginKey);
            }
        } else
        {
            String[] datasetCodes = extractDatasetCodes(pluginProperties);
            this.description =
                    DatastoreServiceDescription.processing(pluginKey, label, datasetCodes,
                            datastoreCode);
        }
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void logConfiguration()
    {
        String key = getPluginDescription().getKey();
        operationLog.info(String.format(pluginTaskName + " '%s' configuration:", key));
        logPropertiesConfiguration();
    }

    /**
     * Returns an instance of a plugin task
     */
    public T getPluginInstance()
    {
        return pluginInstance;
    }

    private T createPluginInstance(Class<T> clazz, File storeRoot)
    {
        try
        {
            return ClassUtils.create(clazz, className, instanceParameters, storeRoot);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot create the plugin class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    private static Properties extractInstanceParameters(final Properties pluginProperties)
    {
        String parametersFilePath = pluginProperties.getProperty(PARAMS_FILE_PATH_PROPERTY_NAME);
        Properties properties = new Properties();
        addAll(properties, pluginProperties);
        if (StringUtils.isBlank(parametersFilePath) == false)
        {
            Properties propertiesFromFile =
                    DssPropertyParametersUtil.loadProperties(parametersFilePath);
            addAll(properties, propertiesFromFile);
        }
        return properties;
    }

    // adds all properties from 'propertiesToAdd' to 'result' overriding the overlapping keys
    private static void addAll(Properties result, Properties propertiesToAdd)
    {
        for (Object key : propertiesToAdd.keySet())
        {
            result.put(key, propertiesToAdd.get(key));
        }
    }

    private static String[] extractDatasetCodes(final Properties pluginProperties)
    {
        String datasetCodesValues =
                PropertyUtils.getMandatoryProperty(pluginProperties, DATASET_CODES_PROPERTY_NAME);
        return PropertyParametersUtil.parseItemisedProperty(datasetCodesValues,
                DATASET_CODES_PROPERTY_NAME);
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    protected final void logPropertiesConfiguration()
    {
        if (operationLog.isInfoEnabled())
        {
            logLine(LABEL_PROPERTY_NAME, description.getLabel());
            logLine(DATASET_CODES_PROPERTY_NAME,
                    CollectionUtils.abbreviate(description.getDatasetTypeCodes(), -1));
            logLine(CLASS_PROPERTY_NAME, className);
            logLine(PARAMS_FILE_PATH_PROPERTY_NAME, instanceParameters.toString());
        }
    }

    private void logLine(String propertyName, String value)
    {
        operationLog.info(String.format("%s.%s = %s", description.getKey(), propertyName, value));
    }

    /**
     * Ensures that the factory configuration is correct and it is able to create plugins instances
     * 
     * @param checkIfSerializable if true it will be checked that the plugiin instance can be serialized and deserialized
     */
    public void check(boolean checkIfSerializable)
    {
        if (checkIfSerializable)
        {
            checkInstanceSerializable();
        }
    }

    private void checkInstanceSerializable()
    {
        try
        {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outStream);
            out.writeObject(pluginInstance);
            byte[] byteArray = outStream.toByteArray();

            InputStream inStream = new ByteArrayInputStream(byteArray);
            ObjectInputStream in = new ObjectInputStream(inStream);
            in.readObject(); // read the object back
        } catch (Exception ex)
        {
            throwSerializationError(ex.getMessage());
        }

    }

    private void throwSerializationError(String message)
    {
        throw UserFailureException.fromTemplate(
                "Plugin '%s' has problems with serialization/deserialization: %s", pluginInstance
                        .getClass().getName(), message);
    }

    /** @return description of a plugin task. It is the same for all plugin instances. */
    public DatastoreServiceDescription getPluginDescription()
    {
        return description;
    }
}
