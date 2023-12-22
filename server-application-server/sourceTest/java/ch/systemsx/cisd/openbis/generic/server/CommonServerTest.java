/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.Description;
import org.hamcrest.core.IsEqual;
import org.hibernate.SessionFactory;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.DynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.EntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.ExperimentTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.ScriptPEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.ICommonPropertyBasedHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IManagedPropertyHotDeployEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SpaceTranslator;

/**
 * Test cases for corresponding {@link CommonServer} class.
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = RoleAssignmentPE.class)
public final class CommonServerTest extends AbstractServerTestCase
{

    private static final String EXAMPLE_REASON = "reason";

    private final static String BASE_INDEX_URL = "baseIndexURL";

    private ICommonBusinessObjectFactory commonBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    private IEntityValidatorFactory entityValidatorFactory;

    private IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private IManagedPropertyHotDeployEvaluator managedPropertyHotDeployEvaluator;

    private IEntityValidatorHotDeployPlugin entityValidatorHotDeployPlugin;

    private IHotDeploymentController hotDeploymentController;

    private IDataStoreServiceFactory dssFactory;

    private IDataStoreService dataStoreService;

    private SessionFactory hibernateSessionFactory;

    private org.hibernate.Session hibernateSession;

    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    private IConcurrentOperationLimiter operationLimiter;

    private final CommonServer createServer()
    {
        CommonServer server =
                createServer(new EntityValidatorFactory(null, new TestJythonEvaluatorPool()),
                        new DynamicPropertyCalculatorFactory(null, new TestJythonEvaluatorPool()),
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()));
        server.setDssFactory(dssFactory);
        return server;
    }

    public CommonServer createServer(IEntityValidatorFactory entityValidatorFactory2,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory2,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory2)
    {
        CommonServer server =
                new CommonServer(authenticationService, sessionManager, daoFactory,
                        propertiesBatchManager, commonBusinessObjectFactory,
                        dataStoreServiceRegistrator, new LastModificationState(),
                        entityValidatorFactory2, dynamicPropertyCalculatorFactory2,
                        managedPropertyEvaluatorFactory2, operationLimiter);
        server.setSampleTypeSlaveServerPlugin(sampleTypeSlaveServerPlugin);
        server.setDataSetTypeSlaveServerPlugin(dataSetTypeSlaveServerPlugin);
        server.setBaseIndexURL(SESSION_TOKEN, BASE_INDEX_URL);
        server.setDisplaySettingsProvider(new DisplaySettingsProvider());
        server.setSessionWorkspaceProvider(sessionWorkspaceProvider);
        server.setCacheManager(cacheManager);
        return server;
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        dssFactory = context.mock(IDataStoreServiceFactory.class);
        dataStoreService = context.mock(IDataStoreService.class);
        commonBusinessObjectFactory = context.mock(ICommonBusinessObjectFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        dataStoreServiceRegistrator = context.mock(IDataStoreServiceRegistrator.class);
        managedPropertyEvaluatorFactory = context.mock(IManagedPropertyEvaluatorFactory.class);
        managedPropertyHotDeployEvaluator = context.mock(IManagedPropertyHotDeployEvaluator.class);
        dynamicPropertyCalculatorFactory = context.mock(IDynamicPropertyCalculatorFactory.class);
        entityValidatorFactory = context.mock(IEntityValidatorFactory.class);
        entityValidatorHotDeployPlugin = context.mock(IEntityValidatorHotDeployPlugin.class);
        hotDeploymentController = context.mock(IHotDeploymentController.class);
        hibernateSessionFactory = context.mock(SessionFactory.class);
        hibernateSession = context.mock(org.hibernate.Session.class);
        sessionWorkspaceProvider = context.mock(ISessionWorkspaceProvider.class);
        operationLimiter = context.mock(IConcurrentOperationLimiter.class);
    }

    @Test
    public void testGetTemplateColumns()
    {
        prepareGetSession();
        final String type = "MY_TYPE";
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type);
                    ExperimentTypePEBuilder builder = new ExperimentTypePEBuilder();
                    builder.assign("NON-MANAGED-PROP");
                    builder.assign("DYNAMIC-PROP").script(ScriptType.DYNAMIC_PROPERTY, "");
                    builder.assign("MANAGED-PROP-NO-SUBCOLUMNS").script(
                            ScriptType.MANAGED_PROPERTY, "");
                    builder.assign("MANAGED-PROP-SUBCOLUMNS").script(
                            ScriptType.MANAGED_PROPERTY,
                            "def batchColumnNames():\n  return ['A', 'B']\n"
                                    + "def updateFromBatchInput():\n  None");
                    will(returnValue(builder.getExperimentTypePE()));
                }
            });

        String template =
                createServer().getTemplateColumns(SESSION_TOKEN, EntityKind.EXPERIMENT, type,
                        "tsv", false, false, false, BatchOperationKind.REGISTRATION);

        assertEquals(
                "# Besides the full identifier of format '/SPACE_CODE/PROJECT_CODE/EXPERIMENT_CODE', two short formats 'EXPERIMENT_CODE' and 'PROJECT_CODE/EXPERIMENT_CODE' are accepted given that the default project (former short format) or default space (latter short format) are configured. If the proper default value is not configured when using a short format, experiment import will fail.\nidentifier\tNON-MANAGED-PROP\tMANAGED-PROP-NO-SUBCOLUMNS\t"
                        + "MANAGED-PROP-SUBCOLUMNS:A\tMANAGED-PROP-SUBCOLUMNS:B",
                template);
        context.assertIsSatisfied();
    }

    @Test
    public void testLogout()
    {
        final Session mySession = createSession(CommonTestUtils.USER_ID);
        MessageChannel messageChannel = new MessageChannel();
        context.checking(new Expectations()
            {
                {
                    allowing(cacheManager);

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(mySession));
                    one(sessionManager).closeSession(SESSION_TOKEN);

                    one(dataStoreDAO).listDataStores();
                    DataStorePE dataStore = new DataStorePE();
                    dataStore.setRemoteUrl("remote-url");
                    will(returnValue(Arrays.asList(dataStore)));

                    one(dssFactory).createMonitored(dataStore.getRemoteUrl(), LogLevel.WARN);
                    will(returnValue(dataStoreService));

                    one(dataStoreService).cleanupSession(SESSION_TOKEN);
                    will(new Action()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                messageChannel.send(mySession);
                                return null;
                            }

                            @Override
                            public void describeTo(Description arg0)
                            {
                            }
                        });

                    one(sessionWorkspaceProvider).deleteSessionWorkspace(SESSION_TOKEN);
                }
            });

        createServer().logout(SESSION_TOKEN);

        messageChannel.assertNextMessage(mySession);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticateWhichFailed()
    {
        final String user = "user";
        final String password = "password";
        final Session mySession = createSession(user);
        context.checking(new Expectations()
            {
                {
                    // Artefact of our test code
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(mySession));

                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(null));
                }
            });

        assertEquals(null, createServer().tryAuthenticate(user, password));

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session mySession = createSession(user);
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(mySession));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson))); // only 'system' in database

                    allowing(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).lock(null);

                    one(personDAO).tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
                    will(returnValue(systemPerson));

                    person.setDisplaySettings(systemPerson.getDisplaySettings());

                    one(personDAO).createPerson(with(new PersonWithDisplaySettingsMatcher(person)));

                    // assign instance admin role
                    final RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
                    roleAssignmentPE.setRegistrator(systemPerson);
                    roleAssignmentPE.setRole(RoleCode.ADMIN);
                    person.addRoleAssignment(roleAssignmentPE);

                    one(roleAssignmentDAO).createRoleAssignment(with(roleAssignmentPE));
                }
            });

        final SessionContextDTO s = createServer().tryAuthenticate(user, password);

        assertEquals(person.getUserId(), s.getUserName());
        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticateButNotFirstUser()
    {
        final String user = "user";
        final String password = "password";
        final Session mySession = createSession(user);
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(mySession));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    allowing(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).lock(null);

                    one(personDAO).tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
                    will(returnValue(systemPerson));

                    person.setDisplaySettings(systemPerson.getDisplaySettings());
                    one(personDAO).createPerson(with(new PersonWithDisplaySettingsMatcher(person)));
                }
            });

        try
        {
            createServer().tryAuthenticate(user, password);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("User 'test' has no role assignments and thus is not permitted to login.", e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session mySession = createSession(user);
        final PersonPE person = createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(mySession));

                    allowing(personDAO).tryFindPersonByUserId(user);
                    will(returnValue(person));

                    one(personDAO).lock(person);
                }
            });
        assertEquals(null, mySession.tryGetPerson());

        final SessionContextDTO s = createServer().tryAuthenticate(user, password);

        assertEquals(person.getUserId(), s.getUserName());
        context.assertIsSatisfied();
    }

    @Test
    public void testListGroups()
    {
        final PersonPE person = createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL);
        final SpacePE g1 = CommonTestUtils.createSpace("g1");
        final SpacePE g2 = CommonTestUtils.createSpace("g2");
        final Session mySession = createSession(CommonTestUtils.USER_ID);
        mySession.setPerson(person);
        person.setHomeSpace(g1);
        g1.setId(42L);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(mySession));
                    one(groupDAO).listSpaces();
                    will(returnValue(Arrays.asList(g1, g2)));
                }
            });

        final List<Space> groups = createServer().listSpaces(SESSION_TOKEN);

        assertEquals(SpaceTranslator.translate(g1), groups.get(0));
        assertEquals(SpaceTranslator.translate(g2), groups.get(1));
        assertEquals(2, groups.size());
        assertEquals(true, g1.isHome().booleanValue());
        assertEquals(false, g2.isHome().booleanValue());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterSpace()
    {
        prepareGetSession();
        final String spaceCode = "group";
        final String description = "description";
        session.setPerson(createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL));
        final PersonPE person = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
        RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(RoleCode.OBSERVER);
        person.addRoleAssignment(roleAssignment);
        final RecordingMatcher<NewRoleAssignment> assignmentMatcher =
                new RecordingMatcher<NewRoleAssignment>();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createSpaceBO(session);
                    will(returnValue(spaceBO));

                    one(spaceBO).define(spaceCode, description);
                    one(spaceBO).save();

                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(person));

                    one(commonBusinessObjectFactory).createRoleAssignmentTable(session);
                    will(returnValue(roleAssignmentTable));

                    one(roleAssignmentTable).add(with(assignmentMatcher));
                    one(roleAssignmentTable).save();

                }
            });

        createServer().registerSpace(SESSION_TOKEN, spaceCode, description);

        assertEquals("PERSON:test=ADMIN@/group", assignmentMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterInstanceRoleAssignment()
    {
        prepareGetSession();
        final RecordingMatcher<NewRoleAssignment> assignmentMatcher =
                new RecordingMatcher<NewRoleAssignment>();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createRoleAssignmentTable(session);
                    will(returnValue(roleAssignmentTable));

                    one(roleAssignmentTable).add(with(assignmentMatcher));
                    one(roleAssignmentTable).save();
                }
            });

        createServer().registerInstanceRole(SESSION_TOKEN, RoleCode.ADMIN,
                Grantee.createPerson(CommonTestUtils.USER_ID));

        assertEquals("PERSON:test=ADMIN@", assignmentMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteSpaceRole()
    {
        prepareGetSession();
        final Grantee person = Grantee.createPerson(CommonTestUtils.USER_ID);
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).tryFindSpaceRoleAssignment(RoleCode.USER, "S42", person);
                    RoleAssignmentPE assignment = createRoleAssignment();
                    will(returnValue(assignment));

                    one(roleAssignmentDAO).deleteRoleAssignment(assignment);

                }
            });

        createServer().deleteSpaceRole(SESSION_TOKEN, RoleCode.USER, new SpaceIdentifier("S42"),
                person);

        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteInstanceRole()
    {
        prepareGetSession();
        final Grantee person = Grantee.createPerson(CommonTestUtils.USER_ID);
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).tryFindInstanceRoleAssignment(RoleCode.USER, person);
                    RoleAssignmentPE assignment = createRoleAssignment();
                    will(returnValue(assignment));

                    one(roleAssignmentDAO).deleteRoleAssignment(assignment);
                }
            });

        createServer().deleteInstanceRole(SESSION_TOKEN, RoleCode.USER, person);

        context.assertIsSatisfied();
    }

    private RoleAssignmentPE createRoleAssignment()
    {
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(RoleCode.USER);
        assignment.setPersonInternal(new PersonPE());
        return assignment;
    }

    @Test
    public void testListPersons()
    {
        final PersonPE personPE = createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL);
        final Person person = PersonTranslator.translate(personPE);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(personPE)));
                }
            });

        final List<Person> persons = createServer().listPersons(SESSION_TOKEN);

        assertEquals(person.getUserId(), persons.get(0).getUserId());
        assertEquals(person.getFirstName(), persons.get(0).getFirstName());
        assertEquals(person.getLastName(), persons.get(0).getLastName());
        assertEquals(person.getEmail(), persons.get(0).getEmail());
        assertEquals(person.getDatabaseInstance(), persons.get(0).getDatabaseInstance());
        assertEquals(1, persons.size());

        // Check that strings are not being escaped
        assertEquals(personPE.getFirstName(), person.getFirstName());
        assertEquals(personPE.getLastName(), person.getLastName());
        assertEquals(personPE.getEmail(), person.getEmail());
        assertEquals(personPE.getUserId(), person.getUserId());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterPerson()
    {
        prepareGetSession();
        prepareRegisterPerson();

        createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);

        context.assertIsSatisfied();
    }

    public final static class PersonWithDisplaySettingsMatcher extends IsEqual<PersonPE>
    {

        private final PersonPE item;

        public PersonWithDisplaySettingsMatcher(PersonPE item)
        {
            super(item);
            this.item = item;
        }

        @Override
        public boolean matches(Object arg)
        {
            if (super.matches(arg) == false)
            {
                return false;
            }
            final PersonPE that = (PersonPE) arg;
            return item.getDisplaySettings().equals(that.getDisplaySettings());
        }

    }

    @Test
    public void testRegisterExistingPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {

                    one(personDAO).listByCodes(Arrays.asList(CommonTestUtils.USER_ID));
                    will(returnValue(Arrays
                            .asList(createPersonWithRoleAssignmentsFromPrincipal(PRINCIPAL))));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Following persons already exist: [" + CommonTestUtils.USER_ID + "]",
                    e.getMessage());
        }

        context.assertIsSatisfied();
    }

    private PersonPE createPersonWithRoleAssignmentsFromPrincipal(Principal principal)
    {
        PersonPE person = CommonTestUtils.createPersonFromPrincipal(principal);
        setRoleAssignments(person);
        return person;
    }

    private void setRoleAssignments(PersonPE person)
    {
        // users without any roles cannot login
        Set<RoleAssignmentPE> rolesAssignments = new HashSet<RoleAssignmentPE>();
        RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(RoleCode.ADMIN);
        rolesAssignments.add(roleAssignment);
        person.setRoleAssignments(rolesAssignments);
    }

    @Test
    public void testRegisterUnknownPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listByCodes(Arrays.asList(CommonTestUtils.USER_ID));
                    will(returnValue(new ArrayList<PersonPE>()));

                    final PersonPE systemPerson = createSystemUser();
                    one(personDAO).tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
                    will(returnValue(systemPerson));

                    one(authenticationService).getPrincipal(CommonTestUtils.USER_ID);
                    will(throwException(new IllegalArgumentException()));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, CommonTestUtils.USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Following persons unknown by the authentication service: ["
                    + CommonTestUtils.USER_ID + "]", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListRoles()
    {
        prepareGetSession();
        final RoleAssignmentPE rolePE = new RoleAssignmentPE();
        rolePE.setRole(RoleCode.ETL_SERVER);
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    will(returnValue(Arrays.asList(rolePE)));
                }
            });

        final List<RoleAssignment> roles = createServer().listRoleAssignments(SESSION_TOKEN);

        assertEquals(RoleWithHierarchy.INSTANCE_ETL_SERVER, roles.get(0).getRoleSetCode());
        assertEquals(1, roles.size());

        context.assertIsSatisfied();
    }

    @Test
    public final void testListSampleExternalData()
    {
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("FFT");
        externalDataPE.setFileFormatType(fileFormatType);
        final LocatorTypePE locatorType = new LocatorTypePE();
        locatorType.setCode("LT");
        externalDataPE.setLocatorType(locatorType);
        final DataStorePE dataStorePE = new DataStorePE();
        dataStorePE.setCode("DST");
        externalDataPE.setDataStore(dataStorePE);
        final AbstractExternalData externalData =
                DataSetTranslator.translate(externalDataPE, BASE_INDEX_URL, null,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null);
        prepareGetSession();
        final boolean showOnlyDirectlyConnected = true;
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(datasetLister).listBySampleTechId(sampleId, showOnlyDirectlyConnected);
                    will(returnValue(Arrays.asList(externalData)));
                }
            });

        final List<AbstractExternalData> list =
                createServer().listSampleExternalData(SESSION_TOKEN, sampleId,
                        showOnlyDirectlyConnected);

        assertEquals(1, list.size());
        assertTrue(equals(externalData, list.get(0)));

        context.assertIsSatisfied();
    }

    @Test
    public void testListExperimentExternalData()
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("FFT");
        externalDataPE.setFileFormatType(fileFormatType);
        final LocatorTypePE locatorType = new LocatorTypePE();
        locatorType.setCode("LT");
        externalDataPE.setLocatorType(locatorType);
        final DataStorePE dataStorePE = new DataStorePE();
        dataStorePE.setCode("DST");
        externalDataPE.setDataStore(dataStorePE);
        final AbstractExternalData externalData =
                DataSetTranslator.translate(externalDataPE, BASE_INDEX_URL, null,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    one(datasetLister).listByExperimentTechId(experimentId, true);
                    will(returnValue(Arrays.asList(externalData)));
                }
            });

        final List<AbstractExternalData> list =
                createServer().listExperimentExternalData(SESSION_TOKEN, experimentId, true);

        assertEquals(1, list.size());
        assertTrue(equals(externalData, list.get(0)));

        context.assertIsSatisfied();
    }

    private boolean equals(AbstractExternalData data1, AbstractExternalData data2)
    {
        return EqualsBuilder.reflectionEquals(data1, data2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListExperiments()
    {
        final ProjectIdentifier projectIdentifier = CommonTestUtils.createProjectIdentifier();
        final ExperimentType experimentType =
                ExperimentTranslator.translate(CommonTestUtils.createExperimentType(),
                        new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExperimentTable(session);
                    will(returnValue(experimentTable));

                    one(experimentTable).load(experimentType.getCode(),
                            Collections.singletonList(projectIdentifier), false, false);

                    one(experimentTable).getExperiments();
                    will(returnValue(new ArrayList<ExperimentPE>()));

                    one(metaprojectDAO)
                            .listMetaprojectAssignmentsForEntities(
                                    with(any(PersonPE.class)),
                                    with(any(Collection.class)),
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.class)));
                }
            });
        createServer().listExperiments(SESSION_TOKEN, experimentType, projectIdentifier);
        context.assertIsSatisfied();
    }

    @Test
    public void testListExperimentTypes()
    {
        prepareGetSession();
        final List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        final ExperimentTypePE experimentTypePE = CommonTestUtils.createExperimentType();
        final ExperimentType experimentType =
                ExperimentTranslator.translate(experimentTypePE, new HashMap<MaterialTypePE, MaterialType>(),
                        new HashMap<PropertyTypePE, PropertyType>());
        types.add(experimentTypePE);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(types));
                }
            });
        final List<ExperimentType> experimentTypes =
                createServer().listExperimentTypes(SESSION_TOKEN);
        assertEquals(1, experimentTypes.size());
        assertEquals(experimentType, experimentTypes.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListProjects()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).listProjects();
                    will(returnValue(new ArrayList<ProjectPE>()));
                }
            });
        createServer().listProjects(SESSION_TOKEN);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPropertyTypes()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createPropertyTypeTable(session);
                    will(returnValue(propertyTypeTable));

                    one(propertyTypeTable).loadWithRelations();

                    one(propertyTypeTable).getPropertyTypes();
                    will(returnValue(new ArrayList<PropertyTypePE>()));
                }
            });
        createServer().listPropertyTypes(SESSION_TOKEN, true);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListDataTypes()
    {
        prepareGetSession();
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(DataTypeCode.HYPERLINK);
        context.checking(new Expectations()
            {
                {
                    one(propertyTypeDAO).listDataTypes();
                    will(returnValue(Collections.singletonList(dataTypePE)));
                }
            });
        final List<DataType> dataTypes = createServer().listDataTypes(SESSION_TOKEN);
        assertEquals(1, dataTypes.size());
        assertEquals(DataTypeCode.HYPERLINK, dataTypes.get(0).getCode());
        context.assertIsSatisfied();
    }

    @Test
    public final void testFileFormatTypes()
    {
        prepareGetSession();
        final FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("FFT");
        final List<FileFormatTypePE> list = Collections.singletonList(fileFormatTypePE);
        context.checking(new Expectations()
            {
                {

                    one(fileFormatDAO).listFileFormatTypes();
                    will(returnValue(list));
                }
            });

        List<FileFormatType> types = createServer().listFileFormatTypes(SESSION_TOKEN);

        assertEquals(1, types.size());
        assertEquals(fileFormatTypePE.getCode(), types.get(0).getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileFormatsWithNoCodesSpecified() throws Exception
    {
        List<String> codes = new ArrayList<String>();
        prepareGetSession();
        createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileFormatsWithCodesSpecified() throws Exception
    {
        final List<String> codes = Arrays.asList("code1", "code2");
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    for (String code : codes)
                    {
                        one(fileFormatDAO).tryToFindFileFormatTypeByCode(code);
                        FileFormatTypePE type = createFileFormatType(code);
                        will(returnValue(type));
                        one(fileFormatDAO).delete(type);
                    }
                }
            });
        createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        context.assertIsSatisfied();
    }

    private static final FileFormatTypePE createFileFormatType(String code)
    {
        FileFormatTypePE result = new FileFormatTypePE();
        result.setCode(code);
        return result;
    }

    @Test
    public void testDeleteUnknownFileFormat() throws Exception
    {
        final String code = "unknown-type";
        final List<String> codes = Arrays.asList(code);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(fileFormatDAO).tryToFindFileFormatTypeByCode(code);
                    will(returnValue(null));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileFormatWithDataSets() throws Exception
    {
        final String code = "used-type";
        final List<String> codes = Arrays.asList(code);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(fileFormatDAO).tryToFindFileFormatTypeByCode(code);
                    FileFormatTypePE type = createFileFormatType(code);
                    will(returnValue(type));

                    one(fileFormatDAO).delete(type);
                    will(throwException(new DataIntegrityViolationException("")));
                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServer().deleteFileFormatTypes(SESSION_TOKEN, codes);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteMaterials()
    {
        final List<TechId> materialIds = Arrays.asList(new TechId(1L), new TechId(2L));
        final String reason = EXAMPLE_REASON;

        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createMaterialTable(session);
                    will(returnValue(materialTable));

                    one(materialTable).deleteByTechIds(materialIds, reason);
                }
            });

        createServer().deleteMaterials(SESSION_TOKEN, materialIds, reason);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterFileFormatType()
    {
        prepareGetSession();
        final FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("my-type");
        fileFormatTypePE.setDescription("my description");
        context.checking(new Expectations()
            {
                {
                    one(fileFormatDAO).createOrUpdate(fileFormatTypePE);
                }
            });
        FileFormatType fileFormatType = new FileFormatType();
        fileFormatType.setCode(fileFormatTypePE.getCode());
        fileFormatType.setDescription(fileFormatTypePE.getDescription());

        createServer().registerFileFormatType(SESSION_TOKEN, fileFormatType);

        context.assertIsSatisfied();
    }

    @Test
    public final void testListVocabularies()
    {
        prepareGetSession();
        final boolean excludeInternal = true;
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));

                    one(vocabularyDAO).listVocabularies(excludeInternal);
                    will(returnValue(Collections.emptyList()));
                }
            });
        final List<Vocabulary> vocabularies =
                createServer().listVocabularies(SESSION_TOKEN, true, excludeInternal);
        assertEquals(0, vocabularies.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListAllScripts()
    {
        prepareGetSession();
        final ScriptPE experimentJythonValidationScript =
                new ScriptPEBuilder().name("s1").available().description("script one")
                        .entityKind(EntityKind.EXPERIMENT).script("blabla")
                        .pluginType(PluginType.JYTHON).scriptType(ScriptType.ENTITY_VALIDATION)
                        .getScript();
        final ScriptPE jythonDynamicPropertiesScript =
                new ScriptPEBuilder().name("s2").pluginType(PluginType.JYTHON)
                        .scriptType(ScriptType.DYNAMIC_PROPERTY).getScript();
        final ScriptPE predeployedManagedPropertiesScript =
                new ScriptPEBuilder().name("s3").pluginType(PluginType.PREDEPLOYED)
                        .scriptType(ScriptType.MANAGED_PROPERTY).getScript();
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).listEntities(null, null);
                    will(returnValue(Arrays.asList(experimentJythonValidationScript,
                            jythonDynamicPropertiesScript, predeployedManagedPropertiesScript)));

                    one(managedPropertyEvaluatorFactory).tryGetPredeployedPluginByName("s3");
                    will(returnValue(managedPropertyHotDeployEvaluator));

                    one(managedPropertyHotDeployEvaluator).getSupportedEntityKinds();
                    will(returnValue(EnumSet.of(
                            ICommonPropertyBasedHotDeployPlugin.EntityKind.DATA_SET,
                            ICommonPropertyBasedHotDeployPlugin.EntityKind.MATERIAL)));
                }
            });

        List<Script> scripts =
                createServer(entityValidatorFactory, dynamicPropertyCalculatorFactory,
                        managedPropertyEvaluatorFactory).listScripts(SESSION_TOKEN, null, null);

        assertEquals("s1", scripts.get(0).getName());
        assertEquals("script one", scripts.get(0).getDescription());
        assertEquals("blabla", scripts.get(0).getScript());
        assertEquals(ScriptType.ENTITY_VALIDATION, scripts.get(0).getScriptType());
        assertEquals(PluginType.JYTHON, scripts.get(0).getPluginType());
        assertEquals("[EXPERIMENT]", Arrays.asList(scripts.get(0).getEntityKind()).toString());
        assertEquals(true, scripts.get(0).isAvailable());
        assertEquals("s2", scripts.get(1).getName());
        assertEquals(ScriptType.DYNAMIC_PROPERTY, scripts.get(1).getScriptType());
        assertEquals(PluginType.JYTHON, scripts.get(1).getPluginType());
        assertEquals(null, scripts.get(1).getEntityKind());
        assertEquals(false, scripts.get(1).isAvailable());
        assertEquals("s3", scripts.get(2).getName());
        assertEquals(ScriptType.MANAGED_PROPERTY, scripts.get(2).getScriptType());
        assertEquals(PluginType.PREDEPLOYED, scripts.get(2).getPluginType());
        assertEquals("[DATA_SET, MATERIAL]", Arrays.asList(scripts.get(2).getEntityKind())
                .toString());
        assertEquals(3, scripts.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListAllSampleValidationScripts()
    {
        prepareGetSession();
        final ScriptPE experimentJythonValidationScript =
                new ScriptPEBuilder().name("s1").available().description("script one")
                        .entityKind(EntityKind.EXPERIMENT).script("blabla")
                        .pluginType(PluginType.JYTHON).scriptType(ScriptType.ENTITY_VALIDATION)
                        .getScript();
        final ScriptPE sampleJythonValidationScript =
                new ScriptPEBuilder().name("s2").entityKind(EntityKind.SAMPLE)
                        .pluginType(PluginType.JYTHON).scriptType(ScriptType.ENTITY_VALIDATION)
                        .getScript();
        final ScriptPE jythonValidationScript =
                new ScriptPEBuilder().name("s3").pluginType(PluginType.JYTHON)
                        .scriptType(ScriptType.ENTITY_VALIDATION).getScript();
        final ScriptPE predeployedValidationScript =
                new ScriptPEBuilder().name("s4").pluginType(PluginType.PREDEPLOYED)
                        .scriptType(ScriptType.ENTITY_VALIDATION).getScript();
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).listEntities(ScriptType.ENTITY_VALIDATION, EntityKind.SAMPLE);
                    will(returnValue(Arrays.asList(experimentJythonValidationScript,
                            sampleJythonValidationScript, jythonValidationScript,
                            predeployedValidationScript)));

                    one(entityValidatorFactory).tryGetPredeployedPluginByName("s4");
                    will(returnValue(entityValidatorHotDeployPlugin));

                    one(entityValidatorHotDeployPlugin).getSupportedEntityKinds();
                    will(returnValue(EnumSet.of(
                            ICommonPropertyBasedHotDeployPlugin.EntityKind.DATA_SET,
                            ICommonPropertyBasedHotDeployPlugin.EntityKind.SAMPLE)));
                }
            });

        List<Script> scripts =
                createServer(entityValidatorFactory, dynamicPropertyCalculatorFactory,
                        managedPropertyEvaluatorFactory).listScripts(SESSION_TOKEN,
                                ScriptType.ENTITY_VALIDATION, EntityKind.SAMPLE);

        assertEquals("s2", scripts.get(0).getName());
        assertEquals(ScriptType.ENTITY_VALIDATION, scripts.get(0).getScriptType());
        assertEquals(PluginType.JYTHON, scripts.get(0).getPluginType());
        assertEquals("[SAMPLE]", Arrays.asList(scripts.get(0).getEntityKind()).toString());
        assertEquals(false, scripts.get(0).isAvailable());
        assertEquals("s3", scripts.get(1).getName());
        assertEquals(ScriptType.ENTITY_VALIDATION, scripts.get(1).getScriptType());
        assertEquals(PluginType.JYTHON, scripts.get(1).getPluginType());
        assertEquals(null, scripts.get(1).getEntityKind());
        assertEquals(false, scripts.get(1).isAvailable());
        assertEquals("s4", scripts.get(2).getName());
        assertEquals(ScriptType.ENTITY_VALIDATION, scripts.get(2).getScriptType());
        assertEquals(PluginType.PREDEPLOYED, scripts.get(2).getPluginType());
        assertEquals("[SAMPLE, DATA_SET]", Arrays.asList(scripts.get(2).getEntityKind()).toString());
        assertEquals(3, scripts.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteScripts()
    {
        prepareGetSession();
        final TechId id1 = new TechId(3);
        final TechId id2 = new TechId(4);
        final ScriptPE jythonValidationScript =
                new ScriptPEBuilder().name("s3").pluginType(PluginType.JYTHON)
                        .scriptType(ScriptType.ENTITY_VALIDATION).getScript();
        final ScriptPE predeployedValidationScript =
                new ScriptPEBuilder().name("s4").pluginType(PluginType.PREDEPLOYED)
                        .scriptType(ScriptType.ENTITY_VALIDATION).getScript();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createScriptBO(session);
                    will(returnValue(scriptBO));

                    one(scriptBO).deleteByTechId(id1);
                    will(returnValue(jythonValidationScript));

                    one(scriptBO).deleteByTechId(id2);
                    will(returnValue(predeployedValidationScript));

                    one(entityValidatorFactory).getHotDeploymentController();
                    will(returnValue(hotDeploymentController));

                    one(hotDeploymentController).disablePlugin("s4");
                }
            });

        createServer(entityValidatorFactory, dynamicPropertyCalculatorFactory,
                managedPropertyEvaluatorFactory).deleteScripts(SESSION_TOKEN,
                        Arrays.asList(id1, id2));

        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterPropertyType()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createPropertyTypeBO(session);
                    will(returnValue(propertyTypeBO));

                    one(propertyTypeBO).define(with(aNonNull(PropertyType.class)));
                    one(propertyTypeBO).save();
                }
            });
        createServer().registerPropertyType(SESSION_TOKEN, new PropertyType());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterVocabulary()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(session);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).define(with(aNonNull(NewVocabulary.class)));
                    one(vocabularyBO).save();
                }
            });
        createServer().registerVocabulary(SESSION_TOKEN, new NewVocabulary());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddVocabularyTerms()
    {
        VocabularyTerm t1 = new VocabularyTerm();
        t1.setCode("aöé");
        VocabularyTerm t2 = new VocabularyTerm();
        t2.setCode("büç");
        final List<VocabularyTerm> terms = Arrays.asList(t1, t2);
        final TechId vocabularyId = CommonTestUtils.TECH_ID;
        final Long previousTermOrdinal = 0L;
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(session);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).loadDataByTechId(vocabularyId);
                    one(vocabularyBO).addNewTerms(terms, previousTermOrdinal);
                    one(vocabularyBO).save();
                }
            });

        createServer().addVocabularyTerms(SESSION_TOKEN, vocabularyId, terms, previousTermOrdinal);

        context.assertIsSatisfied();
    }

    @Test
    public void testAddVocabularyTermsManagedInternally()
    {
        VocabularyTerm t1 = new VocabularyTerm();
        t1.setCode("aöé");
        VocabularyTerm t2 = new VocabularyTerm();
        t2.setCode("büç");
        final List<VocabularyTerm> terms = Arrays.asList(t1, t2);
        final TechId vocabularyId = CommonTestUtils.TECH_ID;
        final Long previousTermOrdinal = 0L;
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(session);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).loadDataByTechId(vocabularyId);
                    one(vocabularyBO).addNewTerms(terms, previousTermOrdinal);
                    one(vocabularyBO).save();
                }
            });

        createServer().addVocabularyTerms(SESSION_TOKEN, vocabularyId, terms, previousTermOrdinal);

        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteVocabularyTerms()
    {
        final TechId vocabularyId = CommonTestUtils.TECH_ID;
        final List<VocabularyTerm> termToBeDeleted = Arrays.asList(new VocabularyTerm());
        final List<VocabularyTermReplacement> termsToBeReplaced =
                Arrays.asList(new VocabularyTermReplacement());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createVocabularyBO(session);
                    will(returnValue(vocabularyBO));

                    one(vocabularyBO).loadDataByTechId(vocabularyId);
                    one(vocabularyBO).delete(termToBeDeleted, termsToBeReplaced);
                    one(vocabularyBO).save();
                }
            });

        createServer().deleteVocabularyTerms(SESSION_TOKEN, vocabularyId, termToBeDeleted,
                termsToBeReplaced);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterMaterialType()
    {
        final MaterialType type = new MaterialType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(session);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerMaterialType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateMaterialType()
    {
        final MaterialType type = new MaterialType();
        type.setCode("my-type");
        type.setDescription("my description");
        type.setModificationDate(new Date(42));
        final MaterialTypePE typePE = new MaterialTypePE();
        typePE.setCode(type.getCode());
        typePE.setModificationDate(type.getModificationDate());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateMaterialType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterSampleType()
    {
        final SampleType type = new SampleType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(session);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerSampleType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateSampleType()
    {
        final SampleType type = new SampleType();
        type.setCode("my-type");
        type.setDescription("my description");
        type.setModificationDate(new Date(42));
        final SampleTypePE typePE = new SampleTypePE();
        typePE.setCode(type.getCode());
        typePE.setModificationDate(type.getModificationDate());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.SAMPLE);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateSampleType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExperimentType()
    {
        final ExperimentType type = new ExperimentType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(session);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();
                }
            });

        createServer().registerExperimentType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateExperimentType()
    {
        final ExperimentType type = new ExperimentType();
        type.setCode("my-type");
        type.setDescription("my description");
        type.setModificationDate(new Date(42));
        final ExperimentTypePE typePE = new ExperimentTypePE();
        typePE.setCode(type.getCode());
        typePE.setModificationDate(type.getModificationDate());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateExperimentType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSetType()
    {
        final DataSetType type = new DataSetType();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypeBO(session);
                    will(returnValue(entityTypeBO));

                    one(entityTypeBO).define(type);
                    one(entityTypeBO).save();

                    one(dataStoreServiceRegistrator).register(type);
                }
            });

        createServer().registerDataSetType(SESSION_TOKEN, type);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateDataSetType()
    {
        final DataSetType type = new DataSetType();
        type.setCode("my-type");
        type.setDescription("my description");
        type.setModificationDate(new Date(42));
        final DataSetTypePE typePE = new DataSetTypePE();
        typePE.setCode(type.getCode());
        typePE.setModificationDate(type.getModificationDate());
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.DATA_SET);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                    will(returnValue(typePE));

                    one(entityTypeDAO).createOrUpdateEntityType(typePE);
                }
            });

        createServer().updateDataSetType(SESSION_TOKEN, type);

        assertEquals(type.getDescription(), typePE.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAssignPropertyType()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        final boolean mandatory = true;
        final String value = "50m";
        final String section = "section 1";
        final NewETPTAssignment newAssignment =
                new NewETPTAssignment(entityKind, propertyTypeCode, entityTypeCode, mandatory,
                        value, section, 1L, false, false, null, false, false);
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(session,
                            DtoConverters.convertEntityKind(entityKind));
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).createAssignment(newAssignment);
                }
            });
        assertEquals(
                "Mandatory property type 'USER.DISTANCE' successfully assigned to experiment type 'ARCHERY'",
                createServer().assignPropertyType(SESSION_TOKEN, newAssignment));
        context.assertIsSatisfied();
    }

    @Test
    public void testUassignPropertyType()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(session,
                            DtoConverters.convertEntityKind(entityKind));
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).loadAssignment(propertyTypeCode, entityTypeCode);
                    one(entityTypePropertyTypeBO).deleteLoadedAssignment();
                }
            });

        createServer().unassignPropertyType(SESSION_TOKEN, entityKind, propertyTypeCode,
                entityTypeCode);

        context.assertIsSatisfied();
    }

    @Test
    public void testCountPropertyTypedEntities()
    {
        prepareGetSession();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createEntityTypePropertyTypeBO(session,
                            DtoConverters.convertEntityKind(entityKind));
                    will(returnValue(entityTypePropertyTypeBO));

                    one(entityTypePropertyTypeBO).countAssignmentValues(propertyTypeCode,
                            entityTypeCode);
                    will(returnValue(1));
                    // TODO 2009-09-10, Piotr Buczek: write BO and DAO tests
                }
            });

        int count =
                createServer().countPropertyTypedEntities(SESSION_TOKEN, entityKind,
                        propertyTypeCode, entityTypeCode);

        assertEquals(1, count);
        context.assertIsSatisfied();
    }

    @Test
    public void testListMaterials()
    {
        prepareGetSession();
        final MaterialType materialType =
                MaterialTypeTranslator.translate(CommonTestUtils.createMaterialType(), null, null);
        final ListMaterialCriteria criteria =
                ListMaterialCriteria.createFromMaterialType(materialType);
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createMaterialLister(session);
                    will(returnValue(materialLister));

                    one(materialLister).list(criteria, true);
                    will(returnValue(new ArrayList<MaterialTypePE>()));
                }
            });
        createServer().listMaterials(SESSION_TOKEN, criteria, true);
        context.assertIsSatisfied();
    }

    @Test
    public void testListMaterialTypes()
    {
        prepareGetSession();
        final List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        types.add(CommonTestUtils.createMaterialType());
        context.checking(new Expectations()
            {
                {
                    one(daoFactory)
                            .getEntityTypeDAO(
                                    ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(types));
                }
            });
        final List<MaterialType> materialTypes = createServer().listMaterialTypes(SESSION_TOKEN);
        assertEquals(1, materialTypes.size());
        assertEquals(types.get(0).getCode(), materialTypes.get(0).getCode());
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPermanentlyDeleteDataSetsWithTrashDisabled()
    {
        final boolean enableTrash = false;
        final String reason = EXAMPLE_REASON;
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("ds1", "ds2", "ds3");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(daoFactory).getTransactionTimestamp();
                    will(returnValue(new Date()));

                    one(dataSetTable).loadByDataSetCodes(dataSetCodes, false, false);
                    one(dataSetTable).getDataSets();
                    DataPE ds1 = createDataSet("ds1", "type1");
                    DataPE ds2 = createDataSet("ds2", "type1");
                    DataPE ds3 = createDataSet("ds3", "type2");
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));

                    one(dataSetTypeSlaveServerPlugin).permanentlyDeleteDataSets(session,
                            Arrays.asList(ds1, ds2), reason, false);
                    one(dataSetTypeSlaveServerPlugin).permanentlyDeleteDataSets(session,
                            Arrays.asList(ds3), reason, false);

                    one(dataSetDAO).listByCode(
                            new HashSet<String>(Arrays.asList(ds1.getCode(), ds2.getCode(),
                                    ds3.getCode())));
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                }
            });

        createServer().deleteDataSets(SESSION_TOKEN, dataSetCodes, reason, DeletionType.PERMANENT,
                enableTrash);

        context.assertIsSatisfied();
    }

    @Test
    public void testPermanentlyDeleteDataSetsWithTrashEnabled()
    {
        final boolean enableTrash = true;
        final String reason = EXAMPLE_REASON;
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("ds1", "ds2", "ds3");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDeletedDataSetTable(session);
                    will(returnValue(deletedDataSetTable));

                    one(daoFactory).getTransactionTimestamp();
                    will(returnValue(new Date()));

                    one(deletedDataSetTable).loadByDataSetCodes(dataSetCodes);
                    one(deletedDataSetTable).permanentlyDeleteLoadedDataSets(reason, false);

                    one(dataSetDAO).listByCode(new HashSet<String>(dataSetCodes));
                    will(returnValue(Arrays.asList()));
                }
            });

        createServer().deleteDataSets(SESSION_TOKEN, dataSetCodes, reason, DeletionType.PERMANENT,
                enableTrash);

        context.assertIsSatisfied();
    }

    @Test
    public void testTrashDataSets()
    {
        final boolean enableTrash = true;
        final String reason = EXAMPLE_REASON;
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("ds1", "ds2", "ds3");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).loadByDataSetCodes(dataSetCodes, false, false);
                    one(dataSetTable).getDataSets();
                    DataPE ds1 = createDataSet("ds1", "type1");
                    ds1.setId(1L);
                    DataPE ds2 = createDataSet("ds2", "type1");
                    ds1.setId(2L);
                    DataPE ds3 = createDataSet("ds3", "type2");
                    ds1.setId(3L);
                    List<DataPE> dataSets = Arrays.asList(ds1, ds2, ds3);
                    will(returnValue(dataSets));

                    one(commonBusinessObjectFactory).createTrashBO(session);
                    will(returnValue(trashBO));
                    one(trashBO).createDeletion(reason);
                    one(trashBO).trashDataSets(TechId.createList(dataSets));

                }
            });

        createServer().deleteDataSets(SESSION_TOKEN, dataSetCodes, reason, DeletionType.TRASH,
                enableTrash);

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSets()
    {
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("a", "b");
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).loadByDataSetCodes(dataSetCodes, true, false);
                    one(dataSetTable).uploadLoadedDataSetsToCIFEX(uploadContext);
                }
            });

        createServer().uploadDataSets(SESSION_TOKEN, dataSetCodes, uploadContext);

        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSets()
    {
        prepareGetSession();
        final boolean removeFromDataStore = true;
        final List<String> dataSetCodes = Arrays.asList("a", "b");
        final Map<String, String> options = new HashMap<>();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).loadByDataSetCodes(dataSetCodes, false, true);
                    one(dataSetTable).archiveDatasets(removeFromDataStore, options);
                }
            });

        createServer().archiveDatasets(SESSION_TOKEN, dataSetCodes, removeFromDataStore, options);

        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchiveDataSets()
    {
        prepareGetSession();
        final List<String> dataSetCodes = Arrays.asList("a", "b");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).loadByDataSetCodes(dataSetCodes, false, true);
                    one(dataSetTable).unarchiveDatasets();
                }
            });

        createServer().unarchiveDatasets(SESSION_TOKEN, dataSetCodes);

        context.assertIsSatisfied();
    }

    @Test
    public void testSaveDisplaySettings()
    {
        final PersonPE person = new PersonPE();
        person.setId(123L);
        EntityVisit v0 = visit(EntityKind.MATERIAL, 0);
        EntityVisit v1 = visit(EntityKind.SAMPLE, 2);
        DisplaySettings currentDisplaySettings = displaySettingsWithVisits(v0, v1);
        person.setDisplaySettings(currentDisplaySettings);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getSessionFactory();
                    will(returnValue(hibernateSessionFactory));

                    one(hibernateSessionFactory).getCurrentSession();
                    will(returnValue(hibernateSession));

                    one(hibernateSession).get(PersonPE.class, person.getId());
                    will(returnValue(person));

                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    Session mySession = createSession(CommonTestUtils.USER_ID);
                    mySession.setPerson(person);
                    will(returnValue(mySession));

                    one(personDAO).lock(person);
                    one(personDAO).updatePerson(person);
                }
            });
        EntityVisit v2 = visit(EntityKind.EXPERIMENT, 1);
        EntityVisit v3 = visit(EntityKind.DATA_SET, 3);
        EntityVisit v4 = visit(EntityKind.SAMPLE, 2);
        DisplaySettings displaySettings = displaySettingsWithVisits(v0, v1, v2, v3, v4);

        createServer().saveDisplaySettings(SESSION_TOKEN, displaySettings, 3);

        assertSame(displaySettings, person.getDisplaySettings());
        @SuppressWarnings("deprecation")
        List<EntityVisit> visits = displaySettings.getVisits();
        assertEquals("DATA_SET-3", visits.get(0).getEntityTypeCode());
        assertEquals("SAMPLE-2", visits.get(1).getEntityTypeCode());
        assertEquals("EXPERIMENT-1", visits.get(2).getEntityTypeCode());
        assertEquals(3, visits.size());

        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    private DisplaySettings displaySettingsWithVisits(EntityVisit... entityVisits)
    {
        DisplaySettings settings = new DisplaySettings();
        for (EntityVisit entityVisit : entityVisits)
        {
            settings.addEntityVisit(entityVisit);
        }
        return settings;
    }

    private EntityVisit visit(EntityKind kind, long timeStamp)
    {
        EntityVisit entityVisit = new EntityVisit();
        entityVisit.setEntityKind(kind.toString());
        entityVisit.setEntityTypeCode(kind + "-" + timeStamp);
        entityVisit.setIdentifier("E" + timeStamp);
        entityVisit.setPermID("id-" + timeStamp);
        entityVisit.setTimeStamp(timeStamp);
        return entityVisit;
    }

    @Test
    public void testChangeUserHomeGroup()
    {
        final TechId groupId = CommonTestUtils.TECH_ID;
        final SpacePE group = new SpacePE();
        group.setId(groupId.getId());
        final PersonPE person = new PersonPE();
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    Session mySession = createSession(CommonTestUtils.USER_ID);
                    mySession.setPerson(person);
                    will(returnValue(mySession));

                    one(groupDAO).getByTechId(groupId);
                    will(returnValue(group));

                    allowing(personDAO).updatePerson(person);
                }
            });

        createServer().changeUserHomeSpace(SESSION_TOKEN, groupId);

        assertSame(group, person.getHomeSpace());
        assertSame(groupId.getId(), person.getHomeSpace().getId());

        context.assertIsSatisfied();
    }

    @Test
    public void testChangeUserHomeGroupToNull()
    {
        final TechId groupId = CommonTestUtils.TECH_ID;
        final SpacePE group = new SpacePE();
        group.setId(groupId.getId());
        final PersonPE person = new PersonPE();
        person.setHomeSpace(group);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    Session mySession = createSession(CommonTestUtils.USER_ID);
                    mySession.setPerson(person);
                    will(returnValue(mySession));
                    allowing(personDAO).updatePerson(person);
                }
            });

        createServer().changeUserHomeSpace(SESSION_TOKEN, null);

        assertNull(person.getHomeSpace());

        context.assertIsSatisfied();
    }

    @Test
    public void testGetExperimentInfo() throws Exception
    {
        prepareGetSession();
        final ExperimentIdentifier experimentIdentifier =
                CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE experimentPE = CommonTestUtils.createExperiment(experimentIdentifier);
        experimentPE.setPermId("<b>permId</b>");
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).loadByExperimentIdentifier(experimentIdentifier);
                    one(experimentBO).enrichWithProperties();
                    one(experimentBO).enrichWithAttachments();

                    one(experimentBO).getExperiment();
                    will(returnValue(experimentPE));

                    one(metaprojectDAO).listMetaprojectsForEntity(session.tryGetPerson(),
                            experimentPE);
                    will(returnValue(new HashSet<MetaprojectPE>()));
                }
            });
        final Experiment experiment =
                createServer().getExperimentInfo(SESSION_TOKEN, experimentIdentifier);
        assertEquals(experimentPE.getCode(), experiment.getCode());
        assertEquals(experimentPE.getExperimentType().getCode(), experiment.getExperimentType()
                .getCode());
        assertEquals(experimentPE.getPermId(), experiment.getPermId());
        context.assertIsSatisfied();
    }

    @Test
    public void testListDeletions()
    {
        // tests listing, sorting by date and translation
        prepareGetSession();

        final List<Deletion> deletions = new ArrayList<Deletion>();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createDeletionTable(session);
                    will(returnValue(deletionTable));

                    one(deletionTable).load(true);
                    one(deletionTable).getDeletions();
                    will(returnValue(deletions));
                }
            });

        List<Deletion> result = createServer().listDeletions(SESSION_TOKEN, true);

        assertSame(deletions, result);
        context.assertIsSatisfied();
    }

    public void testDeleteSamplesPermanently()
    {
        final DeletionType deletionType = DeletionType.PERMANENT;
        final String reason = "example reason";
        final List<TechId> sampleIds = TechId.createList(1, 2, 3);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createSampleTable(session);
                    will(returnValue(sampleTable));
                    one(sampleTable).deleteByTechIds(sampleIds, reason);
                }
            });

        createServer().deleteSamples(SESSION_TOKEN, sampleIds, reason, deletionType);

        context.assertIsSatisfied();
    }

    public void testTrashSamples()
    {
        final DeletionType deletionType = DeletionType.TRASH;
        final String reason = "example reason";
        final List<TechId> sampleIds = TechId.createList(1, 2, 3);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createTrashBO(session);
                    will(returnValue(trashBO));
                    one(trashBO).createDeletion(reason);
                    one(trashBO).trashSamples(sampleIds);
                }
            });

        createServer().deleteSamples(SESSION_TOKEN, sampleIds, reason, deletionType);

        context.assertIsSatisfied();
    }

    public void testDeleteExperimentsPermanently()
    {
        final DeletionType deletionType = DeletionType.PERMANENT;
        final String reason = "example reason";
        final List<TechId> experimentIds = TechId.createList(1, 2, 3);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));
                    one(experimentBO).deleteByTechIds(experimentIds, reason);
                }
            });

        createServer().deleteSamples(SESSION_TOKEN, experimentIds, reason, deletionType);

        context.assertIsSatisfied();
    }

    public void testTrashExperimentSamples()
    {
        final DeletionType deletionType = DeletionType.TRASH;
        final String reason = "example reason";
        final List<TechId> experimentIds = TechId.createList(1, 2, 3);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createTrashBO(session);
                    will(returnValue(trashBO));
                    one(trashBO).createDeletion(reason);
                    one(trashBO).trashExperiments(experimentIds);
                }
            });

        createServer().deleteSamples(SESSION_TOKEN, experimentIds, reason, deletionType);

        context.assertIsSatisfied();
    }
}
