/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_CHANGING_SERVICE_SERVER)
public class GeneralInformationChangingService extends
        AbstractServer<IGeneralInformationChangingService> implements
        IGeneralInformationChangingService
{
    public static final int MINOR_VERSION = 7;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer server;

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVICE)
    private IGenericClientService genericClientService;

    @Resource(name = "request-context-provider")
    @Private
    public IRequestContextProvider requestContextProvider;

    // Default constructor needed by Spring
    public GeneralInformationChangingService()
    {
    }

    GeneralInformationChangingService(IOpenBisSessionManager sessionManager,
            IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager,
            ICommonServer server)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.server = server;
    }

    @Override
    public IGeneralInformationChangingService createLogger(IInvocationLoggerContext context)
    {
        return new GeneralInformationChangingServiceLogger(sessionManager, context);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties)
    {
        checkSession(sessionToken);

        EntityHelper.updateSampleProperties(server, sessionToken, new TechId(sampleID), properties);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        server.addUnofficialVocabularyTerm(sessionToken, vocabularyId, code, label, description,
                previousTermOrdinal);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term)
    {
        TechId vocabularyTechId = new TechId(vocabularyId);
        server.addUnofficialVocabularyTerm(sessionToken, vocabularyTechId, term.getCode(),
                term.getLabel(), term.getDescription(), term.getPreviousTermOrdinal());
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @SuppressWarnings("deprecation")
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId)
    {
        final Session session = getSession(sessionToken);
        return new WebAppSettings(webAppId, displaySettingsProvider.getWebAppSettings(
                session.getPerson(), webAppId));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings)
    {
        try
        {
            final Session session = getSession(sessionToken);
            final PersonPE person = session.tryGetPerson();
            if (person != null)
            {
                PersonPE attachedPerson = getDAOFactory().getPersonDAO().tryFindPersonByUserId(person.getUserId());
                getDAOFactory().getPersonDAO().lock(attachedPerson);

                displaySettingsProvider.executeActionWithPersonLock(person, new IDelegatedActionWithResult<Object>()
                {
                    @Override public Object execute(final boolean didOperationSucceed)
                    {
                        displaySettingsProvider.replaceWebAppSettings(person, webAppSettings);
                        getDAOFactory().getPersonDAO().updatePerson(person);
                        return null;
                    }
                });
            }
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public Metaproject createMetaproject(String sessionToken, String name, String descriptionOrNull)
    {
        Metaproject registration = new Metaproject();
        registration.setName(name);
        registration.setDescription(descriptionOrNull);
        return server.registerMetaproject(sessionToken, registration);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            String name, String descriptionOrNull)
    {
        Metaproject update = new Metaproject();
        update.setName(name);
        update.setDescription(descriptionOrNull);
        return server.updateMetaproject(sessionToken, Translator.translate(metaprojectId), update);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        server.deleteMetaproject(sessionToken, Translator.translate(metaprojectId), null);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd)
    {
        server.addToMetaproject(sessionToken, Translator.translate(metaprojectId), Translator.translate(assignmentsToAdd));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove)
    {
        server.removeFromMetaproject(sessionToken, Translator.translate(metaprojectId), Translator.translate(assignmentsToRemove));
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType getSampleType(String sampleTypeCode, String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes = server.listSampleTypes(sessionToken);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType = null;
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType auxSampleType : sampleTypes)
        {
            if (auxSampleType.getCode().equals(sampleTypeCode))
            {
                sampleType = auxSampleType;
                break;
            }
        }
        return sampleType;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public final String registerSamplesWithSilentOverrides(
            final String sessionToken,
            final String sampleTypeCode,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final String sessionKey,
            final String defaultGroupIdentifier)
    {
        List<BatchRegistrationResult> results = genericClientService.registerSamplesWithSilentOverrides(
                getSampleType(sampleTypeCode, sessionToken),
                spaceIdentifierSilentOverrideOrNull,
                experimentIdentifierSilentOverrideOrNull,
                sessionKey, false, null,
                defaultGroupIdentifier,
                false);

        return results.get(0).getMessage();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public final String registerSamples(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey,
            final String defaultGroupIdentifier)
    {
        return registerSamplesWithSilentOverrides(sessionToken, sampleTypeCode, null, null, sessionKey, defaultGroupIdentifier);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public final String updateSamplesWithSilentOverrides(
            final String sessionToken,
            final String sampleTypeCode,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final String sessionKey,
            final String defaultGroupIdentifier)
    {
        List<BatchRegistrationResult> results = genericClientService.updateSamplesWithSilentOverrides(
                getSampleType(sampleTypeCode, sessionToken),
                spaceIdentifierSilentOverrideOrNull,
                experimentIdentifierSilentOverrideOrNull,
                sessionKey, false, null,
                defaultGroupIdentifier);

        return results.get(0).getMessage();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public final String updateSamples(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey,
            final String defaultGroupIdentifier)
    {
        return updateSamplesWithSilentOverrides(sessionToken, sampleTypeCode, null, null, sessionKey, defaultGroupIdentifier);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public final Map<String, Object> uploadedSamplesInfo(
            final String sessionToken,
            final String sampleTypeCode,
            final String sessionKey)
    {
        return genericClientService.uploadedSamplesInfo(
                getSampleType(sampleTypeCode, sessionToken),
                sessionKey);
    }

    @Override
    public void deleteProjects(String sessionToken, List<Long> projectIds, String reason)
    {
        server.deleteProjects(sessionToken, TechId.createList(projectIds), reason);
    }

    @Override
    public void deleteExperiments(String sessionToken, List<Long> experimentIds, String reason, DeletionType deletionType)
    {
        server.deleteExperiments(sessionToken, TechId.createList(experimentIds), reason, Translator.translate(deletionType));
    }

    @Override
    public void deleteSamples(String sessionToken, List<Long> sampleIds, String reason, DeletionType deletionType)
    {
        server.deleteSamples(sessionToken, TechId.createList(sampleIds), reason, Translator.translate(deletionType));
    }

    @Override
    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason, DeletionType deletionType)
    {
        server.deleteDataSets(sessionToken, dataSetCodes, reason, Translator.translate(deletionType), false);
    }

    @Override
    public void deleteDataSetsForced(String sessionToken, List<String> dataSetCodes, String reason, DeletionType deletionType)
    {
        server.deleteDataSetsForced(sessionToken, dataSetCodes, reason, Translator.translate(deletionType), false);
    }

    @Override
    public void revertDeletions(String sessionToken, List<Long> deletionIds)
    {
        server.revertDeletions(sessionToken, TechId.createList(deletionIds));
    }

    @Override
    public void deletePermanently(String sessionToken, List<Long> deletionIds)
    {
        server.deletePermanently(sessionToken, TechId.createList(deletionIds), false);
    }

    @Override
    public void deletePermanentlyForced(String sessionToken, List<Long> deletionIds)
    {
        server.deletePermanentlyForced(sessionToken, TechId.createList(deletionIds), false);
    }

    @Override
    public void registerPerson(String sessionToken, String userID)
    {
        server.registerPerson(sessionToken, userID);
    }

    @Override
    public void registerSpace(String sessionToken, String spaceCode, String spaceDescription)
    {
        server.registerSpace(sessionToken, spaceCode, spaceDescription);
    }

    @Override
    public void registerPersonSpaceRole(String sessionToken, String spaceCode, String userID, String roleCode)
    {
        Grantee grantee = Grantee.createPerson(userID);
        SpaceIdentifier spaceIdentifier = new SpaceIdentifier(spaceCode);
        server.registerSpaceRole(sessionToken, RoleCode.valueOf(roleCode), spaceIdentifier, grantee);
    }

}
