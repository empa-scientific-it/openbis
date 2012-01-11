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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Utility class to load properties.
 * 
 * @author Tomasz Pylak
 */
public class DssPropertyParametersUtil
{
    /** Prefix of system properties which may override service.properties. */
    public static final String OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX = "openbis.dss.";

    public static final String DSS_CODE_KEY = "data-store-server-code";

    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    public static final String DOWNLOAD_URL_KEY = "download-url";

    public static final String SERVER_URL_KEY = "server-url";

    /**
     * Temp directory for dss usage.
     */
    static final String DSS_TEMP_DIR_PATH = "dss-temp-dir";

    /**
     * Directory for registration log files.
     */
    public static final String DSS_REGISTRATION_LOG_DIR_PATH = "dss-registration-log-dir";

    /** Location of service properties file. */
    public static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    /** loads server configuration */
    public static ExtendedProperties loadServiceProperties()
    {
        return loadProperties(SERVICE_PROPERTIES_FILE);
    }

    public static ExtendedProperties loadProperties(String filePath)
    {
        Properties properties = PropertyUtils.loadProperties(filePath);
        Properties systemProperties = System.getProperties();
        ExtendedProperties dssSystemProperties =
                ExtendedProperties.getSubset(systemProperties,
                        OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX, true);
        Set<Entry<Object, Object>> entrySet = dssSystemProperties.entrySet();
        for (Entry<Object, Object> entry : entrySet)
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        return ExtendedProperties.createWith(properties);
    }

    public static String getDataStoreCode(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DSS_CODE_KEY).toUpperCase();
    }

    public final static File getStoreRootDir(final Properties properties)
    {
        return FileUtilities.normalizeFile(new File(PropertyUtils.getMandatoryProperty(properties,
                STOREROOT_DIR_KEY)));
    }

    public static String getOpenBisServerUrl(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, SERVER_URL_KEY);
    }

    public static String getDownloadUrl(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DOWNLOAD_URL_KEY);
    }

    public static File getDssInternalTempDir(final Properties properties)
    {
        String defaultTempDirPath =
                new File(System.getProperty("user.dir"), "dss-tmp").getAbsolutePath();
        String tempDirPath =
                PropertyUtils.getProperty(properties, DSS_TEMP_DIR_PATH, defaultTempDirPath);
        File tempDir = new File(tempDirPath);
        tempDir.mkdirs();
        if (false == tempDir.exists())
        {
            throw new ConfigurationFailureException(
                    "Could not create an internal temp directory for the data store server at path: "
                            + tempDir.getPath()
                            + ". Please make sure this directory exists and is writable by the data store server or provide an alternate using directory for the "
                            + DSS_TEMP_DIR_PATH + " configuration setting");
        }
        return tempDir;
    }

    public static File getDssRegistrationLogDir(final Properties properties)
    {
        String defaultRegistrationLogDirPath =
                new File(System.getProperty("user.dir"), "log-registrations").getAbsolutePath();
        String registrationLogDirPath =
                PropertyUtils.getProperty(properties, DSS_REGISTRATION_LOG_DIR_PATH,
                        defaultRegistrationLogDirPath);
        File registrationLogDir = new File(registrationLogDirPath);
        registrationLogDir.mkdirs();
        if (false == registrationLogDir.exists())
        {
            throw new ConfigurationFailureException(
                    "Could not create a directory for storing for registration logs at path: "
                            + registrationLogDir.getPath()
                            + ". Please make sure this directory exists and is writable by the data store server or provide an alternate using directory for the "
                            + DSS_REGISTRATION_LOG_DIR_PATH + " configuration setting");
        }
        return registrationLogDir;
    }
}
