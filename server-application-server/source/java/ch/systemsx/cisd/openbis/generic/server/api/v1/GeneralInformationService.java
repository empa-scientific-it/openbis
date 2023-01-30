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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperation;
import ch.systemsx.cisd.openbis.generic.server.IConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.server.api.v1.sort.SampleSearchResultSorter;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentListPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleListPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister.DataSetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister.IDataSetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister.SampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreURLForDataSets;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
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
    public static final int MINOR_VERSION = 32;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory boFactory;

    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Autowired
    private IConcurrentOperationLimiter operationLimiter;

    // Default constructor needed by Spring
    public GeneralInformationService()
    {
    }

    GeneralInformationService(IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IPropertiesBatchManager propertiesBatchManager,
            ICommonServer commonServer, IConcurrentOperationLimiter operationLimiter)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.boFactory = boFactory;
        this.commonServer = commonServer;
        this.operationLimiter = operationLimiter;
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
            if (false == roleSet.isProjectLevel())
            {
                Set<RoleWithHierarchy> roles = roleSet.getRoles();
                Set<Role> translatedRoles = new HashSet<Role>();
                for (RoleWithHierarchy role : roles)
                {
                    if (false == roleSet.isProjectLevel())
                    {
                        translatedRoles.add(Translator.translate(role));
                    }
                }
                namedRoleSets.put(roleSet.name(), translatedRoles);
            }
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
        List<SpacePE> spaces = listSpaces();
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
            if (false == roleAssignment.getRoleWithHierarchy().isProjectLevel())
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
        }
        return roleAssignmentsPerSpace;
    }

    private List<SpacePE> listSpaces()
    {
        IDAOFactory daoFactory = getDAOFactory();
        return daoFactory.getSpaceDAO().listSpaces();
    }

    private void addProjectsTo(SpaceWithProjectsAndRoleAssignments fullSpace, SpacePE space)
    {
        List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects(space);
        for (ProjectPE project : projects)
        {
            fullSpace.add(new Project(project.getId(), project.getPermId(), fullSpace.getCode(),
                    project.getCode(), project.getDescription()));
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        return searchForSamples(sessionToken, searchCriteria,
                EnumSet.of(SampleFetchOption.PROPERTIES));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        Session session = getSession(sessionToken);
        PersonPE user = session.tryGetPerson();
        return searchForSamples(session, session.getUserName(), user, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<Sample> searchForSamplesOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, EnumSet<SampleFetchOption> fetchOptions, String userId)
    {
        Session session = getSession(sessionToken);

        final PersonPE user = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        return searchForSamples(session, userId, user, searchCriteria, fetchOptions);
    }

    private List<Sample> searchForSamples(Session session, String userId, final PersonPE user,
            SearchCriteria searchCriteria, EnumSet<SampleFetchOption> fetchOptions)
    {
        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_SAMPLES, new ConcurrentOperation<List<Sample>>()
            {
                @Override
                public List<Sample> execute()
                {
                    EnumSet<SampleFetchOption> sampleFetchOptions =
                            (fetchOptions != null) ? fetchOptions : EnumSet.noneOf(SampleFetchOption.class);

                    DetailedSearchCriteria detailedSearchCriteria =
                            SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                                    SearchableEntityKind.SAMPLE, searchCriteria);

                    ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister sampleLister =
                            boFactory.createSampleLister(session, user.getId());

                    Collection<Long> sampleIDs =
                            new SampleSearchManager(getDAOFactory().getHibernateSearchDAO(), sampleLister)
                                    .searchForSampleIDs(userId, detailedSearchCriteria);

                    SampleByIdentiferValidator filter = new SampleByIdentiferValidator();
                    filter.init(new AuthorizationDataProvider(getDAOFactory()));
                    List<Sample> results = createSampleLister(user).getSamples(sampleIDs, sampleFetchOptions, filter);


                    return new SampleSearchResultSorter().sort(results, detailedSearchCriteria);
                }
            });
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
        validator.init(new AuthorizationDataProvider(getDAOFactory()));
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleByIdentiferValidator.class)
    public List<Sample> listSamplesForExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class) String experimentIdentifierString)
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
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class) String experimentIdentifierString, String userId)
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
        validator.init(new AuthorizationDataProvider(getDAOFactory()));
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> listDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = SampleListPredicate.class) List<Sample> samples)
    {
        return listDataSets(sessionToken, samples, EnumSet.noneOf(Connections.class));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class) List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, false, false);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperimentsHavingDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class) List<Project> projects, String experimentTypeString)
    {
        return listExperiments(sessionToken, projects, experimentTypeString, false, true);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperimentsHavingSamples(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPredicate.class) List<Project> projects, String experimentTypeString)
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

        List<ProjectIdentifier> projectIdentifiers = new LinkedList<ProjectIdentifier>();
        List<Experiment> experiments = new ArrayList<Experiment>();

        for (Project project : projects)
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifier(project.getSpaceCode(), project.getCode());
            projectIdentifiers.add(projectIdentifier);
        }

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> basicExperiments;

        if (onlyHavingSamples)
        {
            basicExperiments =
                    commonServer.listExperimentsHavingSamples(sessionToken, experimentType,
                            projectIdentifiers);
        } else if (onlyHavingDataSets)
        {
            basicExperiments =
                    commonServer.listExperimentsHavingDataSets(sessionToken, experimentType,
                            projectIdentifiers);
        } else
        {
            basicExperiments =
                    commonServer.listExperiments(sessionToken, experimentType,
                            projectIdentifiers);
        }

        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment basicExperiment : basicExperiments)
        {
            experiments.add(Translator.translate(basicExperiment));
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
        final ArrayList<Experiment> experiments = new ArrayList<Experiment>(allExperiments.size());

        final ExperimentByIdentiferValidator validator = new ExperimentByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(getDAOFactory()));

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> listDataSetsForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SamplePredicate.class) Sample sample, boolean areOnlyDirectlyConnectedIncluded)
    {
        checkSession(sessionToken);
        List<AbstractExternalData> externalData =
                commonServer.listSampleExternalData(sessionToken, new TechId(sample.getId()),
                        areOnlyDirectlyConnectedIncluded);
        return Translator.translate(externalData, EnumSet.noneOf(Connections.class));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<DataStore> listDataStores(String sessionToken)
    {
        return Translator.translateDataStores(commonServer.listDataStores(sessionToken));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        return commonServer.getDefaultPutDataStoreBaseURL(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
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
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<DataStoreURLForDataSets> getDataStoreBaseURLs(String sessionToken,
            List<String> dataSetCodes)
    {
        Session session = getSession(sessionToken);

        final IDataSetLister lister = new DataSetLister(getDAOFactory(), session.tryGetPerson());
        return lister.getDataStoreDownloadURLs(dataSetCodes);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<SampleType> listSampleTypes(String sessionToken)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes =
                commonServer.listSampleTypes(sessionToken);
        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                getVocabularyTermsMap(sessionToken);
        List<SampleType> result = new ArrayList<SampleType>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType : sampleTypes)
        {
            result.add(Translator.translate(sampleType, vocabTerms));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType> listExperimentTypes(
            String sessionToken)
    {
        List<ExperimentType> experimentTypes = commonServer.listExperimentTypes(sessionToken);
        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                getVocabularyTermsMap(sessionToken);
        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType> result =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType>();
        for (ExperimentType experimentType : experimentTypes)
        {
            result.add(Translator.translate(experimentType, vocabTerms));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> listDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = SampleListPredicate.class) List<Sample> samples, EnumSet<Connections> connections)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType> sampleTypes =
                commonServer.listSampleTypes(sessionToken);
        SampleToDataSetRelatedEntitiesTranslator translator =
                new SampleToDataSetRelatedEntitiesTranslator(sampleTypes, samples);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<AbstractExternalData> dataSets =
                commonServer.listRelatedDataSets(sessionToken, dsre, true);
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

        List<AbstractExternalData> dataSets =
                commonServer.listRelatedDataSetsOnBehalfOfUser(sessionToken, dsre, true, userId);

        final List<DataSet> unfilteredDatasets = Translator.translate(dataSets, connectionsToGet);

        // Filter for user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final ArrayList<DataSet> datasets = new ArrayList<DataSet>(unfilteredDatasets.size());

        final DataSetByExperimentOrSampleIdentifierValidator validator =
                new DataSetByExperimentOrSampleIdentifierValidator();
        validator.init(new AuthorizationDataProvider(getDAOFactory()));

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> listDataSetsForExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentListPredicate.class) List<Experiment> experiments, EnumSet<Connections> connections)
    {
        checkSession(sessionToken);
        EnumSet<Connections> connectionsToGet =
                (connections != null) ? connections : EnumSet.noneOf(Connections.class);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType> experimentTypes =
                commonServer.listExperimentTypes(sessionToken);
        ExperimentToDataSetRelatedEntitiesTranslator translator =
                new ExperimentToDataSetRelatedEntitiesTranslator(experimentTypes, experiments);
        DataSetRelatedEntities dsre = translator.convertToDataSetRelatedEntities();
        List<AbstractExternalData> dataSets =
                commonServer.listRelatedDataSets(sessionToken, dsre, true);
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
        List<AbstractExternalData> dataSets =
                commonServer.listRelatedDataSetsOnBehalfOfUser(sessionToken, dsre, true, userId);

        final List<DataSet> unfilteredDatasets = Translator.translate(dataSets, connectionsToGet);
        // Filter for user
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        final ArrayList<DataSet> datasets = new ArrayList<DataSet>(unfilteredDatasets.size());

        final DataSetByExperimentOrSampleIdentifierValidator validator =
                new DataSetByExperimentOrSampleIdentifierValidator();
        validator.init(new AuthorizationDataProvider(getDAOFactory()));

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> getDataSetMetaData(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes)
    {
        return getDataSetMetaData(getSession(sessionToken), dataSetCodes, true);
    }

    private List<DataSet> getDataSetMetaData(Session session, List<String> dataSetCodes, boolean noContainedDataSets)
    {
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
            AbstractExternalData ds =
                    DataSetTranslator.translate(dataPE, session.getBaseIndexURL(),
                            MetaprojectTranslator.translate(metaprojects),
                            managedPropertyEvaluatorFactory,
                            new SamplePropertyAccessValidator(session, getDAOFactory()));
            if (ds instanceof ContainerDataSet && noContainedDataSets)
            {
                ContainerDataSet cds = (ContainerDataSet) ds;
                cds.getContainedDataSets().clear();
            }
            result.add(Translator.translate(ds, connections, new HashMap<String, DataSet>()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> getDataSetMetaData(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes,
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

        Session session = getSession(sessionToken);
        if (dataSetFetchOptions.isSubsetOf(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS,
                DataSetFetchOption.CHILDREN))
        {
            final IDataSetLister lister = new DataSetLister(getDAOFactory(), session.tryGetPerson());
            return lister.getDataSetMetaData(dataSetCodes, dataSetFetchOptions);
        } else
        {
            List<DataSet> dataSetList = getDataSetMetaData(session, dataSetCodes, false);
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    // There is no @ReturnValueFilter because commonServer.searchForDataSetsWithSequences() does already the filtering.
    public List<SearchDomainSearchResult> searchOnSearchDomain(String sessionToken, String preferredSearchDomainOrNull,
            String searchString, Map<String, String> optionalParametersOrNull)
    {
        checkSession(sessionToken);

        List<SearchDomainSearchResult> result = new ArrayList<SearchDomainSearchResult>();
        List<SearchDomainSearchResultWithFullEntity> list = commonServer.searchOnSearchDomain(sessionToken,
                preferredSearchDomainOrNull, searchString, optionalParametersOrNull);
        for (SearchDomainSearchResultWithFullEntity sequenceSearchResult : list)
        {
            result.add(sequenceSearchResult.getSearchResult());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken)
    {
        checkSession(sessionToken);

        return commonServer.listAvailableSearchDomains(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public List<DataSet> searchForDataSets(String sessionToken, SearchCriteria searchCriteria)
    {
        checkSession(sessionToken);

        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                        SearchableEntityKind.DATA_SET, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData> privateDataSets =
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
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData> privateDataSets =
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
        final DataSetByExperimentOrSampleIdentifierValidator experimentIdentifierValidator =
                new DataSetByExperimentOrSampleIdentifierValidator();

        experimentIdentifierValidator.init(new AuthorizationDataProvider(getDAOFactory()));

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class) List<String> experimentIdentifiers)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
        validator.init(new AuthorizationDataProvider(getDAOFactory()));

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Metaproject> listMetaprojects(String sessionToken)
    {
        return commonServer.listMetaprojects(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<Metaproject> listMetaprojectsOnBehalfOfUser(String sessionToken, String userId)
    {
        return commonServer.listMetaprojectsOnBehalfOfUser(sessionToken, userId);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public MetaprojectAssignments getMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments assignments =
                commonServer.getMetaprojectAssignments(sessionToken, Translator.translate(metaprojectId));
        return translate(assignments);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public MetaprojectAssignments getMetaprojectOnBehalfOfUser(String sessionToken,
            IMetaprojectId metaprojectId, String userId)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments assignments =
                commonServer.getMetaprojectAssignmentsOnBehalfOfUser(sessionToken, Translator.translate(metaprojectId),
                        userId);
        return translate(assignments);
    }

    private MetaprojectAssignments translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments assignments)
    {
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Attachment> listAttachmentsForProject(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdPredicate.class) IProjectId projectId, boolean allVersions)
    {
        Session session = getSession(sessionToken);

        IProjectBO projectBO = boFactory.createProjectBO(session);
        ProjectPE project = projectBO.tryFindByProjectId(projectId);
        if (project == null)
        {
            throw new UserFailureException("No project found for id '" + projectId + "'.");
        }
        return listAttachments(sessionToken, projectId, project, allVersions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Attachment> listAttachmentsForExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdPredicate.class) IExperimentId experimentId, boolean allVersions)
    {
        Session session = getSession(sessionToken);

        IExperimentBO experimentBO = boFactory.createExperimentBO(session);
        ExperimentPE experiment = experimentBO.tryFindByExperimentId(Translator.translate(experimentId));
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for id '" + experimentId + "'.");
        }
        return listAttachments(sessionToken, experimentId, experiment, allVersions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Attachment> listAttachmentsForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleIdPredicate.class) ISampleId sampleId, boolean allVersions)
    {
        Session session = getSession(sessionToken);

        ISampleBO sampleBO = boFactory.createSampleBO(session);
        SamplePE sample = sampleBO.tryFindBySampleId(Translator.translate(sampleId));
        if (sample == null)
        {
            throw new UserFailureException("No sample found for id '" + sampleId + "'.");
        }
        return listAttachments(sessionToken, sampleId, sample, allVersions);
    }

    private List<Attachment> listAttachments(String sessionToken, IObjectId objectId,
            AttachmentHolderPE attachmentHolder, boolean allVersions)
    {
        List<AttachmentPE> attachments =
                getDAOFactory().getAttachmentDAO().listAttachments(attachmentHolder);
        return Translator.translateAttachments(sessionToken, objectId, attachmentHolder,
                attachments, allVersions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public final Map<String, String> getUserDisplaySettings(final String sessionToken)
    {
        String spaceCode = null;
        String projectCode = null;

        PersonPE person = this.getAuthSession(sessionToken).tryGetPerson();

        // Get User space
        if (person != null && person.getHomeSpace() != null)
        {
            spaceCode = person.getHomeSpace().getCode();
        }

        // Get Project from user settings
        if (person != null && person.getDisplaySettings() != null)
        {
            projectCode = person.getDisplaySettings().getDefaultProject();
        }

        // Build Result
        Map<String, String> userSettings = new HashMap<String, String>();
        if (spaceCode != null)
        {
            userSettings.put("spaceCode", spaceCode);
        }
        if (projectCode != null)
        {
            userSettings.put("projectCode", projectCode);
        }
        return userSettings;
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType> listPropertyTypes(String sessionToken, boolean withRelations)
    {
        HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms =
                getVocabularyTermsMap(sessionToken);
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType> basic = commonServer.listPropertyTypes(sessionToken, withRelations);

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType> api =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType>();

        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType propertyType : basic)
        {
            api.add(Translator.translate(propertyType, vocabTerms));
        }
        return api;
    }

    @Override
    @Transactional
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    // this is not a readOnly transaction - uses nextVal()
    public String generateCode(String sessionToken, String prefix, String entityKind)
    {
        checkSession(sessionToken);
        return new EntityCodeGenerator(getDAOFactory()).generateCode(prefix, EntityKind.valueOf(entityKind));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public List<Deletion> listDeletions(String sessionToken, EnumSet<DeletionFetchOption> fetchOptions)
    {
        if (fetchOptions != null && fetchOptions.contains(DeletionFetchOption.ALL_ENTITIES))
        {
            return Translator.translate(commonServer.listDeletions(sessionToken, true));
        } else if (fetchOptions != null && fetchOptions.contains(DeletionFetchOption.ORIGINAL_ENTITIES))
        {
            return Translator.translate(commonServer.listOriginalDeletions(sessionToken));
        } else
        {
            return Translator.translate(commonServer.listDeletions(sessionToken, false));
        }
    }

    @Override
    public List<Person> listPersons(String sessionToken)
    {
        return commonServer.listPersons(sessionToken);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public Long countNumberOfSamplesForType(String sessionToken, String sampleTypeCode)
    {
        org.hibernate.Session currentSession = this.getDAOFactory().getSessionFactory().getCurrentSession();
        SQLQuery querySampleTypeId = currentSession.createSQLQuery("SELECT id from sample_types WHERE code = :sampleTypeCode");
        querySampleTypeId.setParameter("sampleTypeCode", sampleTypeCode);
        int sampleTypeId = ((Number) querySampleTypeId.uniqueResult()).intValue();

        SQLQuery querySampleCount = currentSession.createSQLQuery("SELECT COUNT(*) FROM samples_all WHERE saty_id = :sampleTypeId");
        querySampleCount.setParameter("sampleTypeId", sampleTypeId);
        long sampleCount = ((Number) querySampleCount.uniqueResult()).longValue();

        return sampleCount;
    }
}
