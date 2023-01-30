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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetRegistrationCache;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
public interface IDataBO extends IEntityBusinessObject
{

    /**
     * Returns a data set found by the given id or null if it does not exist. Does not change the state of this object, especially the result of
     * {@link #getData()}.
     */
    DataPE tryFindByDataSetId(final IDataSetId dataSetId);

    /**
     * Returns the data item which has been created by {@link #define(NewExternalData, SamplePE, SourceType)} or null.
     */
    public DataPE tryGetData();

    /**
     * Returns the data item which has been created by {@link #define(NewExternalData, SamplePE, SourceType)}.
     */
    public DataPE getData();

    /**
     * Defines a new external data item directly connected to a sample.
     * <p>
     * After invocation of this method {@link IExperimentBO#save()} should be invoked to store the new external data item in the Data Access Layer.
     */
    public void define(NewExternalData data, SamplePE sample, SourceType sourceType);

    /**
     * Defines a new external data item not directly connected to a sample but with mandatory connection with an experiment.
     * <p>
     * After invocation of this method {@link IExperimentBO#save()} should be invoked to store the new external data item in the Data Access Layer.
     */
    public void define(NewExternalData data, ExperimentPE experiment, SourceType sourceType);

    public void setCache(DataSetRegistrationCache cache);

    public DataSetRegistrationCache getCache();

    /**
     * Changes given data set. Currently allowed changes: properties, sample, parents, components.
     */
    public void update(DataSetUpdatesDTO updates);

    /**
     * Updates status of given data sets.
     * 
     * @throws UserFailureException if a data set does not exist or status couldn't be set.
     */
    public void updateStatuses(List<String> dataSetCodes, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException;

    /**
     * Updates sizes of given data sets (map key: data set code, map value: data set size).
     * 
     * @throws UserFailureException if a data set does not exist or size couldn't be set.
     */
    public void updateSizes(Map<String, Long> sizeMap);

    /**
     * Set the status for the loaded data set to the given new status value if the current status equals an expected value.
     * 
     * @return true if the update is successful, false if the current status is different than <code>oldStatus</code>.
     */
    public boolean compareAndSetDataSetStatus(DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive);

    /**
     * Adds chosen properties to given data set. If given property has been already defined, the value is not updated.
     */
    public void addPropertiesToDataSet(String dataSetCode, List<NewProperty> properties);

    /**
     * Loads the data set item with specified code.
     */
    public void loadByCode(String dataSetCode);

    /**
     * Enrich data set with parents and experiment.
     */
    public void enrichWithParentsAndExperiment();

    /**
     * Enrich data set with children and experiment.
     */
    public void enrichWithChildren();

    /**
     * Enrich data set with virtual children.
     */
    public void enrichWithContainedDataSets();

    /**
     * Enrich data set with properties.
     */
    public void enrichWithProperties();

    /**
     * Changes the value of a managed property.
     */
    public void updateManagedProperty(IManagedProperty managedProperty);

    /**
     * Set the contained data sets. Taken out of the define method to prevent hibernate problems.
     */
    public void setContainedDataSets(ExperimentPE experimentOrNull, SamplePE sampleOrNull, NewContainerDataSet newData);

    /**
     * Indicate that the storage of the external data has been confirmed.
     */
    public void setStorageConfirmed();

    /**
     * @return true if the storage of this dataset has been confirmed or this is not external data
     */
    public boolean isStorageConfirmed();

    /**
     * Set sample and experiment of a specified data set
     */
    public void assignDataSetToSampleAndExperiment(DataPE data, SamplePE sample, ExperimentPE experiment);
}
