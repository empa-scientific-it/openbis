/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentListPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleListPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetByExperimentIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister.DataSetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister.IDataSetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.SampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreURLForDataSets;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
public class GeneralInformationService extends AbstractServer<IGeneralInformationService> implements
        IGeneralInformationService
{
    public static final int MINOR_VERSION = 22;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory boFactory;

    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    // Default constructor needed by Spring
    public GeneralInformationService()
    {
    }

    GeneralInformationService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IPropertiesBatchManager propertiesBatchManager,
            ICommonServer commonServer)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.boFactory = boFactory;
        this.commonServer = commonServer;
    }

    @Override
    public IGeneralInformationService createLogger(IInvocationLoggerContext context)
    {
        return new GeneralInformationServiceLogger(sessionManager, context);
    }

    @Override
    @Transactional
    // this is not a readOnly transaction - it can create new users
    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        SessionContextDTO session = tryAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionToken)
    {
        return tryGetSession(sessionToken) != null;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken)
    {
        checkSession(sessionToken);

        Map<String, Set<Role>> namedRoleSets = new LinkedHashMap<String, Set<Role>>();
        RoleWithHierarchy[] values = RoleWithHierarchy.values();
        for (RoleWithHierarchy roleSet : values)
        {
            Set<RoleWithHierarchy> roles = roleSet.getRoles();
            Set<Role> translatedRoles = new HashSet<Role>();
            for (RoleWithHierarchy role : roles)
            {
                translatedRoles.add(Translator.translate(role));
            }
            namedRoleSets.put(roleSet.name(), translatedRoles);
        }
        return namedRoleSets;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SimpleSpaceValidator.class)
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull)
    {
        checkSession(sessionToken);

        Map<String, List<RoleAssignmentPE>> roleAssignmentsPerSpace = getRoleAssignmentsPerSpace();
        List<RoleAssignmentPE> instanceRoleAssignments = roleAssignmentsPerSpace.get(null);
        List<SpacePE> spaces = listSpaces(databaseInstanceCodeOrNull);
        List<SpaceWithProjectsAndRoleAssignments> result =
                new ArrayList<SpaceWithProjectsAndRoleAssignments>();
        for (SpacePE space : spaces)
        {
            SpaceWithProjectsAndRoleAssignments fullSpace =
                    new SpaceWithProjectsAndRoleAssignments(space.getCode());
            addProjectsTo(fullSpace, space);
            addRoles(fullSpace, instanceRoleAssignments);
            List<RoleAssignmentPE> list = roleAssignmentsPerSpace.get(space.getCode());
            if (list != null)
            {
                addRoles(fullSpace, list);
            }
            result.add(fullSpace);
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

    private Map<String, List<RoleAssignmentPE>> getRoleAssignmentsPerSpace()
    {
        List<RoleAssignmentPE> roleAssignments =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        Map<String, List<RoleAssignmentPE>> roleAssignmentsPerSpace =
                new HashMap<String, List<RoleAssignmentPE>>();
        for (RoleAssignmentPE roleAssignment : roleAssignments)
        {
            SpacePE space = roleAssignment.getSpace();
            String spaceCode = space == null ? null : space.getCode();
            List<RoleAssignmentPE> list = roleAssignmentsPerSpace.get(spaceCode);
            if (list == null)
            {
                list = new ArrayList<RoleAssignmentPE>();
                roleAssignmentsPerSpace.put(spaceCode, list);
            }
            list.add(roleAssignment);
        }
        return roleAssignmentsPerSpace;
    }

    private List<SpacePE> listSpaces(String databaseInstanceCodeOrNull)
    {
        IDAOFactory daoFactory = getDAOFactory();
        DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        if (databaseInstanceCodeOrNull != null)
        {
            IDatabaseInstanceDAO databaseInstanceDAO = daoFactory.getDatabaseInstanceDAO();
            databaseInstance =
                    databaseInstanceDAO.tryFindDatabaseInstanceByCode(databaseInstanceCodeOrNull);
        }
        return daoFactory.getSpaceDAO().listSpaces(databaseInstance);
    }

    private void addProjectsTo(SpaceWithProjectsAndRoleAssignments fullSpace, SpacePE space)
    {
        List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects(space);
        for (ProjectPE project : projects)
        {
            fullSpace.add(new Project(project.getId(), project.getPermId(), fullSpace.getCode(),
                    project.getCode()));
        }
    }

    private void addRoles(SpaceWithProjectsAndRoleAssignments fullSpace, List<RoleAssignmentPE> list)
    {
        for (RoleAssignmentPE roleAssignment : list)
        {
            Role role =
                    Translator.translate(roleAssignment.getRole(),
                            roleAssignment.getSpace() != null);
            Set<PersonPE> persons;
            AuthorizationGroupPE authorizationGroup = roleAssignment.getAuthorizationGroup();
            if (authorizationGroup != null)
            {
                persons = authorizationGroup.getPersons();
            } else
            {
                persons = Collections.singleton(roleAssignment.getPerson());
            }
            for (PersonPE person : persons)
            {
                fullSpace.add(person.getUserId(), role);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        return searchForSamples(sessionToken, searchCriteria,
                EnumSet.of(SampleFetchOption.PROPERTIES));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        Session session = getSession(sessionToken);
        EnumSet<SampleFetchOption> sampleFetchOptions =
                (fetchOptions != null) ? fetchOptions : EnumSet.noneOf(SampleFetchOption.class);
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.SAMPLE, searchCriteria);
        ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister sampleLister =
                boFactory.createSampleLister(session);
        Collection<Long> sampleIDs =
                new SampleSearchManager(getDAOFactory().getHibernateSearchDAO(), sampleLister)
                        .searchForSampleIDs(session.getUserName(), detailedSearchCriteria);
        return createSampleLister(session.tryGetPerson()).getSamples(sampleIDs, sampleFetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<Sample> searchForSamplesOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, EnumSet<SampleFetchOption> fetchOptions, String userId)
    {
        Session session = getSession(sessionToken);

        EnumSet<SampleFetchOption> sampleFetchOptions =
                (fetchOptions != null) ? fetchOptions : EnumSet.noneOf(SampleFetchOption.class);
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.SAMPLE, searchCriteria);

        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);

        ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister sampleLister =
                boFactory.createSampleLister(session, person.getId());
        Collection<Long> sampleIDs =
                new SampleSearchManager(getDAOFactory().getHibernateSearchDAO(), sampleLister)
                        .searchForSampleIDs(userId, detailedSearchCriteria);

        final List<Sample> unfilteredSamples =
                createSampleLister(person).getSamples(sampleIDs, sampleFetchOptions);
        return filterSamplesVisibleToUser(sessionToken, unfilteredSamples, userId);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<Sample> filterSamplesVisibleToUser(String sessionToken, List<Sample> allSamples,
            String userId)
    {
        checkSession(sessionToken);

        // filter by user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final SampleByIdentiferValidator validator = new SampleByIdentiferValidator();
        final ArrayList<Sample> samples = new ArrayList<Sample>(allSamples.size());
        for (Sample sample : allSamples)
        {
            if (validator.doValidation(person, sample))
            {
                samples.add(sample);
            }
        }
        return samples;
    }

    protected ISampleLister createSampleLister(PersonPE person)
    {
        return new SampleLister(getDAOFactory(), person);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    public List<Sample> listSamplesForExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class)
            String experimentIdentifierString)
    {
        checkSession(sessionToken);
        ExperimentIdentifier experimentId =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExperiment =
                commonServer.getExperimentInfo(sessionToken, experimentId);

        ListSampleCriteria listSampleCriteria =
                ListSampleCriteria.createForExperiment(new TechId(privateExperiment.getId()));
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> privateSamples =
                commonServer.listSamples(sessionToken, listSampleCriteria);
        return Translator.translateSamples(privateSamples);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<Sample> listSamplesForExperimentOnBehalfOfUser(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class)
            String experimentIdentifierString, String userId)
    {
        checkSession(sessionToken);
        ExperimentIdentifier experimentId =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExperiment =
                commonServer.getExperimentInfo(sessionToken, experimentId);

        ListSampleCriteria listSampleCriteria =
                ListSampleCriteria.createForExperiment(new TechId(privateExperiment.getId()));
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> privateSamples =
                commonServer.listSamplesOnBehalfOfUser(sessionToken, listSampleCriteria, userId);

        final List<Sample> unfilteredSamples = Translator.translateSamples(privateSamples);
        // Filter for user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final SampleByIdentiferValidator validator = new SampleByIdentiferValidator();
        final ArrayList<Sample> samples = new ArrayList<Sample>(unfilteredSamples.size());
        for (Sample sample : unfilteredSamples)
        {
            if (validator.doValidation(person, sample))
            {
                samples.add(sample);
            }
        }
        return samples;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> listDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = SampleListPredicate.class)
            List<Sample> samples)
    {
        return listDataSets(sessionToken, samples, EnumSet.noneOf(Connections.class));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class)
            List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, false, false);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperimentsHavingDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class)
            List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, false, true);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperimentsHavingSamples(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class)
            List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, true, false);
    }

    private List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentTypeString, boolean onlyHavingSamples, boolean onlyHavingDataSets)
    {
        checkSession(sessionToken);

        // Convert the string to an experiment type
        ExperimentType experimentType = null;
        if (experimentTypeString == null || EntityType.ALL_TYPES_CODE.equals(experimentTypeString))
        {
            experimentType = new ExperimentType();
            experimentType.setCode(EntityType.ALL_TYPES_CODE);
        } else
        {
            experimentType = tryFindExperimentType(sessionToken, experimentTypeString);
            if (null == experimentType)
            {
                throw new UserFailureException("Unknown experiment type : " + experimentTypeString);
            }
        }

        // Retrieve the matches for each project
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();

        for (Project project : projects)
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifier(project.getSpaceCode(), project.getCode());

            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> basicExperiments;

            if (onlyHavingSamples)
            {
                basicExperiments =
                        commonServer.listExperimentsHavingSamples(sessionToken, experimentType,
                                projectIdentifier);
            } else if (onlyHavingDataSets)
            {
                basicExperiments =
                        commonServer.listExperimentsHavingDataSets(sessionToken, experimentType,
                                projectIdentifier);
            } else
            {
                basicExperiments =
                        commonServer.listExperiments(sessionToken, experimentType,
                                projectIdentifier);
            }
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment basicExperiment : basicExperiments)
            {
                experiments.add(Translator.translate(basicExperiment));
            }
        }
        return experiments;
    }

    private ExperimentType tryFindExperimentType(String sessionToken, String experimentTypeString)
    {
        List<ExperimentType> experimentTypes = commonServer.listExperimentTypes(sessionToken);
        for (ExperimentType anExperimentType : experimentTypes)
        {
            if (anExperimentType.getCode().equals(experimentTypeString))
            {
                return anExperimentType;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<Experiment> filterExperimentsVisibleToUser(String sessionToken,
            List<Experiment> allExperiments, String userId)
    {
        checkSession(sessionToken);

        // filter by user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final ExperimentByIdentiferValidator validator = new ExperimentByIdentiferValidator();
        final ArrayList<Experiment> experiments = new ArrayList<Experiment>(allExperiments.size());
        for (Experiment experiment : allExperiments)
        {
            if (validator.doValidation(person, experiment))
            {
                experiments.add(experiment);
            }
        }
        return experiments;

    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> listDataSetsForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SamplePredicate.class)
            Sample sample, boolean areOnlyDirectlyConnectedIncluded)
    {
        checkSession(sessionToken);
        List<ExternalData> externalData =
                commonServer.listSampleExternalData(sessionToken, new TechId(sample.getId()),
                        areOnlyDirectlyConnectedIncluded);
        return Translator.translate(externalData, EnumSet.noneOf(Connections.class));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        return commonServer.getDefaultPutDataStoreBaseURL(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode)
    {
        Session session = getSession(sessionToken);

        final IDataSetLister lister = new DataSetLister(getDAOFactory(), session.tryGetPerson());
        final List<DataStoreURLForDataSets> dataStores =
                lister.getDataStoreDownloadURLs(Collections.singletonList(dataSetCode));
        if (dataStores.isEmpty())
        {
            return null;
        }
        return dataStores.get(0).getDataStoreURL();
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<DataStoreURLForDataSets> getDataStoreBaseURLs(String sessionToken,
            List<String> dataSetCodes)
    {
        Session session = getSession(sessionToken);

        final IDataSetLister lister = new DataSetLister(getDAOFactory(), session.tryGetPerson());
        return lister.getDataStoreDownloadURLs(dataSetCodes);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType> privateDataSetTypes =
                commonServer.listDataSetTypes(sessionToken);

        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                getVocabularyTermsMap(sessionToken);

        ArrayList<DataSetType> dataSetTypes = new ArrayList<DataSetType>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType privateDataSetType : privateDataSetTypes)
        {
            dataSetTypes.add(Translator.translate(privateDataSetType, vocabTerms));
        }
        return dataSetTypes;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> getVocabularyTermsMap(
            String sessionToken)
    {
        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                new HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>>();
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary> privateVocabularies =
                commonServer.listVocabularies(sessionToken, true, false);
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary privateVocabulary : privateVocabularies)
        {
            vocabTerms.put(privateVocabulary,
                    Translator.translatePropertyTypeTerms(privateVocabulary.getTerms()));
        }
        return vocabTerms;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Vocabulary> listVocabularies(String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary> privateVocabularies =
                commonServer.listVocabularies(sessionToken, true, false);
        List<Vocabulary> result = new ArrayList<Vocabulary>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary privateVocabulary : privateVocabularies)
        {
            result.add(Translator.translate(privateVocabulary));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> listDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = SampleListPredicate.class)
            List<Sample> samples, EnumSet<Connections> connections)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes =
                commonServer.listSampleTypes(sessionToken);
        SampleToDataSetRelatedEntitiesTranslator translator =
                new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<ExternalData> dataSets = commonServer.listRelatedDataSets(sessionToken, dsre, true);
        return Translator.translate(dataSets, connectionsToGet);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<DataSet> listDataSetsOnBehalfOfUser(String sessionToken, List<Sample> samples,
            EnumSet<Connections> connections, String userId)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes =
                commonServer.listSampleTypes(sessionToken);
        SampleToDataSetRelatedEntitiesTranslator translator =
                new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();

        List<ExternalData> dataSets =
                commonServer.listRelatedDataSetsOnBehalfOfUser(sessionToken, dsre, true, userId);

        final List<DataSet> unfilteredDatasets = Translator.translate(dataSets, connectionsToGet);

        // Filter for user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final DataSetByExperimentIdentifierValidator validator =
                new DataSetByExperimentIdentifierValidator();
        final ArrayList<DataSet> datasets = new ArrayList<DataSet>(unfilteredDatasets.size());
        for (DataSet dataset : unfilteredDatasets)
        {
            if (validator.doValidation(person, dataset))
            {
                datasets.add(dataset);
            }
        }
        return datasets;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> listDataSetsForExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentListPredicate.class)
            List<Experiment> experiments, EnumSet<Connections> connections)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType> experimentTypes =
                commonServer.listExperimentTypes(sessionToken);
        ExperimentToDataSetRelatedEntitiesTranslator translator =
                new ExperimentToDataSetRelatedEntitiesTranslator(experimentTypes, experiments);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<ExternalData> dataSets = commonServer.listRelatedDataSets(sessionToken, dsre, true);
        return Translator.translate(dataSets, connectionsToGet);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<DataSet> listDataSetsForExperimentsOnBehalfOfUser(String sessionToken,
            List<Experiment> experiments, EnumSet<Connections> connections, String userId)
    {

        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType> experimentTypes =
                commonServer.listExperimentTypes(sessionToken);
        ExperimentToDataSetRelatedEntitiesTranslator translator =
                new ExperimentToDataSetRelatedEntitiesTranslator(experimentTypes, experiments);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<ExternalData> dataSets =
                commonServer.listRelatedDataSetsOnBehalfOfUser(sessionToken, dsre, true, userId);

        final List<DataSet> unfilteredDatasets = Translator.translate(dataSets, connectionsToGet);
        // Filter for user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final DataSetByExperimentIdentifierValidator validator =
                new DataSetByExperimentIdentifierValidator();
        final ArrayList<DataSet> datasets = new ArrayList<DataSet>(unfilteredDatasets.size());
        for (DataSet dataset : unfilteredDatasets)
        {
            if (validator.doValidation(person, dataset))
            {
                datasets.add(dataset);
            }
        }
        return datasets;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes)
    {
        Session session = getSession(sessionToken);

        IDataDAO dataDAO = getDAOFactory().getDataDAO();
        List<DataSet> result = new ArrayList<DataSet>();
        EnumSet<Connections> connections = EnumSet.of(Connections.PARENTS, Connections.CHILDREN);
        for (String dataSetCode : dataSetCodes)
        {
            DataPE dataPE = dataDAO.tryToFindDataSetByCode(dataSetCode);
            if (dataPE == null)
            {
                throw new UserFailureException("Unknown data set " + dataSetCode);
            }
            HibernateUtils.initialize(dataPE.getChildRelationships());
            HibernateUtils.initialize(dataPE.getProperties());
            Collection<MetaprojectPE> metaprojects =
                    getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                            session.tryGetPerson(), dataPE);
            ExternalData ds =
                    DataSetTranslator.translate(dataPE, session.getBaseIndexURL(),
                            MetaprojectTranslator.translate(metaprojects),
                            managedPropertyEvaluatorFactory);
            result.add(Translator.translate(ds, connections));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes,
            EnumSet<DataSetFetchOption> fetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("SessionToken was null");
        }
        if (dataSetCodes == null)
        {
            throw new IllegalArgumentException("DataSetCodes were null");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("FetchOptions were null");
        }

        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        for (DataSetFetchOption option : fetchOptions)
        {
            dataSetFetchOptions.addOption(option);
        }

        if (dataSetFetchOptions.isSubsetOf(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS,
                DataSetFetchOption.CHILDREN))
        {
            Session session = getSession(sessionToken);
            final IDataSetLister lister =
                    new DataSetLister(getDAOFactory(), session.tryGetPerson());
            return lister.getDataSetMetaData(dataSetCodes, dataSetFetchOptions);
        } else
        {
            List<DataSet> dataSetList = getDataSetMetaData(sessionToken, dataSetCodes);
            if (dataSetList != null)
            {
                for (DataSet dataSet : dataSetList)
                {
                    if (dataSet != null)
                    {
                        dataSet.setFetchOptions(new DataSetFetchOptions(DataSetFetchOption.values()));
                    }
                }
            }
            return dataSetList;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentIdentifierValidator.class)
    public List<DataSet> searchForDataSets(String sessionToken, SearchCriteria searchCriteria)
    {
        checkSession(sessionToken);

        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.DATA_SET, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData> privateDataSets =
                commonServer.searchForDataSets(sessionToken, detailedSearchCriteria);

        // The underlying search, as currently implemented, does not return any of the connections
        return Translator.translate(privateDataSets, EnumSet.noneOf(Connections.class));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<DataSet> searchForDataSetsOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, String userId)
    {
        checkSession(sessionToken);

        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.DATA_SET, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData> privateDataSets =
                commonServer.searchForDataSetsOnBehalfOfUser(sessionToken, detailedSearchCriteria,
                        userId);

        // The underlying search, as currently implemented, does not return any of the connections
        return Translator.translate(privateDataSets, EnumSet.noneOf(Connections.class));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<DataSet> filterDataSetsVisibleToUser(String sessionToken,
            List<DataSet> allDataSets, String userId)
    {
        checkSession(sessionToken);

        // filter by user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final DataSetByExperimentIdentifierValidator experimentIdentifierValidator =
                new DataSetByExperimentIdentifierValidator();

        final ArrayList<DataSet> dataSets = new ArrayList<DataSet>(allDataSets.size());
        for (DataSet dataSet : allDataSets)
        {
            if (experimentIdentifierValidator.doValidation(person, dataSet))
            {
                dataSets.add(dataSet);
            }
        }
        return dataSets;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class)
            List<String> experimentIdentifiers)
    {
        checkSession(sessionToken);

        List<ExperimentIdentifier> parsedIdentifiers =
                ExperimentIdentifierFactory.parse(experimentIdentifiers);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> experiments =
                commonServer.listExperiments(sessionToken, parsedIdentifiers);

        return Translator.translateExperiments(experiments);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> searchForExperiments(String sessionToken, SearchCriteria searchCriteria)
    {
        checkSession(sessionToken);

        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.EXPERIMENT, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> experiments =
                commonServer.searchForExperiments(sessionToken, detailedSearchCriteria);
        return Translator.translateExperiments(experiments);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ProjectByIdentiferValidator.class)
    public List<Project> listProjects(String sessionToken)
    {
        checkSession(sessionToken);

        return Translator.translateProjects(commonServer.listProjects(sessionToken));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<Project> listProjectsOnBehalfOfUser(String sessionToken, String userId)
    {
        final List<Project> unfilteredProjects = listProjects(sessionToken);

        // filter by user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final ProjectByIdentiferValidator validator = new ProjectByIdentiferValidator();
        final ArrayList<Project> projects = new ArrayList<Project>();
        for (Project project : unfilteredProjects)
        {
            if (validator.doValidation(person, project))
            {
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Material> getMaterialByCodes(String sessionToken,
            List<MaterialIdentifier> materialIdentifier)
    {
        // convert api material indetifier into dto material identifier
        Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier> materialCodes =
                CollectionUtils
                        .collect(
                                materialIdentifier,
                                new Transformer<MaterialIdentifier, ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier>()
                                    {
                                        @Override
                                        public ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier transform(
                                                MaterialIdentifier arg0)
                                        {
                                            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier(
                                                    arg0.getMaterialCode(), arg0
                                                            .getMaterialTypeIdentifier()
                                                            .getMaterialTypeCode());
                                        }
                                    });

        ListMaterialCriteria criteria =
                ListMaterialCriteria.createFromMaterialIdentifiers(materialCodes);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materials =
                commonServer.listMaterials(sessionToken, criteria, true);
        return Translator.translateMaterials(materials);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Material> searchForMaterials(String sessionToken, SearchCriteria searchCriteria)
    {
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.MATERIAL, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materials =
                commonServer.searchForMaterials(sessionToken, detailedSearchCriteria);
        return Translator.translateMaterials(materials);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public List<Metaproject> listMetaprojects(String sessionToken)
    {
        return commonServer.listMetaprojects(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public MetaprojectAssignments getMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments assignments =
                commonServer.getMetaprojectAssignments(sessionToken, metaprojectId);

        MetaprojectAssignments result = new MetaprojectAssignments();
        result.setMetaproject(assignments.getMetaproject());
        result.setExperiments(Translator.translateExperiments(assignments.getExperiments()));
        result.setSamples(Translator.translateSamples(assignments.getSamples()));
        result.setDataSets(Translator.translate(assignments.getDataSets(),
                EnumSet.noneOf(Connections.class)));
        result.setMaterials(Translator.translateMaterials(assignments.getMaterials()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Attachment> listAttachmentsForProject(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class)
            ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ProjectIdentifier project,
            boolean allVersions)
    {
        final TechId techId;
        if (project.getDatabaseId() != null)
        {
            techId = new TechId(project.getDatabaseId());
        } else if (project.getPermId() != null)
        {
            final Long id =
                    boFactory.getEntityResolver().tryResolveProjectIdByPermId(
                            project.getPermId());
            if (id == null)
            {
                return Collections.emptyList();
            }
            techId = new TechId(id);
        } else if (project.getCode() != null)
        {
            final Long id =
                    boFactory.getEntityResolver().tryResolveProjectIdByCode(
                            project.getSpaceCode(), project.getCode());
            if (id == null)
            {
                return Collections.emptyList();
            }
            techId = new TechId(id);
        } else
        {
            throw new IllegalArgumentException("No identifier given.");
        }
        final List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> attachments =
                commonServer.listProjectAttachments(sessionToken, techId);
        return Translator.translateAttachments(attachments, allVersions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Attachment> listAttachmentsForExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class)
            ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentIdentifier experiment,
            boolean allVersions)
    {
        final TechId techId;
        if (experiment.getDatabaseId() != null)
        {
            techId = new TechId(experiment.getDatabaseId());
        } else if (experiment.getPermId() != null)
        {
            final Long id =
                    boFactory.getEntityResolver().tryResolveExperimentIdByPermId(
                            experiment.getPermId());
            if (id == null)
            {
                return Collections.emptyList();
            }
            techId = new TechId(id);
        } else if (experiment.getCode() != null)
        {
            final Long id =
                    boFactory.getEntityResolver().tryResolveExperimentIdByCode(
                            experiment.getSpaceCode(), experiment.getProjectCode(),
                            experiment.getCode());
            if (id == null)
            {
                return Collections.emptyList();
            }
            techId = new TechId(id);
        } else
        {
            throw new IllegalArgumentException("No identifier given.");
        }
        final List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> attachments =
                commonServer.listExperimentAttachments(sessionToken, techId);
        return Translator.translateAttachments(attachments, allVersions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Attachment> listAttachmentsForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleIdentifierPredicate.class)
            SampleIdentifier sample,
            boolean allVersions)
    {
        final TechId techId;
        if (sample.getDatabaseId() != null)
        {
            techId = new TechId(sample.getDatabaseId());
        } else if (sample.getPermId() != null)
        {
            final Long id =
                    boFactory.getEntityResolver().tryResolveSampleIdByPermId(
                            sample.getPermId());
            if (id == null)
            {
                return Collections.emptyList();
            }
            techId = new TechId(id);
        } else if (sample.getCode() != null)
        {
            final Long id =
                    boFactory.getEntityResolver().tryResolveSampleIdByCode(
                            sample.getSpaceCode(), sample.getCode());
            if (id == null)
            {
                return Collections.emptyList();
            }
            techId = new TechId(id);
        } else
        {
            throw new IllegalArgumentException("No identifier given.");
        }
        final List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> attachments =
                commonServer.listSampleAttachments(sessionToken, techId);
        return Translator.translateAttachments(attachments, allVersions);
    }
}
