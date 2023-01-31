/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IArchiveFileRepository;

/**
 * Locates archive files from file system.
 * 
 * @author anttil
 */
public class FileSystemArchiveFileRepository implements IArchiveFileRepository
{

    private final List<File> archiveDirectories;

    private final IFileLocator fileLocator;

    /**
     * @param archiveDirectories
     * @param fileLocator
     */
    public FileSystemArchiveFileRepository(List<File> archiveDirectories, IFileLocator fileLocator)
    {
        this.archiveDirectories = archiveDirectories;
        this.fileLocator = fileLocator;

    }

    @Override
    public File getArchiveFileOf(String dataSet)
    {
        for (File directory : archiveDirectories)
        {
            File file = fileLocator.getPathToArchiveOfDataSet(directory, dataSet);
            if (file.exists())
            {
                return file;
            }
        }

        return new File("...");
    }
}
