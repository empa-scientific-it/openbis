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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Class which is able to create a copy of incoming data for additional processing. The copy has a changed name to trace back the dataset to which the
 * original data belong.
 * <p>
 * The processor uses following properties: {@link #DATASET_CODE_SEPARATOR_PROPERTY}.
 * </p>
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDatasetDropboxHandler implements Serializable,
        IPostRegistrationDatasetHandler
{
    private static final long serialVersionUID = 1L;

    // how many times should it be retried when making copy of items fails
    private final static String COPY_RETRIES_PROPERTY_NAME = "copy-max-retries";

    // interval time in seconds between two copy retries
    private final static String COPY_FAILURE_INTERVAL_IN_SEC_PROPERTY_NAME =
            "copy-failure-interval";

    /**
     * @return the directory where copy of the original data should be created
     */
    abstract protected File tryGetDropboxDir(File originalData,
            DataSetInformation dataSetInformation);

    /**
     * @return the name of the dataset copy file
     */
    abstract protected String createDropboxDestinationFileName(
            DataSetInformation dataSetInformation, File incomingDataSetDirectory);

    // --------

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDatasetDropboxHandler.class);

    private static final String DEFAULT_DATASET_CODE_SEPARATOR = ".";

    /**
     * Property name which is used to specify the text which will be used to separate the sample code and dataset code in the name of the file which
     * will be created in the dropbox.
     */
    public final static String DATASET_CODE_SEPARATOR_PROPERTY = "entity-separator";

    protected final String datasetCodeSeparator;

    private final IFileOperations fileOperations;

    private final int maxRetriesOnFailure;

    private final long millisToSleepOnFailure;

    private File recentlyStoredDropboxDataset;

    public AbstractDatasetDropboxHandler(Properties properties)
    {
        this(properties, FileOperations.getInstance());
    }

    public AbstractDatasetDropboxHandler(Properties properties, IFileOperations fileOperations)
    {
        this.fileOperations = fileOperations;
        this.datasetCodeSeparator =
                PropertyUtils.getProperty(properties, DATASET_CODE_SEPARATOR_PROPERTY,
                        DEFAULT_DATASET_CODE_SEPARATOR);
        this.maxRetriesOnFailure = PropertyUtils.getInt(properties, COPY_RETRIES_PROPERTY_NAME, 0);
        this.millisToSleepOnFailure =
                PropertyUtils.getInt(properties, COPY_FAILURE_INTERVAL_IN_SEC_PROPERTY_NAME, 0) * 1000;
    }

    protected final File tryGetDirectory(String propertyName, Properties properties)
    {
        String filePath = PropertyUtils.getProperty(properties, propertyName);
        if (filePath == null)
        {
            return null;
        }
        File file = new File(filePath);
        if (fileOperations.isDirectory(file) == false)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "The directory '%s' set for '%s' property does not exist.", filePath,
                    propertyName);
        }
        return file;
    }

    @Override
    public final Status handle(File originalData, final DataSetInformation dataSetInformation, Map<String, String> parameterBindings)
    {
        File dropboxDir = tryGetDropboxDir(originalData, dataSetInformation);
        if (dropboxDir != null)
        {
            String destinationFileName =
                    createDropboxDestinationFileName(dataSetInformation, originalData);
            copy(originalData, dropboxDir, destinationFileName);
        }
        return Status.OK;
    }

    private void copy(File originalData, File dropboxDir, String destinationFileName)
    {
        File destFile = new File(dropboxDir, destinationFileName);
        copyToDirectoryAs(originalData, dropboxDir, destinationFileName);
        this.recentlyStoredDropboxDataset = destFile;
    }

    public void copyToDirectoryAs(final File source, final File destDir, final String newName)
    {
        Object result =
                new CallableExecutor(maxRetriesOnFailure, millisToSleepOnFailure)
                        .executeCallable(new Callable<Object>()
                            {
                                // returns null on error, non-null on success
                                @Override
                                public Object call() throws Exception
                                {
                                    try
                                    {
                                        fileOperations.copyToDirectoryAs(source, destDir, newName);
                                    } catch (IOExceptionUnchecked ex)
                                    {
                                        operationLog.warn(createCopyErrorMessage(source, destDir,
                                                newName)
                                                + ". Operation will be retried. Details: "
                                                + ex.getMessage());
                                        return null;
                                    }
                                    return Boolean.TRUE;
                                }
                            });
        if (result == null)
        {
            throw new EnvironmentFailureException(createCopyErrorMessage(source, destDir, newName));
        }
    }

    private String createCopyErrorMessage(final File source, final File destDir,
            final String newName)
    {
        return String.format("Cannot copy '%s' to '%s' as '%s'.", source.getPath(), destDir
                .getPath(), newName);
    }

    @Override
    public void undoLastOperation()
    {
        if (recentlyStoredDropboxDataset != null && recentlyStoredDropboxDataset.exists())
        {
            fileOperations.deleteRecursively(recentlyStoredDropboxDataset);
        }
        recentlyStoredDropboxDataset = null;
    }
}
