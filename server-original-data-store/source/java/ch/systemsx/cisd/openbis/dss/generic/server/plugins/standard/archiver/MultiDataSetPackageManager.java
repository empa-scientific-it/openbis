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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.tar.Untar;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.TarPackageManager;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetPackageManager extends TarPackageManager implements IMultiDataSetPackageManager
{

    public MultiDataSetPackageManager(Properties properties, ISimpleLogger ioSpeedLogger)
    {
        super(properties, ioSpeedLogger);
    }

    @Override
    public Status extractMultiDataSets(File packageFile, HashMap<String, File> dataSetCodeToDirectory)
    {
        Untar untar = null;
        try
        {
            untar = new Untar(packageFile);
            untar.extract(dataSetCodeToDirectory);

            for (File location : dataSetCodeToDirectory.values())
            {
                File metadataFile = new File(location, AbstractDataSetPackager.META_DATA_FILE_NAME);
                if (metadataFile.exists() && metadataFile.isFile())
                {
                    FileUtilities.delete(metadataFile);
                }
            }
            return Status.OK;
        } catch (Exception ex)
        {
            logger.log(LogLevel.ERROR, "Error during untaring " + packageFile, ex);
            return Status.createError(ex.toString());
        } finally
        {
            if (untar != null)
            {
                try
                {
                    untar.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }
}
