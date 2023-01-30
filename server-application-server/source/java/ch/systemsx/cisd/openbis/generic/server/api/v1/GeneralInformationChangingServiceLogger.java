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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
class GeneralInformationChangingServiceLogger extends AbstractServerLogger implements
        IGeneralInformationChangingService
{

    public GeneralInformationChangingServiceLogger(ISessionManager<Session> sessionManagerNull,
            IInvocationLoggerContext context)
    {
        super(sessionManagerNull, context);
    }

    @Override
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties)
    {
        logTracking(sessionToken, "update-sample-properties", "SAMPLE(%s)", sampleID);
    }

    @Override
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        logTracking(sessionToken, "add_unofficial_vocabulary_term",
                "ID(%s) CODE(%s), LABEL(%s), DESCRIPTION(%s), PREVIOUS_ORDINAL(%s)", vocabularyId,
                code, label, description, Long.toString(previousTermOrdinal));
    }

    @Override
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term)
    {
        logTracking(sessionToken, "add-unofficial-vocabulary-term", "VOCABULARY_ID(%s) TERM(%s)",
                vocabularyId, term);

    }

    @Override
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId)
    {
        logAccess(sessionToken, "get-custom-display-settings", "WEB_APP_ID(%s)", webAppId);
        return null;
    }

    @Override
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings)
    {
        logAccess(sessionToken, "set-custom-display-settings", "WEB_APP_ID(%s)",
                webAppSettings.getWebAppId());
    }

    @Override
    public Metaproject createMetaproject(String sessionToken, String name, String description)
    {
        logAccess(sessionToken, "createMetaproject", "NAME(%s) DESCRIPTION(%s)", name, description);
        return null;
    }

    @Override
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            String name, String descriptionOrNull)
    {
        logAccess(sessionToken, "updateMetaproject", "METAPROJECT_ID(%s) NAME(%s) DESCRIPTION(%s)",
                metaprojectId, name, descriptionOrNull);
        return null;
    }

    @Override
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "deleteMetaproject", "METAPROJECT_ID(%s)", metaprojectId);
    }

    @Override
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd)
    {
        MetaprojectAssignmentsIds assignments =
                assignmentsToAdd != null ? assignmentsToAdd : new MetaprojectAssignmentsIds();

        logAccess(sessionToken, "addToMetaproject",
                "METAPROJECT_ID(%s), EXPERIMENTS(%s), SAMPLES(%s), DATA_SETS(%s), MATERIALS(%s)",
                metaprojectId, abbreviate(assignments.getExperiments()),
                abbreviate(assignments.getSamples()), abbreviate(assignments.getDataSets()),
                abbreviate(assignments.getMaterials()));
    }

    @Override
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove)
    {
        MetaprojectAssignmentsIds assignments =
                assignmentsToRemove != null ? assignmentsToRemove : new MetaprojectAssignmentsIds();

        logAccess(sessionToken, "removeFromMetaproject",
                "METAPROJECT_ID(%s), EXPERIMENTS(%s), SAMPLES(%s), DATA_SETS(%s), MATERIALS(%s)",
                metaprojectId, abbreviate(assignments.getExperiments()),
                abbreviate(assignments.getSamples()), abbreviate(assignments.getDataSets()),
                abbreviate(assignments.getMaterials()));
    }

    @Override
    public String registerSamplesWithSilentOverrides(String sessionToken, String sampleTypeCode, String spaceIdentifierSilentOverrideOrNull,
            String experimentIdentifierSilentOverrideOrNull, String sessionKey,
            String defaultGroupIdentifier)
    {
        logAccess(sessionToken, "registerSamplesWithSilentOverrides",
                "sampleTypeCode(%s), sessionKey(%s), spaceIdentifierSilentOverrideOrNull(%s), experimentIdentifierSilentOverrideOrNull(%s), defaultGroupIdentifier(%s)",
                sampleTypeCode,
                spaceIdentifierSilentOverrideOrNull,
                experimentIdentifierSilentOverrideOrNull,
                sessionKey,
                defaultGroupIdentifier);

        return null;
    }

    @Override
    public String registerSamples(String sessionToken, String sampleTypeCode, String sessionKey, String defaultGroupIdentifier)
    {
        logAccess(sessionToken, "registerSamples",
                "sampleTypeCode(%s), sessionKey(%s), defaultGroupIdentifier(%s)",
                sampleTypeCode,
                sessionKey,
                defaultGroupIdentifier);

        return null;
    }

    @Override
    public String updateSamplesWithSilentOverrides(String sessionToken, String sampleTypeCode, String spaceIdentifierSilentOverrideOrNull,
            String experimentIdentifierSilentOverrideOrNull, String sessionKey,
            String defaultGroupIdentifier)
    {
        logAccess(sessionToken, "updateSamplesWithSilentOverrides",
                "sampleTypeCode(%s), sessionKey(%s), spaceIdentifierSilentOverrideOrNull(%s), experimentIdentifierSilentOverrideOrNull(%s), defaultGroupIdentifier(%s)",
                sampleTypeCode,
                spaceIdentifierSilentOverrideOrNull,
                experimentIdentifierSilentOverrideOrNull,
                sessionKey,
                defaultGroupIdentifier);

        return null;
    }

    @Override
    public String updateSamples(String sessionToken, String sampleTypeCode, String sessionKey, String defaultGroupIdentifier)
    {
        logAccess(sessionToken, "updateSamples",
                "sampleTypeCode(%s), sessionKey(%s), defaultGroupIdentifier(%s)",
                sampleTypeCode,
                sessionKey,
                defaultGroupIdentifier);

        return null;
    }

    @Override
    public Map<String, Object> uploadedSamplesInfo(String sessionToken, String sampleTypeCode, String sessionKey)
    {
        logAccess(sessionToken, "uploadedSamplesInfo",
                "sampleTypeCode(%s), sessionKey(%s)",
                sampleTypeCode,
                sessionKey);

        return null;
    }

    @Override
    public void deleteProjects(String sessionToken, List<Long> projectIds, String reason)
    {
        logAccess(sessionToken, "deleteProjects", "projectIds(%s), reason(%s)", abbreviate(projectIds), reason);
    }

    @Override
    public void deleteExperiments(String sessionToken, List<Long> experimentIds, String reason, DeletionType deletionType)
    {
        logAccess(sessionToken, "deleteExperiments", "experimentIds(%s), reason(%s), deletionType(%s)", abbreviate(experimentIds), reason,
                deletionType);
    }

    @Override
    public void deleteSamples(String sessionToken, List<Long> sampleIds, String reason, DeletionType deletionType)
    {
        logAccess(sessionToken, "deleteSamples", "sampleIds(%s), reason(%s), deletionType(%s)", abbreviate(sampleIds), reason, deletionType);
    }

    @Override
    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason, DeletionType deletionType)
    {
        logAccess(sessionToken, "deleteDataSets", "dataSetCodes(%s), reason(%s), deletionType(%s)", abbreviate(dataSetCodes), reason, deletionType);
    }

    @Override
    public void deleteDataSetsForced(String sessionToken, List<String> dataSetCodes, String reason, DeletionType deletionType)
    {
        logAccess(sessionToken, "deleteDataSetsForced", "dataSetCodes(%s), reason(%s), deletionType(%s)", abbreviate(dataSetCodes), reason,
                deletionType);
    }

    @Override
    public void revertDeletions(String sessionToken, List<Long> deletionIds)
    {
        logAccess(sessionToken, "revertDeletions", "deletionIds(%s)", abbreviate(deletionIds));
    }

    @Override
    public void deletePermanently(String sessionToken, List<Long> deletionIds)
    {
        logAccess(sessionToken, "deletePermanently", "deletionIds(%s)", abbreviate(deletionIds));
    }

    @Override
    public void deletePermanentlyForced(String sessionToken, List<Long> deletionIds)
    {
        logAccess(sessionToken, "deletePermanentlyForced", "deletionIds(%s)", abbreviate(deletionIds));
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public void registerPerson(String sessionToken, String userID)
    {
        logAccess(sessionToken, "registerPerson", "userID(%s)", userID);
    }

    @Override
    public void registerSpace(String sessionToken, String spaceCode, String spaceDescription)
    {
        logAccess(sessionToken, "registerSpace", "spaceCode(%s), spaceDescription(%s)", spaceCode, spaceDescription);
    }

    @Override
    public void registerPersonSpaceRole(String sessionToken, String spaceCode, String userID, String roleCode)
    {
        logAccess(sessionToken, "registerPersonSpaceRole", "spaceCode(%s), userID(%s), roleCode(%s)", spaceCode, userID, roleCode);
    }

}
