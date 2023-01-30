/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Utility class working with {@link SimpleDataSetInformationDTO}.
 * 
 * @author Izabela Adamczyk
 */
public class SimpleDataSetHelper
{
    /**
     * filters out all non-file datasets and applies translation on the remaining datasets
     */
    public static final List<SimpleDataSetInformationDTO> filterAndTranslate(
            List<AbstractExternalData> externalData)
    {
        if (externalData == null)
        {
            return null;
        }
        List<SimpleDataSetInformationDTO> result = new ArrayList<SimpleDataSetInformationDTO>();
        for (AbstractExternalData ed : externalData)
        {
            if (ed instanceof PhysicalDataSet)
            {
                PhysicalDataSet dataSet = (PhysicalDataSet) ed;
                result.add(translate(dataSet));
            }
        }
        return result;
    }

    private static SimpleDataSetInformationDTO translate(PhysicalDataSet data)
    {
        SimpleDataSetInformationDTO result = new SimpleDataSetInformationDTO();
        result.setDataStoreCode(data.getDataStore().getCode());
        result.setDataSetCode(data.getCode());
        result.setRegistrationTimestamp(data.getRegistrationDate());
        result.setModificationTimestamp(data.getModificationDate());
        result.setAccessTimestamp(data.getAccessTimestamp());
        result.setSpeedHint(data.getSpeedHint());
        result.setDataSetShareId(data.getShareId());
        result.setDataSetLocation(data.getLocation());
        result.setStatus(data.getStatus());
        result.setPresentInArchive(data.isPresentInArchive());
        result.setDataSetSize(data.getSize());
        result.setSpaceCode(data.getSpace().getCode());
        result.setStorageConfirmed(data.isStorageConfirmation());
        Experiment experiment = data.getExperiment();
        if (experiment != null)
        {
            result.setExperimentCode(experiment.getCode());
            result.setProjectCode(experiment.getProject().getCode());
        }
        Sample sample = data.getSample();
        if (sample != null)
        {
            result.setSampleCode(data.getSampleCode());
        }
        result.setDataSetType(data.getDataSetType().getCode());
        result.setDataStoreUrl(data.getDataStore().getHostUrl());
        List<ContainerDataSet> containerDataSets = data.getContainerDataSets();
        for (ContainerDataSet containerDataSet : containerDataSets)
        {
            String containerDataSetCode = containerDataSet.getCode();
            result.addOrderInContainer(containerDataSetCode, data.getOrderInContainer(containerDataSetCode));
        }
        result.setH5Folders(data.isH5Folders());
        result.setH5arFolders(data.isH5arFolders());
        return result;
    }

    public static SimpleDataSetInformationDTO translate(DatasetDescription datasetDescription)
    {
        SimpleDataSetInformationDTO result = new SimpleDataSetInformationDTO();
        result.setDataSetCode(datasetDescription.getDataSetCode());
        result.setRegistrationTimestamp(datasetDescription.getRegistrationTimestamp());
        result.setSpeedHint(datasetDescription.getSpeedHint());
        result.setDataSetLocation(datasetDescription.getDataSetLocation());
        result.setDataSetSize(datasetDescription.getDataSetSize());
        result.setDataSetType(datasetDescription.getDataSetTypeCode());
        result.setExperimentCode(datasetDescription.getExperimentCode());
        result.setSpaceCode(datasetDescription.getSpaceCode());
        result.setProjectCode(datasetDescription.getProjectCode());
        result.setSampleCode(datasetDescription.getSampleCode());
        result.setOrderInContainers(datasetDescription.getOrderInContainers());
        result.setStorageConfirmed(datasetDescription.isStorageConfirmed());
        return result;
    }
}
