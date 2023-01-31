/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.FtpPathResolverRegistry;

/**
 * @author Franz-Josef Elmer
 */
public class FtpPathResolverConfig
{
    final static String DATASET_DISPLAY_TEMPLATE_KEY = "dataset.display.template";

    final static String SHOW_PARENTS_AND_CHILDREN_KEY = "dataset.show-parents-and-children";

    final static String DATASET_FILELIST_SUBPATH_KEY = "dataset.filelist.subpath.";

    final static String DATASET_FILELIST_FILTER_KEY = "dataset.filelist.filter.";

    final static String PATH_RESOLVER_KEY = "resolver-class";

    private static final String DEFAULT_DATASET_TEMPLATE = "${dataSetCode}";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpPathResolverConfig.class);

    private boolean showParentsAndChildren;

    private String dataSetDisplayTemplate = "";

    private Map<String /* dataset type */, String /* path */> fileListSubPaths =
            new HashMap<String, String>();

    private Map<String /* dataset type */, String /* filter pattern */> fileListFilters =
            new HashMap<String, String>();

    private final String resolverClass;

    private final Properties properties;

    public FtpPathResolverConfig(Properties props)
    {
        dataSetDisplayTemplate =
                PropertyUtils.getProperty(props, DATASET_DISPLAY_TEMPLATE_KEY, DEFAULT_DATASET_TEMPLATE);
        showParentsAndChildren = PropertyUtils.getBoolean(props, SHOW_PARENTS_AND_CHILDREN_KEY, false);

        resolverClass = PropertyUtils.getProperty(props, PATH_RESOLVER_KEY, FtpPathResolverRegistry.class.getCanonicalName());

        properties = props;

        ExtendedProperties fileListSubPathProps =
                ExtendedProperties.getSubset(props, DATASET_FILELIST_SUBPATH_KEY, true);
        for (Object key : fileListSubPathProps.keySet())
        {
            String dataSetType = key.toString();
            String subPath = fileListSubPathProps.getProperty(dataSetType);
            fileListSubPaths.put(dataSetType, subPath);

        }

        ExtendedProperties fileListFilterProps =
                ExtendedProperties.getSubset(props, DATASET_FILELIST_FILTER_KEY, true);
        for (Object key : fileListFilterProps.keySet())
        {
            String dataSetType = key.toString();
            String filter = fileListFilterProps.getProperty(dataSetType);
            fileListFilters.put(dataSetType, filter);
        }
    }

    public String getDataSetDisplayTemplate()
    {
        return dataSetDisplayTemplate;
    }

    public boolean isShowParentsAndChildren()
    {
        return showParentsAndChildren;
    }

    public Map<String, String> getFileListSubPaths()
    {
        return Collections.unmodifiableMap(fileListSubPaths);
    }

    public Map<String, String> getFileListFilters()
    {
        return Collections.unmodifiableMap(fileListFilters);
    }

    public void logStartupInfo(String serverType)
    {
        operationLog.info(serverType + " Server data set display template : " + dataSetDisplayTemplate);

        for (Entry<String, String> subpathEntry : fileListSubPaths.entrySet())
        {
            String message =
                    String.format("%s Server subpath configuration for data "
                            + "set type '%s' : '%s'", serverType, subpathEntry.getKey(),
                            subpathEntry.getValue());
            operationLog.info(message);
        }
        for (Entry<String, String> filterEntry : fileListFilters.entrySet())
        {
            String message =
                    String.format("%s Server file filter configuration for data "
                            + "set type '%s' : '%s'", serverType, filterEntry.getKey(), filterEntry.getValue());
            operationLog.info(message);
        }
    }

    public Properties getProperties()
    {
        return properties;
    }

    public IFtpPathResolverRegistry getResolverRegistry()
    {
        try
        {
            Class<?> clazz = Class.forName(resolverClass);
            Object instance = clazz.newInstance();
            IFtpPathResolverRegistry registry = (IFtpPathResolverRegistry) instance;
            registry.initialize(this);
            operationLog.info("Succesfully initialized path resolver of type " + registry.getClass().getName());
            return registry;
        } catch (ClassNotFoundException ex)
        {
            throw new UserFailureException("Failed to create PathResolverRegistry. Couldn't find class " + resolverClass, ex);
        } catch (InstantiationException ex)
        {
            throw new UserFailureException("Failed to create PathResolverRegistry. Couldn't instantiate object of a class " + resolverClass, ex);
        } catch (IllegalAccessException ex)
        {
            throw new UserFailureException("Failed to create PathResolverRegistry. Couldn't instantiate object of a class " + resolverClass, ex);
        } catch (ClassCastException ex)
        {
            throw new UserFailureException("Failed to create PathResolverRegistry. Couldn't cast object of a class " + resolverClass + " to "
                    + IFtpPathResolverRegistry.class.getName(),
                    ex);
        }
    }

}
