/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.ExceptionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author pkupczyk
 */
@Component
public class CreateUploadedDataSetExecutor implements ICreateUploadedDataSetExecutor
{

    @Override
    public DataSetPermId create(String sessionToken, UploadedDataSetCreation creation)
    {
        try
        {
            getOpenBIService().checkSession(sessionToken);

            NewDataSetDTO newDataset = getNewDataSet(sessionToken, creation);

            List<DataSetInformation> dataSetInfos = getPutService().putDataSet(sessionToken, newDataset, creation.getUploadId());

            if (dataSetInfos != null && false == dataSetInfos.isEmpty())
            {
                return new DataSetPermId(dataSetInfos.get(0).getDataSetCode());
            } else
            {
                return null;
            }
        } catch (IOException e)
        {
            throw ExceptionUtils.create(new OperationContext(null), e);
        }
    }

    private NewDataSetDTO getNewDataSet(String sessionToken, UploadedDataSetCreation creation) throws IOException
    {
        DataSetOwnerType ownerType;
        DataSetOwner owner;
        String typeCode;
        List<String> parentCodes = new ArrayList<String>();

        // type

        if (creation.getTypeId() != null)
        {
            DataSetTypeSearchCriteria criteria = new DataSetTypeSearchCriteria();
            criteria.withId().thatEquals(creation.getTypeId());

            SearchResult<DataSetType> searchResult =
                    getApplicationServerApi().searchDataSetTypes(sessionToken, criteria, new DataSetTypeFetchOptions());

            if (searchResult.getObjects() != null && false == searchResult.getObjects().isEmpty())
            {
                DataSetType type = searchResult.getObjects().get(0);
                typeCode = type.getCode();
            } else
            {
                throw new ObjectNotFoundException(creation.getTypeId());
            }
        } else
        {
            throw new UserFailureException("A dataset needs a type.");
        }

        // owner

        if (creation.getSampleId() != null)
        {
            Map<ISampleId, Sample> samples =
                    getApplicationServerApi().getSamples(sessionToken, Arrays.asList(creation.getSampleId()), new SampleFetchOptions());
            Sample sample = samples.get(creation.getSampleId());

            if (sample == null)
            {
                throw new ObjectNotFoundException(creation.getSampleId());
            }

            ownerType = DataSetOwnerType.SAMPLE;
            owner = new NewDataSetDTO.DataSetOwner(ownerType, sample.getIdentifier().getIdentifier());
        } else if (creation.getExperimentId() != null)
        {
            Map<IExperimentId, Experiment> experiments =
                    getApplicationServerApi().getExperiments(sessionToken, Arrays.asList(creation.getExperimentId()), new ExperimentFetchOptions());
            Experiment experiment = experiments.get(creation.getExperimentId());

            if (experiment == null)
            {
                throw new ObjectNotFoundException(creation.getExperimentId());
            }

            ownerType = DataSetOwnerType.EXPERIMENT;
            owner = new NewDataSetDTO.DataSetOwner(ownerType, experiment.getIdentifier().getIdentifier());
        } else
        {
            throw new UserFailureException("A dataset needs either a sample or an experiment as an owner.");
        }

        // parents

        if (creation.getParentIds() != null && false == creation.getParentIds().isEmpty())
        {
            Map<IDataSetId, DataSet> parents =
                    getApplicationServerApi().getDataSets(sessionToken, creation.getParentIds(), new DataSetFetchOptions());

            for (IDataSetId parentId : creation.getParentIds())
            {
                DataSet parent = parents.get(parentId);
                if (parent == null)
                {
                    throw new ObjectNotFoundException(parentId);
                }
                parentCodes.add(parent.getCode());
            }
        }

        NewDataSetDTO newDataSet = new NewDataSetDTO(typeCode, owner, null, new ArrayList<FileInfoDssDTO>());
        newDataSet.setProperties(creation.getProperties());
        newDataSet.setParentDataSetCodes(parentCodes);
        return newDataSet;
    }

    private IApplicationServerApi getApplicationServerApi()
    {
        return ServiceProvider.getV3ApplicationService();
    }

    private IEncapsulatedOpenBISService getOpenBIService()
    {
        return ServiceProvider.getOpenBISService();
    }

    private PutDataSetService getPutService()
    {
        return (PutDataSetService) ServiceProvider.getDataStoreService().getPutDataSetService();
    }
}
