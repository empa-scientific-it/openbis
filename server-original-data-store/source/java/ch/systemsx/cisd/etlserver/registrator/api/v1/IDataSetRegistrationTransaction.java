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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

import java.io.File;

import net.lemnik.eodsql.DynamicTransactionQuery;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISpaceImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;

/**
 * Interface for a data set registration transaction. All actions that go through the transaction are committed atomically or rolledback.
 * <p>
 * The working directory for a file operations is the incoming data set folder (or incoming directory if the data set is a simple file). Non-absolute
 * paths are resolved relative to the working directory.
 * <p>
 * New data sets are expected to have exactly one file or folder at the top level. When registered, it is this file or folder that is put into the
 * store.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetRegistrationTransaction
{
    // Entity Retrieval/Creation

    /**
     * Create a new data set for registration in openBIS.
     */
    IDataSet createNewDataSet();

    /**
     * Create a new data set with the specified code.
     */
    IDataSet createNewDataSet(String dataSetType);

    IDataSet createNewDataSet(String dataSetType, DataSetKind datasetKindOrNull);

    /**
     * Create a new data set with the specified type and code.
     */
    IDataSet createNewDataSet(String dataSetType, String dataSetCode);

    IDataSet createNewDataSet(String dataSetType, String dataSetCode, DataSetKind datasetKindOrNull);

    /**
     * Get a data set from the openBIS AS. Returns null if the data set does not exist.
     * 
     * @return A data set or null
     */
    IDataSetImmutable getDataSet(String dataSetCode);

    /**
     * Get a data set from the openBIS AS for the purpose of modifying it. Returns null if the data set does not exist.
     * 
     * @return A data set or null
     */
    IDataSetUpdatable getDataSetForUpdate(String dataSetCode);

    /**
     * Given an immutable data set, make it mutable.
     * 
     * @return A data set
     */
    IDataSetUpdatable makeDataSetMutable(IDataSetImmutable dataSet);

    /**
     * Get a sample from the openBIS AS. Returns null if the sample does not exist.
     * 
     * @return A sample or null
     */
    ISampleImmutable getSample(String sampleIdentifierString);

    /**
     * Get a sample from the openBIS AS for the purpose of modifying it. Returns null if the sample does not exist.
     * 
     * @return A sample or null
     */
    ISample getSampleForUpdate(String sampleIdentifierString);

    /**
     * Given an immutable sample, make it mutable.
     * 
     * @return A sample
     */
    ISample makeSampleMutable(ISampleImmutable sample);

    /**
     * Create a new sample to register with the openBIS AS. The sample will have a permId.
     * 
     * @param sampleIdentifierString The identifier for the new sample
     * @param sampleTypeCode The code of the type for the new sample
     */
    ISample createNewSample(String sampleIdentifierString, String sampleTypeCode);

    /**
     * Create a new sample to register with the openBIS AS. The sample will have a permId and automatically generated identifier
     * 
     * @param spaceCode The space in which to create the new sample.
     * @param sampleTypeCode The code of the type for the new sample
     */
    ISample createNewSampleWithGeneratedCode(String spaceCode, String sampleTypeCode);

    /**
     * Get an experiment from the openBIS AS.
     */
    IExperimentImmutable getExperiment(String experimentIdentifierString);

    /**
     * Get an experiment from the openBIS AS for the purpose of modifying it.
     */
    IExperimentUpdatable getExperimentForUpdate(String experimentIdentifierString);

    /**
     * Given an immutable experiment, make it mutable.
     * 
     * @return An experiment
     */
    IExperimentUpdatable makeExperimentMutable(IExperimentImmutable experiment);

    /**
     * Create a new experiment to register with the openBIS AS. The experiment will have a permId.
     * 
     * @param experimentIdentifierString The identifier for the new experiment
     * @param experimentTypeCode The code of the type for the new experiment
     */
    IExperiment createNewExperiment(String experimentIdentifierString, String experimentTypeCode);

    /**
     * Create a new project to register with the openBIS AS.
     * 
     * @param projectIdentifier .
     */
    IProject createNewProject(String projectIdentifier);

    /**
     * Get a project from the openBIS AS. Returns null if the project does not exist.
     * 
     * @return A project or null
     */
    IProjectImmutable getProject(String projectIdentifier);

    /**
     * Get an project from the openBIS AS for the purpose of modifying it.
     */
    IProject getProjectForUpdate(String projectIdentifierString);

    /**
     * Given an immutable project, make it mutable.
     * 
     * @return A mutable project.
     */
    IProject makeProjectMutable(IProjectImmutable project);

    /**
     * Create a new space to register with the openBIS AS.
     * 
     * @param spaceCode the code of the space
     * @param spaceAdminUserIdOrNull the user id of the person, who will receive space admin priviliges.
     */
    ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull);

    /**
     * Get a space from the openBIS AS. Returns null if the space does not exist.
     * 
     * @return A space or null
     */
    ISpaceImmutable getSpace(String spaceCode);

    /**
     * Get a material from the openBIS AS. Returns null if the material does not exist.
     * 
     * @return A material or null
     */
    IMaterialImmutable getMaterial(String materialCode, String materialType);

    /**
     * Get a material from the openBIS AS. Returns null if the material does not exist.
     * 
     * @return A material or null
     */
    IMaterialImmutable getMaterial(String identifier);

    /**
     * Get a material from the openBIS AS for the purpose of modifying it. Returns null if the material does not exist.
     * 
     * @return A material or null
     */
    IMaterial getMaterialForUpdate(String materialCode, String materialType);

    /**
     * Get a material from the openBIS AS for the purpose of modifying it. Returns null if the material does not exist.
     * 
     * @return A material or null
     */
    IMaterial getMaterialForUpdate(String identifier);

    /**
     * Given an immutable material, make it mutable.
     * 
     * @return A material
     */
    IMaterial makeMaterialMutable(IMaterialImmutable material);

    /**
     * Create a new material to register with the openBIS AS.
     * 
     * @param materialCode the code of the material
     * @param materialType the type of the material
     */
    IMaterial createNewMaterial(String materialCode, String materialType);

    /**
     * Get an external data management system from the openBIS AS. Returns null if the object does not exist.
     * 
     * @return external data management system or null
     */
    IExternalDataManagementSystemImmutable getExternalDataManagementSystem(
            String externalDataManagementSystemCode);

    /**
     * Creates the new metaproject for the current user. Only allowed when there is a user available.
     */
    IMetaproject createNewMetaproject(String name, String description);

    /**
     * Creates the new metaproject for the specified user. Only allowed when there is no user available.
     */
    IMetaproject createNewMetaproject(String name, String description, String ownerId);

    /**
     * Only allowed when the user is available.
     * 
     * @return metaproject with given name for current user.
     */
    IMetaproject getMetaproject(String name);

    /**
     * Only allowed when the user is not available.
     * 
     * @return metaproject with given name for specified user.
     */
    IMetaproject getMetaproject(String name, String ownerId);

    /**
     * Get the read-only vocabulary with given code
     * 
     * @returns null if the vocabulary is not found
     */
    IVocabularyImmutable getVocabulary(String code);

    /**
     * Get the vocabulary with given code
     * 
     * @returns null if the vocabulary is not found
     */
    IVocabulary getVocabularyForUpdate(String code);

    /**
     * Creates a new vocabulary term, which has to be assigned to a {@link IVocabulary}.
     */
    IVocabularyTerm createNewVocabularyTerm();

    // File operations -- The source and destination paths are local to the incoming data set folder
    // or incoming directory if the data set is just one file

    /**
     * Move a file from into the root of a data set.
     * 
     * @param src The path of the file to move.
     * @param dst The data set to add the file to.
     * @return The absolute path after the move.
     */
    String moveFile(String src, IDataSet dst);

    /**
     * Move a file to a specified location in a data set. Any necessary intermediate folders are automatically created.
     * 
     * @param src The path of the file to move.
     * @param dst The data set to add the file to.
     * @param dstInDataset The path of the file in the data set
     * @return The absolute path after the move.
     */
    String moveFile(String src, IDataSet dst, String dstInDataset);

    /**
     * Create a new directory and return the path.
     * 
     * @param dst The data set to add the file to.
     * @param dirName The name of the new file to create. (Can be a simple file or directory.)
     * @return The absolute path of the new file.
     */
    String createNewDirectory(IDataSet dst, String dirName);

    /**
     * Create a new empty file and return the path.
     * 
     * @param dst The data set to add the file to.
     * @param fileName The name of the new file to create. (Can be a simple file or directory.)
     * @return The absolute path of the new file.
     */
    String createNewFile(IDataSet dst, String fileName);

    /**
     * Create a new empty file and return the path.
     * 
     * @param dst The data set to add the file to.
     * @param dstInDataset The path of the file in the data set
     * @param fileName The name of the new file to create. (Can be a simple file or directory.)
     * @return The absolute path of the new file.
     */
    String createNewFile(IDataSet dst, String dstInDataset, String fileName);

    /**
     * Retrieve the search service for this transaction. If the user is available for this transaction, then the search service results will be
     * filtered for this user.
     * 
     * @return The search service for this transaction.
     */
    ISearchService getSearchService();

    /**
     * Retrieve the search service for this transaction. It returns the results unfiltered by the user, even if the user is available.
     * 
     * @return The search service for this transaction.
     */
    ISearchService getSearchServiceUnfiltered();

    /**
     * Retrieve the search service for this transaction. The search service results will be filtered for the specified user.
     * 
     * @return The search service for this transaction.
     */
    ISearchService getSearchServiceFilteredForUser(String userId);

    /**
     * @return A service which can be used to get authorization information about a user.
     */
    IAuthorizationService getAuthorizationService();

    /**
     * Gets a database query object for the data source with the specified name.
     * <p>
     * After the rest of the transaction is committed, the queries are committed. Failures in these secondary queries are not fatal, but they are
     * caught and the clients of the transaction are notified.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the service.properties file.
     * @return The query.
     * @throw IllegalArgumentException Thrown if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match the given parameters.
     */
    DynamicTransactionQuery getDatabaseQuery(String dataSourceName) throws IllegalArgumentException;

    /**
     * Return a registration context object which can be used to store information that needs to be accessed through the registration process.
     * <p>
     * It is important to use the registration context, and not global variables, for communication between code in different parts of the
     * registration process. This is because the registration process is not guaranteed to run in a single process.
     * 
     * @return The context, a hash-map-like object.
     */
    DataSetRegistrationContext getRegistrationContext();

    /**
     * Returns the service registrator context.
     * 
     * @deprecated Should not be used. To get to the global state use getGlobalState instead
     */
    @Deprecated
    OmniscientTopLevelDataSetRegistratorState getRegistratorContext();

    /**
     * @return Global state for this dropbox, including configuration properties specified by the user.
     */
    TopLevelDataSetRegistratorGlobalState getGlobalState();

    /**
     * @return The logical incoming file.
     */
    File getIncoming();

    /**
     * Get the id of the user on whose behalf this registration transaction is performed.
     * 
     * @return A userId or null, if there is none.
     */
    String getUserId();

    /**
     * Set the id of the user on whose behalf this registration transaction is performed.
     * 
     * @param userIdOrNull The id of a user or null if this transaction should be performed as the system (etlserver).
     */
    void setUserId(String userIdOrNull);

}
