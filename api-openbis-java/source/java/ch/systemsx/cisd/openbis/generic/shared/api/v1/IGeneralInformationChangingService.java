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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * Service for changing general informations.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGeneralInformationChangingService extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "general-information-changing";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v1";

    /**
     * URL where the service is exposed via JSON interface.
     */
    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     * <p>
     * 
     * @deprecated Because the parameters refer to an internal openBIS class (TechID).
     */
    @Deprecated
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     */
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term);

    /**
     * Returns the persistent settings for a given custom web app.
     * 
     * @param webAppId The id of the custom web app to get the display settings for.
     * @since 1.2
     */
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId);

    /**
     * Sets the persistent settings for a given custom web app.
     * 
     * @param webAppSettings The new display settings
     * @since 1.2
     */
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings);

    /**
     * Creates a new metaproject.
     * 
     * @param name Name of the metaproject
     * @param descriptionOrNull Description of the metaproject
     * @return Newly created metaproject
     * @since 1.3
     */
    public Metaproject createMetaproject(String sessionToken, String name, String descriptionOrNull);

    /**
     * Updates an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject to update
     * @param name New name of the metaproject
     * @param descriptionOrNull New description of the metaproject
     * @return Updated metaproject
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            String name, String descriptionOrNull);

    /**
     * Deletes an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject to delete
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Adds given entities to an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject
     * @param assignmentsToAdd Assignments that should be added to the metaproject
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd);

    /**
     * Removes given entities from an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject
     * @param assignmentsToRemove Assignments that should be removed from the metaproject
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove);

    /**
     * Registers samples parsing a file stored on the HTTP Session.
     * 
     * @param sampleTypeCode Sample type to parse
     * @param spaceIdentifierSilentOverrideOrNull Silently overrides Space identifier if given
     * @param experimentIdentifierSilentOverrideOrNull Silently overrides Experiment identifier if given
     * @param sessionKey key of the file stored on the HTTP Session
     * @param defaultGroupIdentifier
     * @since 1.5
     */
    public String registerSamplesWithSilentOverrides(
            final String sessionToken,
            final String sampleTypeCode,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final String sessionKey,
            final String defaultGroupIdentifier);

    /**
     * Registers samples parsing a file stored on the HTTP Session.
     * 
     * @param sampleTypeCode Sample type to parse
     * @param sessionKey key of the file stored on the HTTP Session
     * @param defaultGroupIdentifier
     * @since 1.5
     */
    public String registerSamples(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey,
            final String defaultGroupIdentifier);

    /**
     * Updates samples parsing a file stored on the HTTP Session.
     * 
     * @param sampleTypeCode Sample type to parse
     * @param sessionKey key of the file stored on the HTTP Session
     * @param defaultGroupIdentifier
     * @since 1.5
     */
    public String updateSamplesWithSilentOverrides(
            final String sessionToken,
            final String sampleTypeCode,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final String sessionKey,
            final String defaultGroupIdentifier);

    /**
     * Updates samples parsing a file stored on the HTTP Session.
     * 
     * @param sampleTypeCode Sample type to parse
     * @param sessionKey key of the file stored on the HTTP Session
     * @param defaultGroupIdentifier
     * @since 1.5
     */
    public String updateSamples(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey,
            final String defaultGroupIdentifier);

    /**
     * Returns information regarding the uploaded file without discarding it.
     * 
     * @param sampleTypeCode Sample type to parse
     * @param sessionKey key of the file stored on the HTTP Session
     * @since 1.5
     */
    public Map<String, Object> uploadedSamplesInfo(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey);

    /**
     * Deletes the specified projects.
     * 
     * @param projectIds Ids of projects to delete
     * @param reason Reason of the deletion
     * @since 1.6
     */
    public void deleteProjects(String sessionToken, List<Long> projectIds, String reason);

    /**
     * Deletes or trashes the specified experiments depending on the chosen deletion type.
     * 
     * @param experimentIds Ids of experiments to delete
     * @param reason Reason of the deletion
     * @param deletionType Type of the deletion
     * @since 1.6
     */
    public void deleteExperiments(String sessionToken, List<Long> experimentIds, String reason,
            DeletionType deletionType);

    /**
     * Deletes or trashes the specified samples depending on the chosen deletion type.
     * 
     * @param sampleIds Ids of samples to delete
     * @param reason Reason of the deletion
     * @param deletionType Type of the deletion
     * @since 1.6
     */
    public void deleteSamples(String sessionToken, List<Long> sampleIds, String reason,
            DeletionType deletionType);

    /**
     * Deletes or trashes the specified data sets depending on the chosen deletion type. This method CANNOT delete data sets with deletion_disallow
     * flag set to true in their type (compare with {@link #deleteDataSetsForced(String, List, String, DeletionType)}.
     * 
     * @param dataSetCodes Codes of data sets to delete
     * @param reason Reason of the deletion
     * @param deletionType Type of the deletion
     * @since 1.6
     */
    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType deletionType);

    /**
     * Deletes or trashes the specified data sets depending on the chosen deletion type. This method CAN delete data sets with deletion_disallow flag
     * set to true in their type but requires special user privileges (compare with {@link #deleteDataSets(String, List, String, DeletionType)}.
     * 
     * @param dataSetCodes Codes of data sets to delete
     * @param reason Reason of the deletion
     * @param deletionType Type of the deletion
     * @since 1.6
     */
    public void deleteDataSetsForced(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType deletionType);

    /**
     * Reverts specified deletions (puts back all entities moved to trash in the deletions).
     * 
     * @param deletionIds Ids of deletions to be reverted
     * @since 1.6
     */
    public void revertDeletions(String sessionToken, List<Long> deletionIds);

    /**
     * Permanently deletes entities moved to trash in specified deletions. This method CANNOT delete data sets with deletion_disallow flag set to true
     * in their type (compare with {@link #deletePermanentlyForced(String, List)})
     * 
     * @param deletionIds Ids of deletions to be deleted permanently
     * @since 1.6
     */
    public void deletePermanently(String sessionToken, List<Long> deletionIds);

    /**
     * Permanently deletes entities moved to trash in specified deletions. It CAN delete data sets with deletion_disallow flag set to true in their
     * type (compare with {@link #deletePermanently(String, List)}).
     * 
     * @param deletionIds Ids of deletions to be deleted permanently
     * @since 1.6
     */
    public void deletePermanentlyForced(String sessionToken, List<Long> deletionIds);

    /**
     * Registers Person, this person should be available in one of the configured login systems to be usable.
     * 
     * @param userID user to be added to the system
     * @since 1.7
     */
    public void registerPerson(String sessionToken, String userID);

    /**
     * Registers a space.
     * 
     * @param spaceCode space code
     * @param spaceDescription space description
     * @since 1.7
     */
    void registerSpace(String sessionToken, String spaceCode, String spaceDescription);

    /**
     * Registers a space role for a given person.
     * 
     * @param spaceCode space code
     * @param userID user id
     * @param roleCode openBIS role
     * @since 1.7
     */
    void registerPersonSpaceRole(String sessionToken, String spaceCode, String userID, String roleCode);

}
