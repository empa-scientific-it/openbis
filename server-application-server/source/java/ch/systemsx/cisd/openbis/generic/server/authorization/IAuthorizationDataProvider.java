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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Interface of providers of data needed for authorization.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthorizationDataProvider
{
    /**
     * Returns the DAO factory.
     */
    public IAuthorizationDAOFactory getDaoFactory();

    /**
     * Returns the authorization configuration.
     */
    public IAuthorizationConfig getAuthorizationConfig();

    /**
     * Returns a list of all data spaces.
     */
    public List<SpacePE> listSpaces();

    /**
     * Returns the space for the given <var>spaceCode</var> or <code>null</code>, if it does not exist.
     */
    public SpacePE tryGetSpace(String spaceCode);

    /**
     * Returns the experiment for the given <var>techId</var>
     * 
     * @return <code>null</code> if no experiment can be found.
     */
    public ExperimentPE tryGetExperimentByTechId(TechId techIds);

    /**
     * Returns experiments for the given collection of <var>techIds</var>
     * 
     * @return Map of found experiments.
     */
    public Map<TechId, ExperimentPE> tryGetExperimentsByTechIds(Collection<TechId> techId);

    /**
     * Returns the experiment for the given <var>permId</var> or <code>null</code>, if it does not exist.
     */
    public ExperimentPE tryGetExperimentByPermId(String permId);

    /**
     * Returns the sample for the given <var>permId</var> or <code>null</code>, if it does not exist.
     */
    public SamplePE tryGetSampleByPermId(String permId);

    /**
     * Returns the sample for the given <var>space</var> and <var>sampleCode</var> or returns <code>null</code>, if it does not exist.
     */
    public SamplePE tryGetSampleBySpaceAndCode(SpacePE space, String sampleCode);

    /**
     * Returns the sample for the given <var>project</var> and <var>sampleCode</var> or returns <code>null</code>, if it does not exist.
     */
    public SamplePE tryGetSampleByProjectAndCode(ProjectPE project, String sampleCode);

    /**
     * Returns samples for the given collection of <var>techIds</var>
     * 
     * @return Map of found samples.
     */
    public Map<TechId, SamplePE> tryGetSamplesByTechIds(Collection<TechId> techIds);

    /**
     * Returns data sets for the given collection of <var>techIds</var>
     * 
     * @return Map of found data sets.
     */
    public Map<TechId, DataPE> tryGetDataSetsByTechIds(Collection<TechId> techIds);

    /**
     * Returns the project of the experiment to which the specified data set belongs.
     * 
     * @return <code>null</code> if no data set found.
     */
    public ProjectPE tryGetProjectForDataSet(String dataSetCode);

    /**
     * Returns the project for the given <var>permId</var>
     * 
     * @return <code>null</code> if no project can be found.
     */
    public ProjectPE tryGetProjectByPermId(PermId permId);

    /**
     * Returns the project for the given <var>techId</var>
     * 
     * @return <code>null</code> if no project can be found.
     */
    public ProjectPE tryGetProjectByTechId(TechId techId);

    /**
     * Returns projects for the given collection of <var>techIds</var>
     * 
     * @return Map of found projects.
     */
    public Map<TechId, ProjectPE> tryGetProjectsByTechIds(Collection<TechId> techIds);

    /**
     * Returns the project for the given <var>permId</var>
     * 
     * @return <code>null</code> if no project can be found.
     */
    public ProjectPE tryGetProjectByIdentifier(ProjectIdentifier identifier);

    /**
     * Returns the information necessary to determine if a user is allowed to access the data sets.
     */
    public Set<DataSetAccessPE> getDatasetCollectionAccessDataByTechIds(List<TechId> dataSetIds, boolean grouped);

    /**
     * Returns the information necessary to determine if a user is allowed to access the data sets.
     */
    public Set<DataSetAccessPE> getDatasetCollectionAccessDataByCodes(List<String> dataSetCodes);

    /**
     * Returns the information necessary to determine if a user is allowed to access the samples.
     */
    public Set<SampleAccessPE> getSampleCollectionAccessDataByTechIds(List<TechId> sampleIds, boolean grouped);

    /**
     * Returns the information necessary to determine if a user is allowed to access the samples.
     */
    public Set<SampleAccessPE> getSampleCollectionAccessDataByPermIds(List<PermId> samplePermIds);

    /**
     * Returns the information necessary to determine if a user is allowed to access the experiments.
     */
    public Set<ExperimentAccessPE> getExperimentCollectionAccessData(List<TechId> experimentIds, boolean grouped);

    /**
     * Returns the information necessary to determine if a user is allowed to delete/revert the data sets.
     */
    public Set<DataSetAccessPE> getDeletedDatasetCollectionAccessData(List<TechId> deletionIds);

    /**
     * Returns the information necessary to determine if a user is allowed to delete/revert the samples.
     */
    public Set<SampleAccessPE> getDeletedSampleCollectionAccessData(List<TechId> deletionIds);

    /**
     * Returns the information necessary to determine if a user is allowed to delete/revert the experiment.
     */
    public Set<ExperimentAccessPE> getDeletedExperimentCollectionAccessData(
            final List<TechId> deletionIds);

    /**
     * Returns the data space of an entity with given <var>entityKind</var> and <var>techId</var>
     * 
     * @return <code>null</code> if entity has no group set.
     */
    public SpacePE tryGetSpace(SpaceOwnerKind entityKind, TechId techId);

    /**
     * Returns a set of distinct spaces owned by the entities of specified type and with specified ids.
     */
    public Set<SpacePE> getDistinctSpacesByEntityIds(SpaceOwnerKind entityKind, List<TechId> techIds);

    /**
     * Returns the sample with given <var>techId</var>.
     */
    public SamplePE getSample(TechId techId);

    /**
     * Returns the filter with given <var>techId</var>
     */
    public GridCustomFilterPE getGridCustomFilter(TechId techId);

    /**
     * Returns the grid custom column with given <var>techId</var>
     */
    public GridCustomColumnPE getGridCustomColumn(TechId techId);

    /**
     * Returns the query with specified ID.
     */
    public QueryPE getQuery(TechId techId);

    /**
     * Fetches all deletions with given tech ids.
     */
    public List<DeletionPE> getDeletions(List<TechId> deletionIds);

    /**
     * Fetches metaproject with given tech id.
     */
    public MetaprojectPE getMetaproject(TechId id);

}
