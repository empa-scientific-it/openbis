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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * An <code>abtract</code> implementation of <code>IStorageProcessor</code>.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractStorageProcessor implements IStorageProcessorTransactional
{
    /**
     * Optional property, true by default. If set to false then the dataset whcih cannot be registered will be left in the incoming folder and will be
     * mentioned in the .faulty_paths file.
     */
    private final static String MOVE_UNREGISTERED_DATASETS_TO_ERROR_DIR_PROPERTY =
            "move-unregistered-datasets-to-error-dir";

    private static final String[] ZIP_FILE_EXTENSIONS =
    { "zip" };

    protected final Properties properties;

    private final boolean moveUnregisteredDatasetsToErrorDir;

    private File storeRootDir;

    protected AbstractStorageProcessor(final Properties properties)
    {
        this.properties = properties;
        this.moveUnregisteredDatasetsToErrorDir =
                PropertyUtils.getBoolean(properties,
                        MOVE_UNREGISTERED_DATASETS_TO_ERROR_DIR_PROPERTY, true);
    }

    protected final String getMandatoryProperty(final String propertyKey)
    {
        return PropertyUtils.getMandatoryProperty(properties, propertyKey);
    }

    protected static final void checkParameters(final File incomingDataSetPath,
            final File targetPath)
    {
        assert incomingDataSetPath != null : "Given incoming data set path can not be null.";
        assert targetPath != null : "Given target path can not be null.";
    }

    //
    // IStorageProcessorTransactional
    //

    @Override
    public final File getStoreRootDirectory()
    {
        return storeRootDir;
    }

    @Override
    public final void setStoreRootDirectory(final File storeRootDirectory)
    {
        this.storeRootDir = storeRootDirectory;
    }

    /**
     * @see IStorageProcessorTransactional#getStorageFormat()
     */
    @Override
    public StorageFormat getStorageFormat()
    {
        return StorageFormat.PROPRIETARY;
    }

    protected static boolean isZipFile(File file)
    {
        if (file.isDirectory())
        {
            return false;
        }
        String fileExtension = FilenameUtils.getExtension(file.getName());
        for (String currentExt : ZIP_FILE_EXTENSIONS)
        {
            if (currentExt.equalsIgnoreCase(fileExtension))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public UnstoreDataAction getDefaultUnstoreDataAction(Throwable exception)
    {
        return moveUnregisteredDatasetsToErrorDir ? UnstoreDataAction.MOVE_TO_ERROR
                : UnstoreDataAction.LEAVE_UNTOUCHED;
    }
}
