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
import java.io.FileFilter;
import java.util.UUID;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;

/**
 * Locates dataset archive files from an archive directory when sharding is in use.
 * 
 * @author anttil
 */
public class ShardingFileLocator implements IFileLocator
{

    @Override
    public File getPathToArchiveOfDataSet(File directory, String dataSetCode)
    {
        for (File uuidDir : directory.listFiles(new UUIDFileFilter()))
        {
            File candidate = new File(uuidDir, DatasetLocationUtil.getDatasetLocationPath(dataSetCode, "") + "/" + dataSetCode + ".zip");
            if (candidate.exists())
            {
                return candidate;
            }
        }
        return new File("...");
    }

    private class UUIDFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            try
            {
                UUID.fromString(file.getName());
                return true;
            } catch (IllegalArgumentException e)
            {
                return false;
            }
        }
    }
}
