/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.server.plugins;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * A processing plugin that create a marker file (named with the dataset code) in specified location.
 * 
 * @author Pawel Glyzewski
 */
public class MarkerFileCreatingProcessingPlugin implements IProcessingPluginTask
{
    private static final String OUTPUT_DIR = "output-dir";

    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MarkerFileCreatingProcessingPlugin.class);

    private final File outputDirectory;

    public MarkerFileCreatingProcessingPlugin(Properties properties, File storeRoot)
    {
        String directoryPath = PropertyUtils.getMandatoryProperty(properties, OUTPUT_DIR);
        this.outputDirectory = new File(directoryPath);
        if (false == outputDirectory.exists() || false == outputDirectory.isDirectory())
        {
            throw new ConfigurationFailureException("'" + OUTPUT_DIR + "' (" + directoryPath
                    + ") should point to an existing directory.");
        }
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        for (DatasetDescription dataset : datasets)
        {
            File markerFile = new File(outputDirectory, dataset.getDataSetCode());
            try
            {
                markerFile.createNewFile();
            } catch (IOException ex)
            {
                CheckedExceptionTunnel.unwrapIfNecessary(ex);
            }
        }

        operationLog.info("Processing done.");
        return null;
    }
}
