/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.bdsmigration;

import java.io.File;

import ch.systemsx.cisd.etlserver.plugins.IMigrator;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractBDSMigrator implements IMigrator
{
    static final String ANNOTATIONS_DIR = "annotations";

    static final String METADATA_DIR = "metadata";

    static final String DATA_DIR = "data";

    static final String ORIGINAL_DIR = "original";

    static final String VERSION_DIR = "version";

    static final String DIR_SEP = "/";

    public boolean migrate(File dataset)
    {
        if (isBDS(dataset))
        {
            return doMigration(dataset);
        }
        return true;
    }

    protected abstract boolean doMigration(File dataset);

    private static boolean isBDS(File dataset)
    {
        File[] files = dataset.listFiles();
        return containsDir(files, VERSION_DIR) && containsDir(files, DATA_DIR)
                && containsDir(files, METADATA_DIR) && containsDir(files, ANNOTATIONS_DIR);
    }

    private static boolean containsDir(File[] files, String dirName)
    {
        if (files != null)
        {
            for (File file : files)
            {
                if (file.getName().equalsIgnoreCase(dirName))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void close()
    {
        // do nothing

    }

}
