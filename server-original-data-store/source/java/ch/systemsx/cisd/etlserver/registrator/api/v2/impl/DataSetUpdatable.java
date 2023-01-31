/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PlaceholderDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * A data set already existing in the openBIS database, that is changed as part of the registration process of another data set.
 * 
 * @author Kaloyan Enimanev
 */
public class DataSetUpdatable extends DataSetImmutable implements IDataSetUpdatable
{
    // Keep track of the requested updates so they can be sent to the server.
    private final DataSetBatchUpdatesDTO updates;

    private final DataSetBatchUpdateDetails updateDetails;

    public DataSetUpdatable(AbstractExternalData dataSet, IEncapsulatedBasicOpenBISService service)
    {
        super(dataSet, service);
        if (dataSet.getProperties() == null)
        {
            dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());
        }
        updates = new DataSetBatchUpdatesDTO();
        updateDetails = new DataSetBatchUpdateDetails();
        initializeUpdates();
    }

    public DataSetUpdatable(DataSetImmutable dataSet)
    {
        this(dataSet.dataSet, dataSet.service);
    }

    private void initializeUpdates()
    {
        updates.setDatasetCode(dataSet.getCode());
        updates.setDatasetId(TechId.create(dataSet));
        updates.setDetails(updateDetails);
        updates.setVersion(dataSet.getVersion());
        List<IEntityProperty> emptyProps = Collections.emptyList();
        updates.setProperties(emptyProps);
        updateDetails.setPropertiesToUpdate(new HashSet<String>());
    }

    @Override
    public void setExperiment(IExperimentImmutable experiment)
    {
        updateDetails.setExperimentUpdateRequested(true);
        if (experiment == null)
        {
            dataSet.setExperiment(null);
            updates.setExperimentIdentifierOrNull(null);
        } else
        {
            ExperimentImmutable exp = (ExperimentImmutable) experiment;
            dataSet.setExperiment(exp.getExperiment());
            String identifierString = dataSet.getExperiment().getIdentifier();
            ExperimentIdentifier experimentIdentifier =
                    ExperimentIdentifierFactory.parse(identifierString);
            updates.setExperimentIdentifierOrNull(experimentIdentifier);
        }
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        updateDetails.setSampleUpdateRequested(true);
        if (sampleOrNull == null)
        {
            dataSet.setSample(null);
            updates.setSampleIdentifierOrNull(null);
        } else
        {
            SampleImmutable samp = (SampleImmutable) sampleOrNull;
            dataSet.setSample(samp.getSample());
            setExperiment(sampleOrNull.getExperiment());

            String identifierString = dataSet.getSampleIdentifier();
            SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(identifierString);
            updates.setSampleIdentifierOrNull(sampleIdentifier);
        }
    }

    @Override
    public void setFileFormatType(String fileFormatTypeCode)
    {
        if (isContainerDataSet())
        {
            // ignore
        } else
        {
            FileFormatType fileFormatType = new FileFormatType();
            fileFormatType.setCode(fileFormatTypeCode);
            dataSet.tryGetAsDataSet().setFileFormatType(fileFormatType);

            updateDetails.setFileFormatUpdateRequested(true);
            updates.setFileFormatTypeCode(fileFormatTypeCode);
        }
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(dataSet, propertyCode, propertyValue);

        updates.setProperties(dataSet.getProperties());
        Set<String> propertiesToUpdate = updateDetails.getPropertiesToUpdate();
        propertiesToUpdate.add(propertyCode);
        updateDetails.setPropertiesToUpdate(propertiesToUpdate);
    }

    @Override
    public void setParentDatasets(List<String> parentDataSetCodes)
    {
        List<AbstractExternalData> dummyParents = createDummyDataSetsFromCodes(parentDataSetCodes);
        dataSet.setParents(dummyParents);

        updateDetails.setParentsUpdateRequested(true);
        updates.setModifiedParentDatasetCodesOrNull(parentDataSetCodes.toArray(new String[0]));
    }

    @Override
    public void setContainedDataSetCodes(List<String> containedDataSetCodes)
    {
        if (isContainerDataSet())
        {
            List<AbstractExternalData> dummyDataSets = createDummyDataSetsFromCodes(containedDataSetCodes);
            dataSet.tryGetAsContainerDataSet().setContainedDataSets(dummyDataSets);

            updateDetails.setContainerUpdateRequested(true);
            updates.setModifiedContainedDatasetCodesOrNull(containedDataSetCodes
                    .toArray(new String[0]));
        } else
        {
            // ignored
        }
    }

    private List<AbstractExternalData> createDummyDataSetsFromCodes(List<String> containedDataSetCodes)
    {
        List<AbstractExternalData> dummies = new ArrayList<AbstractExternalData>();
        if (containedDataSetCodes != null)
        {
            for (String code : containedDataSetCodes)
            {
                PlaceholderDataSet dummy = new PlaceholderDataSet();
                dummy.setCode(code);
                dummies.add(dummy);
            }
        }
        return dummies;
    }

    /**
     * Only visible to internal implementation classes. Not part of the public interface.
     */
    public AbstractExternalData getExternalData()
    {
        return dataSet;
    }

    public DataSetBatchUpdatesDTO getUpdates()
    {
        return updates;
    }

    @Override
    public void setExternalDataManagementSystem(
            IExternalDataManagementSystemImmutable externalDataManagementSystem)
    {
        if (isLinkDataSet())
        {
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem dummy =
                    new ExternalDataManagementSystem();
            dummy.setCode(externalDataManagementSystem.getCode());

            dataSet.tryGetAsLinkDataSet().setExternalDataManagementSystem(dummy);
        } else
        {
            // ignore
        }
    }

    @Override
    public void setExternalCode(String externalCode)
    {
        if (isLinkDataSet())
        {
            dataSet.tryGetAsLinkDataSet().setExternalCode(externalCode);
        } else
        {
            // ignore
        }
    }

}
