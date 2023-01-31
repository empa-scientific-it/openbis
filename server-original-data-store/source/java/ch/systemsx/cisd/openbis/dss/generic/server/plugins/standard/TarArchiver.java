/*
 * Copyright 2014 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;

/**
 * Archiver which distributes and tars data sets to be archived on archive destinations specified by the space to which the data set belongs. In
 * addition all meta data of the data set, its related experiment and sample are stored in a tab-separated value file inside the tarred data set.
 * 
 * @author pkupczyk
 */
public class TarArchiver extends RsyncArchiver
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
            TarPackageManager packageManager = new TarPackageManager(properties, new Log4jSimpleLogger(operationLog));
            return new DistributedPackagingDataSetFileOperationsManager(properties, packageManager);
        }
    }

    public TarArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, new DatasetFileOperationsManagerFactory(properties));
    }
}
