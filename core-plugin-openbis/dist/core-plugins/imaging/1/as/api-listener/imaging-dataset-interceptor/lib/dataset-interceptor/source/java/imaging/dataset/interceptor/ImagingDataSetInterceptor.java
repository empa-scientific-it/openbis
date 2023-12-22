/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package imaging.dataset.interceptor;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.listener.IOperationListener;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPropertyConfig;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ImagingDataSetInterceptor implements IOperationListener
{

    static final String IMAGING_CONFIG_PROPERTY_NAME = "$IMAGING_DATA_CONFIG";
    static final String IMAGING_TYPE = "IMAGING_DATA";
    static final String PREVIEW_TOTAL_COUNT = "preview-total-count";

    private boolean isImagingDataSet(String typePermId) {
        if(typePermId == null || typePermId.isBlank()) {
            return false;
        }
        return typePermId.toUpperCase().contains(IMAGING_TYPE);
    }

    private DataSet getDataSetToUpdate(DataSetUpdate update, IApplicationServerApi api, String sessionToken) {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();

        Map<IDataSetId, DataSet> dataSetToUpdateSearch = api.getDataSets(sessionToken, List.of(update.getDataSetId()), fetchOptions);
        return dataSetToUpdateSearch.get(update.getDataSetId());
    }

    private ImagingDataSetPropertyConfig readConfig(String val)
    {
        try
        {
            ObjectMapper objectMapper = new GenericObjectMapper();
            return objectMapper.readValue(new ByteArrayInputStream(val.getBytes()),
                    ImagingDataSetPropertyConfig.class);
        } catch (JsonMappingException mappingException)
        {
            throw new UserFailureException(mappingException.toString(), mappingException);
        } catch (Exception e)
        {
            throw new UserFailureException("Could not read the parameters!", e);
        }
    }

    private String getPropertyConfig(IPropertiesHolder holder) {
        String propertyValue = holder.getJsonProperty(IMAGING_CONFIG_PROPERTY_NAME);
        if(propertyValue == null) {
            propertyValue = holder.getJsonProperty(IMAGING_CONFIG_PROPERTY_NAME.toLowerCase());
        }
        return propertyValue;
    }


    @Override
    public void beforeOperation(IApplicationServerApi api, String sessionToken,
            IOperation operation)
    {
        if(operation instanceof CreateDataSetsOperation) {
            CreateDataSetsOperation createDataSetsOperation = (CreateDataSetsOperation) operation;

            for(DataSetCreation creation : createDataSetsOperation.getCreations()) {

                EntityTypePermId typeId = (EntityTypePermId) creation.getTypeId();
                String objectTypeCode = typeId.getPermId();
                if(isImagingDataSet(objectTypeCode)) {

                    String propertyConfig = getPropertyConfig(creation);
                    if(propertyConfig == null) {
                        throw new UserFailureException("Imaging property config must not be empty!");
                    }
                    ImagingDataSetPropertyConfig config = readConfig(propertyConfig);

                    if(config.getImages() == null || config.getImages().isEmpty()) {
                        throw new UserFailureException("At least one image must be included!");
                    }
                    int count = 0;
                    for(ImagingDataSetImage image : config.getImages()) {
                        if(image.getPreviews() == null || image.getPreviews().isEmpty()) {
                            throw new UserFailureException("At least one preview must be included!");
                        }
                        count += image.getPreviews().size();
                    }
                    if(creation.getMetaData() == null) {
                        creation.setMetaData(new HashMap<>());
                    }
                    creation.getMetaData().put(PREVIEW_TOTAL_COUNT, Integer.toString(count));

                }


            }



        }
        else if(operation instanceof UpdateDataSetsOperation) {
            UpdateDataSetsOperation updateDataSetsOperation = (UpdateDataSetsOperation) operation;

            for(DataSetUpdate update : updateDataSetsOperation.getUpdates()) {

                DataSet dataSet = getDataSetToUpdate(update, api, sessionToken);
                if (dataSet != null)
                {
                    EntityTypePermId typeId = dataSet.getType().getPermId();
                    if(isImagingDataSet(typeId.getPermId())) {

                        String propertyConfig = getPropertyConfig(update);
                        if(propertyConfig == null) {
                            throw new UserFailureException("Imaging property config must not be empty!");
                        }
                        ImagingDataSetPropertyConfig config = readConfig(propertyConfig);

                        if(config.getImages() == null || config.getImages().isEmpty()) {
                            throw new UserFailureException("At least one image must be included!");
                        }
                        int count = 0;
                        for(ImagingDataSetImage image : config.getImages()) {
                            if(image.getPreviews() == null || image.getPreviews().isEmpty()) {
                                throw new UserFailureException("At least one preview must be included!");
                            }
                            count += image.getPreviews().size();
                        }
                        update.getMetaData().put(PREVIEW_TOTAL_COUNT, Integer.toString(count));
                    }
                }



            }
        }
    }

    @Override
    public void setup(Properties properties)
    {
        //Unused
    }

    @Override
    public void afterOperation(IApplicationServerApi api, String sessionToken, IOperation operation,
            IOperationResult result, RuntimeException runtimeException)
    {
        //Unused
    }


}
