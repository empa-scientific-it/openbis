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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The basic subset of the {@link IEncapsulatedOpenBISService}, that requires only service and a sessionToken.
 * 
 * @author Jakub Straszewski
 */
public interface IEncapsulatedBasicOpenBISService
{
    /**
     * Tries to get the space of specified identifier or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Space tryGetSpace(SpaceIdentifier spaceIdentifier) throws UserFailureException;

    /**
     * Tries to get the project of specified identifier or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Project tryGetProject(ProjectIdentifier projectIdentifier) throws UserFailureException;

    /**
     * Returns the data set type together with assigned property types for the specified data set type code.
     */
    @ManagedAuthentication
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode);

    /**
     * Tries to get the data set for the specified data set code, using the ETL server's session token.
     */
    @ManagedAuthentication
    public AbstractExternalData tryGetDataSet(final String dataSetCode) throws UserFailureException;

    /**
     * Tries to get the data set for the specified data set code, using the ETL server's session token. The returned data set contains only basic
     * attributes, and no related entities.
     */
    @ManagedAuthentication
    public AbstractExternalData tryGetThinDataSet(final String dataSetCode) throws UserFailureException;

    /**
     * Tries to get the data set for the specified data set code, using the ETL server's session token.
     * 
     * @return null if the data set is not local to the caller data store.
     */
    @ManagedAuthentication
    public AbstractExternalData tryGetLocalDataSet(final String dataSetCode)
            throws UserFailureException;

    /**
     * {@link IServiceForDataStoreServer#searchForSamples(String, SearchCriteria)}
     */
    @ManagedAuthentication
    public List<Sample> searchForSamples(SearchCriteria searchCriteria);

    /**
     * {@link IServiceForDataStoreServer#searchForExperiments(String, SearchCriteria)}
     */
    @ManagedAuthentication
    public List<Experiment> searchForExperiments(SearchCriteria searchCriteria);

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the experiment with properties.
     * 
     * @return <code>null</code> if no sample could be found for given <var>sampleIdentifier</var>.
     */
    @ManagedAuthentication
    public Sample tryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * {@link IServiceForDataStoreServer#searchForDataSets(String, SearchCriteria)}
     */
    @ManagedAuthentication
    public List<AbstractExternalData> searchForDataSets(SearchCriteria searchCriteria);

    /**
     * {@link IServiceForDataStoreServer#listExperiments(String, ProjectIdentifier)}
     */
    @ManagedAuthentication
    public List<Experiment> listExperiments(ProjectIdentifier projectIdentifier);

    /**
     * Tries to get the experiment of specified identifier or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Experiment tryGetExperiment(ExperimentIdentifier experimentIdentifier)
            throws UserFailureException;

    /**
     * Lists vocabulary terms.
     */
    @ManagedAuthentication
    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException;

    /**
     * Returns a vocabulary with given code
     */
    @ManagedAuthentication
    public Vocabulary tryGetVocabulary(String code);

    /**
     * Lists materials using given criteria.
     * 
     * @return a sorted list of {@link Material}.
     */
    @ManagedAuthentication
    public List<Material> listMaterials(ListMaterialCriteria criteria, boolean withProperties);

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    @ManagedAuthentication
    public Material tryGetMaterial(MaterialIdentifier materialIdentifier);

    /**
     * List property definitions for the given entity type
     */
    @ManagedAuthentication
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForEntityType(
            String code,
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind);

    /**
     * List metaproject for the current user.
     */
    @ManagedAuthentication
    public List<Metaproject> listMetaprojects();

    /**
     * For given (@code name} and current user returns the corresponding {@link Metaproject}
     */
    @ManagedAuthentication
    public Metaproject tryGetMetaproject(String name);

    /**
     * List metaproject assignments for given metaproject and current user.
     */
    @ManagedAuthentication
    public MetaprojectAssignments getMetaprojectAssignments(String name);

    /**
     * List metaproject for the current user, which are assigned to the given entity.
     */
    @ManagedAuthentication
    public List<Metaproject> listMetaprojectsForEntity(IObjectId entityId);

    /**
     * List metaproject for the current user, which are assigned to the given entities.
     */
    @ManagedAuthentication
    public Map<IObjectId, List<Metaproject>> listMetaprojectsForEntities(Collection<? extends IObjectId> entityIds);

    /**
     * Tries to get the project of specified permId or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Project tryGetProjectByPermId(String permId)
            throws UserFailureException;

    /**
     * Tries to get the experiment of specified permId or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Experiment tryGetExperimentByPermId(String permId)
            throws UserFailureException;

    /**
     * Tries to get the sample of specified permId or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Sample tryGetSampleByPermId(String permId)
            throws UserFailureException;

    /**
     * Returns the session token used by the service.
     */
    @ManagedAuthentication
    public String getSessionToken();
}
