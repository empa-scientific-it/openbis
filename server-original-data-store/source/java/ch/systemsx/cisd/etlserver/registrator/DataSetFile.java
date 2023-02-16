/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.io.Serializable;

/**
 * Wrapper that bundles two copies of incoming dataset file. One is original file from the dropbox (realIncomingFile), second is the prestaging copy
 * (logicalIncomingFile). In most cases, clients should use the logicalIncomingFile. The exception being for actions that execute on error.
 * 
 * @author jakubs
 */
public class DataSetFile implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final File realIncomingFile;

    private final File logicalIncomingFile;

    /**
     * Creates the dataset file with original incoming and prestaging copy
     */
    public DataSetFile(File originalIncoming, File prestagingCopy)
    {
        this.realIncomingFile = originalIncoming;
        this.logicalIncomingFile = prestagingCopy;
    }

    /**
     * Creates the dataset file without prestaging copy.
     */
    public DataSetFile(File incoming)
    {
        this.realIncomingFile = this.logicalIncomingFile = incoming;
    }

    /**
     * This file should be used only when it is really important to do something on the original File.
     * 
     * @returns the original incoming dataset.
     */
    public File getRealIncomingFile()
    {
        return realIncomingFile;
    }

    /**
     * This file should be used for most of the processing.
     * 
     * @returns The prestaging copy. If not available returns the original incoming file.
     */
    public File getLogicalIncomingFile()
    {
        return (logicalIncomingFile != null) ? logicalIncomingFile : realIncomingFile;
    }

    public boolean isLogicalFileSpecified()
    {
        return logicalIncomingFile != null
                && (false == (realIncomingFile.equals(logicalIncomingFile)));
    }

    /**
     * @returns something like "original file: /local/path/inbox/file.txt logical file: /local/path/pre-staging/file.txt"
     */
    @Override
    public String toString()
    {
        return "original file: " + realIncomingFile + " logical file: " + logicalIncomingFile;
    }
}
