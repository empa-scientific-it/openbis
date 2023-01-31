/*
 * Copyright 2008 ETH Zuerich, CISD
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

/**
 * A tiny class which holds the <i>base directory</i> and ensures that the <i>target file</i> lazily gets computed only once.
 * 
 * @author Christian Ribeaud
 */
public final class BaseDirectoryHolder
{

    private final File baseDirectory;

    private final IDataStoreStrategy dataStoreStrategy;

    private final File incomingDataSetPath;

    private File targetFile;

    public BaseDirectoryHolder(final IDataStoreStrategy dataStoreStrategy,
            final File baseDirectory, final File incomingDataSetPath)
    {
        assert dataStoreStrategy != null : "Data store strategy can not be null.";
        assert baseDirectory != null : "Base directory can not be null";
        assert incomingDataSetPath != null : "Incoming data set can not be null.";
        this.dataStoreStrategy = dataStoreStrategy;
        this.baseDirectory = baseDirectory;
        this.incomingDataSetPath = incomingDataSetPath;
    }

    private final File createTargetFile()
    {
        return dataStoreStrategy.getTargetPath(baseDirectory, incomingDataSetPath);
    }

    public final File getBaseDirectory()
    {
        return baseDirectory;
    }

    public final synchronized File getTargetFile()
    {
        if (targetFile == null)
        {
            targetFile = createTargetFile();
        }
        return targetFile;
    }

}
