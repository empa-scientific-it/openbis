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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.security.MD5ChecksumCalculator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Tomasz Pylak
 */
public class DatasetLocationUtil
{
    /**
     * Creates a location where a dataset can be found in a specified base directory.
     * 
     * @throws UserFailureException if the dataset directory does not exist.
     */
    public static File getDatasetLocationPathCheckingIfExists(String dataSetCode, String shareId,
            DatabaseInstance databaseInstance, File storeDir)
    {
        String databaseUuid = databaseInstance.getUuid();

        File dataSetRootDirectory =
                DatasetLocationUtil.getDatasetLocationPath(storeDir, dataSetCode, shareId,
                        databaseUuid);
        if (dataSetRootDirectory.exists() == false)
        {
            throw new UserFailureException("Data set '" + dataSetCode + "' not found in the store.");
        }
        return dataSetRootDirectory;
    }

    /** Creates a location where a dataset can be found in a specified base directory. */
    public static File getDatasetLocationPath(final File baseDir, String dataSetCode,
            String shareId, final String instanceUUID)
    {
        final File instanceDir = new File(new File(shareId), instanceUUID);
        final File shardingDir = createShardingDir(instanceDir, dataSetCode);
        final File datasetDir = new File(shardingDir, dataSetCode);
        return new File(baseDir, datasetDir.getPath());
    }

    /** returns location (path relative to the share) */
    public static String getDatasetLocationPath(String dataSetCode, final String instanceUUID)
    {
        final File instanceDir = new File(instanceUUID);
        final File shardingDir = createShardingDir(instanceDir, dataSetCode);
        final File datasetDir = new File(shardingDir, dataSetCode);
        return datasetDir.getPath();
    }

    // In order not to overwhelm the file system implementation, we won't use a completely flat
    // hierarchy, but instead a structure called 'data sharding'. 'Data sharding' ensures that there
    // are no directories with an extremely large number of data sets.
    private static File createShardingDir(File parentDir, String dataSetCode)
    {
        String checksum = MD5ChecksumCalculator.calculate(dataSetCode);
        final File dirLevel1 = new File(parentDir, checksum.substring(0, 2));
        final File dirLevel2 = new File(dirLevel1, checksum.substring(2, 4));
        final File dirLevel3 = new File(dirLevel2, checksum.substring(4, 6));
        return dirLevel3;
    }

}
