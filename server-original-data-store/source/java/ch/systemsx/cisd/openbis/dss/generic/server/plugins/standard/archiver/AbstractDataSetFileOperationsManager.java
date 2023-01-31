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

import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Jakub Straszewski
 */
public abstract class AbstractDataSetFileOperationsManager
{

    @Private
    public static final String DESTINATION_KEY = "destination";

    @Private
    public static final String TIMEOUT_KEY = "timeout";

    @Private
    public static final String RSYNC_PASSWORD_FILE_KEY = "rsync-password-file";

    @Private
    protected static final String CHECK_EXISTENCE_FAILED = "couldn't check existence";

    @Private
    public static final String DESTINATION_DOES_NOT_EXIST = "destination doesn't exist";

    @Private
    public static final String RSYNC_EXEC = "rsync";

    @Private
    public static final String SSH_EXEC = "ssh";

    @Private
    public static final String GFIND_EXEC = "find";

    @Private
    public static final long DEFAULT_TIMEOUT_SECONDS = 15;

    @Private
    public static final String FOLDER_OF_AS_DELETED_MARKED_DATA_SETS = "DELETED";

    protected transient IEncapsulatedOpenBISService service;

    protected transient IDataSetDirectoryProvider directoryProvider;

    protected IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }

    protected IDataSetDirectoryProvider getDirectoryProvider()
    {
        if (directoryProvider == null)
        {
            directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
        }
        return directoryProvider;
    }

    protected AbstractExternalData getDataSetWithAllMetaData(DatasetDescription datasetDescription)
    {
        AbstractExternalData dataSet = getService().tryGetDataSet(datasetDescription.getDataSetCode());
        String experimentIdentifier = datasetDescription.getExperimentIdentifier();
        if (experimentIdentifier != null)
        {
            dataSet.setExperiment(getService().tryGetExperiment(ExperimentIdentifierFactory.parse(experimentIdentifier)));
        }
        String sampleIdentifier = datasetDescription.getSampleIdentifier();
        if (sampleIdentifier != null)
        {
            dataSet.setSample(getService().tryGetSampleWithExperiment(SampleIdentifierFactory.parse(sampleIdentifier)));
        }
        List<ContainerDataSet> containerDataSets = dataSet.getContainerDataSets();
        if (containerDataSets != null)
        {
            for (ContainerDataSet containerDataSet : containerDataSets)
            {
                // Inject container properties
                if (containerDataSet.getProperties() == null)
                {
                    containerDataSet.setDataSetProperties(getService().tryGetDataSet(containerDataSet.getCode()).getProperties());
                }
                // Inject full container experiment with properties
                String containerExperimentIdentifier = containerDataSet.getExperiment().getIdentifier();
                containerDataSet.setExperiment(getService().tryGetExperiment(ExperimentIdentifierFactory.parse(containerExperimentIdentifier)));
                // Inject full container sample with properties
                Sample sample = containerDataSet.getSample();
                if (sample != null)
                {
                    String containerSampleIdentifier = sample.getIdentifier();
                    containerDataSet.setSample(getService().tryGetSampleWithExperiment(SampleIdentifierFactory.parse(containerSampleIdentifier)));
                }
            }
        }
        return dataSet;
    }
}
