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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;

/**
 * Factory of Archiver Tasks.
 * 
 * @author Piotr Buczek
 */
public class ArchiverPluginFactory
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ArchiverPluginFactory.class);

    private final String className;

    private final Properties archiverProperties;

    /** Property name which stores a plugin class name. */
    @Private
    public final static String CLASS_PROPERTY_NAME = "class";

    public ArchiverPluginFactory(SectionProperties sectionProperties)
    {
        this.className = sectionProperties.getProperties().getProperty(CLASS_PROPERTY_NAME);
        this.archiverProperties = sectionProperties.getProperties();
    }

    /**
     * Checks creation of archiver plugin is possible for specified store.
     */
    public void check(File storeRoot)
    {
        if (isArchiverConfigured() == false)
        {
            return;
        }
        createInstance(storeRoot);
    }

    public boolean isArchiverConfigured()
    {
        return className != null;
    }

    public Properties getArchiverProperties()
    {
        return archiverProperties;
    }

    public IArchiverPlugin createInstance(File storeRoot)
    {
        if (isArchiverConfigured() == false)
        {
            throw new ConfigurationFailureException("Archiver class is not configured.");
        }
        try
        {
            return ClassUtils.create(IArchiverPlugin.class, className, archiverProperties,
                    storeRoot);
        } catch (ConfigurationFailureException ex)
        {
            throw ex; // rethrow the exception without changing the message
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the archiver class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void logConfiguration()
    {
        operationLog.info("Archiver configuration:");
        logPropertiesConfiguration();
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    private final void logPropertiesConfiguration()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("%s = %s", CLASS_PROPERTY_NAME, className));
        }
    }

}
