/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * An interface that contains all data access operations on {@link ExternalDataPE}s.
 * 
 * @author Christian Ribeaud
 */
public interface IDataDAO extends IGenericDAO<DataPE>
{
    /**
     * Checks whether the <var>sample</var> has a data set.
     */
    public boolean hasDataSet(final SamplePE sample) throws DataAccessException;

    /**
     * List the {@link DataPE} related to given <var>entity</var>.
     * 
     * @returns list of {@link DataPE}s that are related to given {@link IEntityInformationHolder}.
     */
    public List<DataPE> listRelatedDataSets(final IEntityInformationHolder entity)
            throws DataAccessException;

    /**
     * List the {@link DataPE} for given <var>sample</var>.
     * 
     * @returns list of {@link DataPE}s that are related to given {@link SamplePE}.
     */
    public List<DataPE> listDataSets(final SamplePE sample) throws DataAccessException;

    /**
     * List the {@link DataPE} for given <var>experiment</var>.
     * 
     * @returns list of {@link DataPE}s that are related to given {@link ExperimentPE}.
     */
    public List<DataPE> listDataSets(final ExperimentPE experiment) throws DataAccessException;

    /**
     * Tries to get the data set for the specified code.
     */
    public DataPE tryToFindDataSetByCode(String dataSetCode);

    /**
     * Tries to get the full data set for the specified code with optional locking.
     */
    public DataPE tryToFindFullDataSetByCode(String dataSetCode, boolean withPropertyTypes,
            boolean lockForUpdate);

    /**
     * Tries to get the full data sets for the specified codes with optional locking.
     */
    public List<DataPE> tryToFindFullDataSetsByCodes(Collection<String> dataSetCodes,
            boolean withPropertyTypes, boolean lockForUpdate);

    /**
     * Sets status of datasets with given codes.
     */
    public void updateDataSetStatuses(List<String> dataSetCodes, DataSetArchivingStatus status);

    /**
     * Updates the status and the present in archive flag for given datasets.
     */
    public void updateDataSetStatuses(List<String> dataSetCodes, DataSetArchivingStatus status,
            boolean newPresentInArchive);

    /**
     * Persists the specified data set.
     */
    public void createDataSet(DataPE dataset);

    /**
     * Updates the specified data set.
     */
    public void updateDataSet(DataPE dataset);

    /**
     * Lists external data belongig to given data store.
     */
    public List<DataPE> listExternalData(final DataStorePE dataStore);

    /**
     * @return Unique set of ids of parents of data sets specified by ids.
     *         <p>
     *         NOTE: does not check if specified ids are proper data set ids.
     */
    public Set<TechId> findParentIds(Collection<TechId> dataSetIds);

    public List<DataPE> listByCode(Set<String> values);

    public void updateDataSets(List<DataPE> externalData);

    /** Move given data sets to trash using specified deletion. */
    public int trash(List<TechId> dataSetIds, DeletionPE deletion) throws DataAccessException;

    /** Returns ids of data sets connected with samples specified by given ids. */
    public List<TechId> listDataSetIdsBySampleIds(final Collection<TechId> samples);

    /** Returns ids of data sets connected with experiments specified by given ids. */
    public List<TechId> listDataSetIdsByExperimentIds(final Collection<TechId> samples);

}
