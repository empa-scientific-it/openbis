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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.AbstractDatasetDropboxHandler;

/**
 * Note: this class is currently used only in integration tests.<br>
 * Storage processor which is able to create a copy of incoming data for additional processing. The copy has a changed name to trace back the dataset
 * to which the original data belong.
 * <p>
 * The processor uses following properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY}, {@link #DROPBOX_INCOMING_DIRECTORY_PROPERTY} and
 * {@link AbstractDatasetDropboxHandler#DATASET_CODE_SEPARATOR_PROPERTY}. All the properties are also passed for the default processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class StorageProcessorWithDropbox extends DelegatingStorageProcessorWithDropbox
{
    /**
     * The path to the directory where an additional copy of the original incoming data will be created for additional processing.
     */
    public final static String DROPBOX_INCOMING_DIRECTORY_PROPERTY = "dropbox-dir";

    public StorageProcessorWithDropbox(Properties properties)
    {
        super(properties);
    }

    @Override
    public IPostRegistrationDatasetHandler createPostRegistrationDataSetHandler()
    {
        return new DatasetDropboxHandler(properties);
    }

    private static class DatasetDropboxHandler extends AbstractDatasetDropboxHandler
    {
        private static final long serialVersionUID = 1L;

        private final File dropboxIncomingDir;

        public DatasetDropboxHandler(Properties properties)
        {
            super(properties);
            this.dropboxIncomingDir =
                    tryGetDirectory(DROPBOX_INCOMING_DIRECTORY_PROPERTY, properties);
        }

        @Override
        protected File tryGetDropboxDir(File originalData, DataSetInformation dataSetInformation)
        {
            return dropboxIncomingDir;
        }

        @Override
        protected String createDropboxDestinationFileName(DataSetInformation dataSetInformation,
                File incomingDataSetDirectory)
        {
            return createDropboxDestinationFileName(dataSetInformation, incomingDataSetDirectory,
                    datasetCodeSeparator);
        }

        private static String createDropboxDestinationFileName(
                DataSetInformation dataSetInformation, File incomingDataSetDirectory,
                String datasetCodeSeparator)
        {
            String dataSetCode = dataSetInformation.getDataSetCode();
            String originalName = incomingDataSetDirectory.getName();
            String newFileName =
                    stripFileName(originalName) + datasetCodeSeparator + dataSetCode
                            + stripFileExtension(originalName);
            return newFileName;
        }

        // returns file extension with the "." at the beginning or empty string if file has no
        // extension
        private static String stripFileExtension(String originalName)
        {
            int ix = originalName.lastIndexOf(".");
            if (ix == -1)
            {
                return "";
            } else
            {
                return originalName.substring(ix);
            }
        }

        private static String stripFileName(String originalName)
        {
            int ix = originalName.lastIndexOf(".");
            if (ix == -1)
            {
                return originalName;
            } else
            {
                return originalName.substring(0, ix);
            }
        }
    }

}
