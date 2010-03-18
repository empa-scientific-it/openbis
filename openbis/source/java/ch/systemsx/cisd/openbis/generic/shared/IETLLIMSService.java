/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSamplesByPropertyPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SampleValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * <b>LIMS</b> <i>Web Service</i> interface for the <b>ETL</b> (<i>Extract, Transform, Load</i>)
 * server.
 * 
 * @author Christian Ribeaud
 */
public interface IETLLIMSService extends IServer, ISessionProvider
{
    /**
     * Returns the home database instance.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public DatabaseInstance getHomeDatabaseInstance(final String sessionToken);

    /**
     * Registers a Data Store Server for the specified info.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo dataStoreServerInfo);

    /**
     * Returns the specified experiment or <code>null</code> if not found.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public Experiment tryToGetExperiment(
            String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) ExperimentIdentifier experimentIdentifier)
            throws UserFailureException;

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the
     * experiment with properties.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no sample could be found for given <var>sampleIdentifier</var>.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public Sample tryGetSampleWithExperiment(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Returns the SampleType together with assigned property types for specified sample type code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException;

    /**
     * Returns the data set type together with assigned property types for specified data set type
     * code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
            throws UserFailureException;

    /**
     * For given sample {@link TechId} returns the corresponding list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<ExternalData> listDataSetsBySampleID(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) final TechId sampleId,
            final boolean showOnlyDirectlyConnected) throws UserFailureException;

    /**
     * Lists samples using given configuration.
     * 
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class) final ListSampleCriteria criteria);

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample code. If sample has no top sample, its own properties are returned.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Registers experiment.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public long registerExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class) NewExperiment experiment)
            throws UserFailureException;

    /**
     * Registers a new sample.
     * 
     * @return the technical ID of the new sample.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public long registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) final NewSample newSample,
            String userIDOrNull) throws UserFailureException;

    /**
     * Registers the specified data connected to a sample.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>, and
     *            <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void registerDataSet(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier sampleIdentifier,
            final NewExternalData externalData) throws UserFailureException;

    /**
     * Registers the specified data connected to an experiment.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>, and
     *            <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void registerDataSet(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) final ExperimentIdentifier experimentIdentifier,
            final NewExternalData externalData) throws UserFailureException;

    /**
     * Does nothing besides checking that the current user has rights to access the content of the
     * dataset.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public void checkDataSetAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to return the data set specified by its code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ExternalData tryGetDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode)
            throws UserFailureException;

    /**
     * Creates and returns a unique code for a new data set.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public String createDataSetCode(final String sessionToken) throws UserFailureException;

    /**
     * Lists samples codes filtered by specified criteria, see {@link ListSamplesByPropertyCriteria}
     * to see the details.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<Sample> listSamplesByCriteria(
            final String sessionToken,
            @AuthorizationGuard(guardClass = ListSamplesByPropertyPredicate.class) final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException;

    /**
     * Lists data sets belonging to chosen data store.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listDataSets(final String sessionToken,
            String dataStore) throws UserFailureException;

    /**
     * List data sets deleted after specified date.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull);

    /**
     * Adds specified properties of given data set. Properties defined before will not be updated.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void addPropertiesToDataSet(
            String sessionToken,
            List<NewProperty> properties,
            String dataSetCode,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) final SpaceIdentifier identifier)
            throws UserFailureException;

    /**
     * Adds specified properties of given data set. Properties defined before will not be updated.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSetStatus(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode,
            final DataSetArchivizationStatus newStatus) throws UserFailureException;

}
