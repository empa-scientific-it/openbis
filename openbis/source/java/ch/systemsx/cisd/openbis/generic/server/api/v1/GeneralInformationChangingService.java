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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exception.InvalidSessionException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.MetaprojectIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.MetaprojectPredicate;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_CHANGING_SERVICE_SERVER)
public class GeneralInformationChangingService extends
        AbstractServer<IGeneralInformationChangingService> implements
        IGeneralInformationChangingService
{
    public static final int MINOR_VERSION = 3;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer server;

    // Default constructor needed by Spring
    public GeneralInformationChangingService()
    {
    }

    GeneralInformationChangingService(ISessionManager<Session> sessionManager,
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
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties)
    {
        checkSession(sessionToken);

        EntityHelper.updateSampleProperties(server, sessionToken, new TechId(sampleID), properties);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        server.addUnofficialVocabularyTerm(sessionToken, vocabularyId, code, label, description,
                previousTermOrdinal);
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
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
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @SuppressWarnings("deprecation")
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId)
    {
        final Session session = getSession(sessionToken);
        return new WebAppSettings(webAppId, displaySettingsProvider.getWebAppSettings(
                session.getPerson(), webAppId));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings)
    {
        try
        {
            final Session session = getSession(sessionToken);
            PersonPE person = session.tryGetPerson();
            if (person != null)
            {
                synchronized (displaySettingsProvider)
                {
                    displaySettingsProvider.replaceWebAppSettings(person, webAppSettings);
                    getDAOFactory().getPersonDAO().updatePerson(person);
                }
            }
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public Metaproject createMetaproject(String sessionToken, String name, String description)
    {
        Metaproject metaproject = new Metaproject();
        metaproject.setName(name);
        metaproject.setDescription(description);
        return server.registerMetaproject(sessionToken, metaproject);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public Metaproject updateMetaproject(String sessionToken,
            @AuthorizationGuard(guardClass = MetaprojectPredicate.class)
            Metaproject metaproject)
    {
        return server.updateMetaproject(sessionToken, metaproject);
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void deleteMetaproject(String sessionToken,
            @AuthorizationGuard(guardClass = MetaprojectIdPredicate.class)
            Long metaprojectId)
    {
        server.deleteMetaproject(sessionToken, new TechId(metaprojectId));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void addToMetaproject(String sessionToken,
            @AuthorizationGuard(guardClass = MetaprojectIdPredicate.class)
            Long metaprojectId, Collection<Experiment> experiments, Collection<Sample> samples,
            Collection<DataSet> dataSets, Collection<Material> materials)
    {
        server.addToMetaproject(sessionToken, new TechId(metaprojectId),
                extractTechIds(experiments), extractTechIds(samples), extractTechIds(dataSets),
                extractTechIds(materials));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void addToMetaprojectByEntityIds(String sessionToken,
            @AuthorizationGuard(guardClass = MetaprojectIdPredicate.class)
            Long metaprojectId, List<Long> experiments, List<Long> samples, List<Long> dataSets,
            List<Long> materials)
    {
        server.addToMetaproject(sessionToken, new TechId(metaprojectId),
                TechId.createList(experiments), TechId.createList(samples),
                TechId.createList(dataSets), TechId.createList(materials));
    }

    @Override
    @Transactional(readOnly = false)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void removeFromMetaproject(String sessionToken,
            @AuthorizationGuard(guardClass = MetaprojectIdPredicate.class)
            Long metaprojectId, Collection<Experiment> experiments, Collection<Sample> samples,
            Collection<DataSet> dataSets, Collection<Material> materials)
    {
        server.removeFromMetaproject(sessionToken, new TechId(metaprojectId),
                extractTechIds(experiments), extractTechIds(samples), extractTechIds(dataSets),
                extractTechIds(materials));
    }

    private List<TechId> extractTechIds(Collection<? extends IIdHolder> entitiesOrNull)
    {
        if (entitiesOrNull == null)
        {
            return null;
        }

        List<TechId> result = new ArrayList<TechId>(entitiesOrNull.size());
        for (IIdHolder entity : entitiesOrNull)
        {
            result.add(TechId.create(entity));
        }

        return result;
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

}
