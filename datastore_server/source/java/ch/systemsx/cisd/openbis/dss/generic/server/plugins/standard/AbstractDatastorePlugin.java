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
import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractDatastorePlugin implements Serializable
{
    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractDatastorePlugin.class);

    static protected final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            AbstractDatastorePlugin.class);

    static protected final Logger notifyLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractDatastorePlugin.class);

    private static final String SUB_DIRECTORY_NAME = "sub-directory-name";

    private static final long serialVersionUID = 1L;

    protected final File storeRoot;

    private final String subDirectory;

    protected final Properties properties;

    protected AbstractDatastorePlugin(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null);
    }

    protected AbstractDatastorePlugin(Properties properties, File storeRoot,
            String subDirectoryOrNull)
    {
        if (storeRoot.exists() == false)
        {
            throw ConfigurationFailureException.fromTemplate("Store root '%s' does not exist.",
                    storeRoot);
        }

        this.storeRoot = storeRoot;
        this.properties = properties;
        if (subDirectoryOrNull == null)
        {
            this.subDirectory = properties.getProperty(SUB_DIRECTORY_NAME, "original");
        } else
        {
            this.subDirectory = subDirectoryOrNull;
        }
    }

    @Deprecated
    /**
     * get directory in the dataset as a File. 
     * @deprecated This method is deprecated as the recommended usage is with IHierarchicalContent* objects.
     */
    protected File getDataSubDir(IDataSetDirectoryProvider provider, DatasetDescription dataset)
    {
        if (StringUtils.isBlank(subDirectory))
        {
            return getDatasetDir(provider, dataset);
        } else
        {
            return new File(getDatasetDir(provider, dataset), subDirectory);
        }
    }

    protected File getDatasetDir(IDataSetDirectoryProvider provider, DatasetDescription dataset)
    {
        return provider.getDataSetDirectory(dataset);
    }

    /**
     * get directory in the dataset as a Hierarchical Content
     */
    protected IHierarchicalContentNode getDataSubDir(IHierarchicalContentProvider provider,
            DatasetDescription dataset)
    {
        if (StringUtils.isBlank(subDirectory))
        {
            return getDatasetDir(provider, dataset).getRootNode();
        } else
        {
            return getDatasetDir(provider, dataset).getNode(subDirectory);
        }
    }

    protected IHierarchicalContent getDatasetDir(IHierarchicalContentProvider provider,
            DatasetDescription dataset)
    {
        return provider.asContent(dataset.getDataSetCode());
    }

    /**
     * get directory in the dataset as a Hierarchical Content
     */
    protected IHierarchicalContentNode getDataSubDir(IHierarchicalContentProvider provider,
            String dataSetCode)
    {
        if (StringUtils.isBlank(subDirectory))
        {
            return getDatasetDir(provider, dataSetCode).getRootNode();
        } else
        {
            return getDatasetDir(provider, dataSetCode).getNode(subDirectory);
        }
    }

    protected IHierarchicalContent getDatasetDir(IHierarchicalContentProvider provider,
            String dataSetCode)
    {
        return provider.asContent(dataSetCode);
    }
}
