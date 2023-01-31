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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Properties;

/**
 * Archiver which distributes and zips data sets to be archived on archive destinations specified by the space to which the data set belongs. In
 * addition all meta data of the data set its related experiment and sample are store in a tab-separated value file inside the zipped data set.
 * 
 * @author Franz-Josef Elmer
 */
public class ZipArchiver extends RsyncArchiver
{

    private static final long serialVersionUID = 1L;

    private static class DatasetFileOperationsManagerFactory implements IDataSetFileOperationsManagerFactory
    {
        private static final long serialVersionUID = 1L;

        private Properties properties;

        public DatasetFileOperationsManagerFactory(Properties properties)
        {
            this.properties = properties;
        }

        @Override
        public IDataSetFileOperationsManager create()
        {
            return new DistributedPackagingDataSetFileOperationsManager(properties, new ZipPackageManager(properties));
        }
    }

    public ZipArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, new DatasetFileOperationsManagerFactory(properties));
    }

}
