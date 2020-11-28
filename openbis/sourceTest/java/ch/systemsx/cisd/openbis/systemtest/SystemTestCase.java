/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Abstract super class of head-less system tests.
 * 
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
// In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
// Spring looks for 'transactionManager').
@Transactional(transactionManager = "transaction-manager")
@Rollback
public abstract class SystemTestCase extends AbstractTransactionalTestNGSpringContextTests
{
    private static final String SOURCE_TEST_CORE_PLUGINS = "sourceTest/core-plugins";

    protected static final String SYSTEM_USER = "system";

    protected static final String NOT_EXISTING_USER = "notexistinguser";

    protected static final String TEST_SPACE_USER = "test_space";

    protected static final String TEST_ROLE_V3 = "test_v3";

    protected static final String TEST_USER = "test";

    protected static final String TEST_POWER_USER_CISD = "test_role";

    protected static final String TEST_INSTANCE_ETLSERVER = "etlserver";

    protected static final String TEST_SPACE_ETLSERVER_TESTSPACE = "test_space_etl_server";

    protected static final String TEST_GROUP_OBSERVER = "observer";

    protected static final String TEST_INSTANCE_OBSERVER = "instance_observer";

    protected static final String TEST_OBSERVER_CISD = "observer_cisd";

    protected static final String TEST_GROUP_POWERUSER = "poweruser";

    protected static final String TEST_GROUP_ADMIN = "admin";

    protected static final String TEST_NO_HOME_SPACE = "homeless";

    protected static final String PASSWORD = "password";

    protected static final String SESSION_KEY = "session-key";

    protected static final String PROVIDER_BOOLEAN = "provider-boolean";

    protected static final String PROVIDER_BOOLEAN_BOOLEAN = "provider-boolean-boolean";

    private static final String HOT_DEPLOYMENT_ENTITY_VALIDATION_PLUGINS_FOLDER_KEY = "entity-validation-plugins-directory";

    private static final String HOT_DEPLOYMENT_DYNAMIC_PROPERTY_PLUGINS_FOLDER_KEY = "dynamic-property-plugins-directory";

    private static final String HOT_DEPLOYMENT_MANAGED_PROPERTY_PLUGINS_FOLDER_KEY = "managed-property-plugins-directory";

    protected static final String HOT_DEPLOYMENT_ENTITY_VALIDATION_PLUGINS_FOLDER = "sourceTest/hot-deployment/entity-validation-plugins";

    protected static final String HOT_DEPLOYMENT_DYNAMIC_PROPERTY_PLUGINS_FOLDER = "sourceTest/hot-deployment/dynamic-property-plugins";

    protected static final String HOT_DEPLOYMENT_MANAGED_PROPERTY_PLUGINS_FOLDER = "sourceTest/hot-deployment/managed-property-plugins";

    protected IDAOFactory daoFactory;

    protected ICommonServerForInternalUse commonServer;

    protected IGenericServer genericServer;

    protected ICommonClientService commonClientService;

    protected IGenericClientService genericClientService;

    protected IServiceForDataStoreServer etlService;

    protected MockHttpServletRequest request;

    protected String systemSessionToken;

    @Autowired
    protected ISessionWorkspaceProvider sessionWorkspaceProvider;

    @BeforeSuite
    public void beforeSuite() throws IOException
    {
        System.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY, SOURCE_TEST_CORE_PLUGINS);
        System.setProperty(Constants.ENABLED_MODULES_KEY, "test-.*");

        initHotDeploymentFolder(HOT_DEPLOYMENT_DYNAMIC_PROPERTY_PLUGINS_FOLDER_KEY, HOT_DEPLOYMENT_DYNAMIC_PROPERTY_PLUGINS_FOLDER);
        initHotDeploymentFolder(HOT_DEPLOYMENT_ENTITY_VALIDATION_PLUGINS_FOLDER_KEY, HOT_DEPLOYMENT_ENTITY_VALIDATION_PLUGINS_FOLDER);
        initHotDeploymentFolder(HOT_DEPLOYMENT_MANAGED_PROPERTY_PLUGINS_FOLDER_KEY, HOT_DEPLOYMENT_MANAGED_PROPERTY_PLUGINS_FOLDER);

        TestInitializer.init();
    }

    @BeforeMethod(alwaysRun = true)
    @BeforeClass
    public void loginAsSystem()
    {
        systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
    }

    /**
     * Sets a {@link MockHttpServletRequest} for the specified context provider
     */
    @Autowired
    public final void setRequestContextProvider(final SpringRequestContextProvider contextProvider)
    {
        request = new MockHttpServletRequest();
        contextProvider.setRequest(request);
    }

    /**
     * Sets <code>daoFactory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setDaoFactory(final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    /**
     * Sets <code>commonServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonServer(final ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    /**
     * Sets <code>genericServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericServer(final IGenericServer genericServer)
    {
        this.genericServer = genericServer;
    }

    /**
     * Sets <code>commonClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonClientService(final ICommonClientService commonClientService)
    {
        this.commonClientService = commonClientService;
    }

    /**
     * Sets <code>genericClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericClientService(final IGenericClientService genericClientService)
    {
        this.genericClientService = genericClientService;
    }

    @Autowired
    public void setETLService(IServiceForDataStoreServer etlService)
    {
        this.etlService = etlService;

    }

    protected SessionContext logIntoCommonClientService()
    {
        SessionContext context = commonClientService.tryToLogin("test", "a");
        AssertJUnit.assertNotNull(context);
        return context;
    }

    protected void logOutFromCommonClientService()
    {
        commonClientService.logout(new DisplaySettings(), false);
    }

    protected void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    public final class NewSampleBuilder
    {
        private NewSample sample = new NewSample();

        private List<IEntityProperty> propertis = new ArrayList<IEntityProperty>();

        public NewSample get()
        {
            return sample;
        }

        public NewSampleBuilder(String identifier)
        {
            sample.setIdentifier(identifier);
        }

        public NewSampleBuilder type(String type)
        {
            SampleType sampleType = new SampleType();
            sampleType.setCode(type);
            sample.setSampleType(sampleType);
            return this;
        }

        public NewSampleBuilder experiment(String identifier)
        {
            sample.setExperimentIdentifier(identifier);
            return this;
        }

        public NewSampleBuilder container(String identifier)
        {
            sample.setContainerIdentifier(identifier);
            return this;
        }

        public NewSampleBuilder parents(String... parentIdentifiers)
        {
            sample.setParentsOrNull(parentIdentifiers);
            return this;
        }

        public NewSampleBuilder property(String key, String value)
        {
            propertis.add(new PropertyBuilder(key).value(value).getProperty());
            return this;
        }

        public void register()
        {
            sample.setProperties(propertis.toArray(new IEntityProperty[propertis.size()]));
            genericClientService.registerSample(SESSION_KEY, sample);
        }
    }

    /**
     * Register a person with specified user ID.
     * 
     * @return userID
     */
    protected String registerPerson(String userID)
    {
        commonServer.registerPerson(systemSessionToken, userID);
        return userID;
    }

    protected void assignInstanceRole(String userID, RoleCode roleCode)
    {
        commonServer.registerInstanceRole(systemSessionToken, roleCode,
                Grantee.createPerson(userID));
    }

    protected void assignSpaceRole(String userID, RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        commonServer.registerSpaceRole(systemSessionToken, roleCode, spaceIdentifier,
                Grantee.createPerson(userID));
    }

    protected void assignSpaceRoleToGroup(String groupCode, RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        commonServer.registerSpaceRole(systemSessionToken, roleCode, spaceIdentifier, Grantee.createAuthorizationGroup(groupCode));
    }

    protected String registerAuthorizationGroupWithUsers(String groupName, List<String> userIds)
    {
        NewAuthorizationGroup newGroup = new NewAuthorizationGroup();
        newGroup.setCode(groupName);
        newGroup.setDescription(groupName);
        commonServer.registerAuthorizationGroup(systemSessionToken, newGroup);

        List<AuthorizationGroup> groups = commonServer.listAuthorizationGroups(systemSessionToken);
        AuthorizationGroup group = null;
        for (AuthorizationGroup groupCandidate : groups)
        {
            if (groupCandidate.getCode().equals(groupName))
            {
                group = groupCandidate;
                break;
            }
        }

        commonServer.addPersonsToAuthorizationGroup(systemSessionToken, TechId.create(group), userIds);

        return groupName;
    }

    /**
     * Authenticates as specified user.
     * 
     * @return session token
     */
    protected String authenticateAs(String user)
    {
        return commonServer.tryAuthenticate(user, "password").getSessionToken();
    }

    protected NewSampleBuilder sample(String identifier)
    {
        return new NewSampleBuilder(identifier);
    }

    protected void uploadFile(String sessionToken, String fileName, String fileContent)
    {
        UploadedFilesBean bean = new UploadedFilesBean();
        bean.addMultipartFile(sessionToken, new MockMultipartFile(fileName, fileName, null, fileContent
                .getBytes()), sessionWorkspaceProvider);
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_KEY, bean);
    }

    protected <T extends Serializable> List<T> asList(TypedTableResultSet<T> resultSet)
    {
        List<T> list = new ArrayList<T>();
        for (GridRowModel<TableModelRowWithObject<T>> gridRowModel : resultSet.getResultSet()
                .getList())
        {
            list.add(gridRowModel.getOriginalObject().getObjectOrNull());
        }
        return list;
    }

    protected <T extends CodeWithRegistration<?>> T getOriginalObjectByCode(
            TypedTableResultSet<T> resultSet, String code)
    {
        GridRowModels<TableModelRowWithObject<T>> list = resultSet.getResultSet().getList();
        List<String> codes = new ArrayList<String>();
        for (GridRowModel<TableModelRowWithObject<T>> gridRowModel : list)
        {
            T originalObject = gridRowModel.getOriginalObject().getObjectOrNull();
            String objectCode = originalObject.getCode();
            if (objectCode.equals(code))
            {
                return originalObject;
            }
            codes.add(objectCode);
        }
        AssertJUnit.fail("No row with code " + code + " found in " + codes);
        return null;
    }

    protected <T extends CodeWithRegistration<?>> void assertObjectWithCodeDoesNotExists(
            TypedTableResultSet<T> resultSet, String code)
    {
        GridRowModels<TableModelRowWithObject<T>> list = resultSet.getResultSet().getList();
        List<String> codes = new ArrayList<String>();
        boolean rowFound = false;
        for (GridRowModel<TableModelRowWithObject<T>> gridRowModel : list)
        {
            T originalObject = gridRowModel.getOriginalObject().getObjectOrNull();
            String objectCode = originalObject.getCode();
            if (objectCode.equals(code))
            {
                rowFound = true;
            }
            codes.add(objectCode);
        }
        if (rowFound)
        {
            AssertJUnit.fail("Row with code " + code + " was found in " + codes);
        }
    }

    protected void assertProperties(String expectedProperties,
            IEntityPropertiesHolder propertiesHolder)
    {
        List<IEntityProperty> properties =
                new ArrayList<IEntityProperty>(propertiesHolder.getProperties());
        Collections.sort(properties, new Comparator<IEntityProperty>()
            {
                @Override
                public int compare(IEntityProperty p1, IEntityProperty p2)
                {
                    return p1.getPropertyType().getCode().compareTo(p2.getPropertyType().getCode());
                }
            });
        assertEquals(expectedProperties, properties.toString());
    }

    protected void assertProperty(IEntityPropertiesHolder propertiesHolder, String key, String value)
    {
        List<IEntityProperty> properties = propertiesHolder.getProperties();
        List<String> propertyCodes = new ArrayList<String>();
        for (IEntityProperty property : properties)
        {
            String code = property.getPropertyType().getCode();
            if (code.equals(key))
            {
                assertEquals("Property " + key, value, property.tryGetAsString());
                return;
            }
            propertyCodes.add(code);
        }
        AssertJUnit.fail("No property " + key + " found in " + propertyCodes);
    }

    protected void assertEntities(String expectedEntities,
            Collection<? extends IIdentifierHolder> entities)
    {
        List<String> identifiers = new ArrayList<String>();
        for (IIdentifierHolder entity : entities)
        {
            identifiers.add(entity.getIdentifier());
        }
        Collections.sort(identifiers);
        assertEquals(expectedEntities, identifiers.toString());
    }

    protected void assertExceptionMessage(IDelegatedAction action, String expectedMessage)
    {
        try
        {
            action.execute();

            if (expectedMessage != null)
            {
                fail("Expected exception was not thrown: " + expectedMessage);
            }
        } catch (Exception e)
        {
            if (expectedMessage == null)
            {
                fail("Unexpected exception was thrown: " + e.getMessage());
            }
            assertTrue(e.getMessage().contains(expectedMessage), "Expected exception: " + expectedMessage + ", Got exception: " + e.getMessage());
        }
    }

    protected List<PropertyHistory> getMaterialPropertiesHistory(long materialID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from material_properties_history as h "
                                + " join material_type_property_types as etpt on h.mtpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.mate_id = ?",
                                new HistoryRowMapper(), materialID);
        sort(list);
        return list;
    }

    protected List<PropertyHistory> getExperimentPropertiesHistory(long experimentID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from experiment_properties_history as h "
                                + " join experiment_type_property_types as etpt on h.etpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.expe_id = ?",
                                new HistoryRowMapper(), experimentID);
        sort(list);
        return list;
    }

    protected List<PropertyHistory> getSamplePropertiesHistory(long sampleID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from sample_properties_history as h "
                                + " join sample_type_property_types as etpt on h.stpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.samp_id = ?",
                                new HistoryRowMapper(), sampleID);
        sort(list);
        return list;
    }

    protected List<PropertyHistory> getDataSetPropertiesHistory(long dataSetID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from data_set_properties_history as h "
                                + " join data_set_type_property_types as etpt on h.dstpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.ds_id = ?",
                                new HistoryRowMapper(), dataSetID);
        sort(list);
        return list;
    }

    private void sort(List<PropertyHistory> list)
    {
        Collections.sort(list, new Comparator<PropertyHistory>()
            {
                @Override
                public int compare(PropertyHistory o1, PropertyHistory o2)
                {
                    return o1.toString().compareTo(o2.toString());
                }
            });
    }

    protected EntityTypePropertyType<?> getETPT(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode)
    {
        List<EntityTypePropertyType<?>> etpts =
                commonServer.listEntityTypePropertyTypes(systemSessionToken);
        for (EntityTypePropertyType<?> etpt : etpts)
        {
            if (etpt.getEntityKind().equals(entityKind) == false)
            {
                continue;
            }
            if (etpt.getPropertyType().getCode().equals(propertyTypeCode) == false)
            {
                continue;
            }
            if (etpt.getEntityType().getCode().equals(entityTypeCode))
            {
                return etpt;
            }
        }
        throw new IllegalArgumentException("No assignment for " + entityTypeCode + " with "
                + propertyTypeCode + ".");
    }

    @DataProvider(name = PROVIDER_BOOLEAN)
    protected Object[][] provideBoolean()
    {
        return new Object[][] { { true }, { false } };
    }

    @DataProvider(name = PROVIDER_BOOLEAN_BOOLEAN)
    protected Object[][] provideBooleanBoolean()
    {
        return new Object[][] { { true, true }, { true, false }, { false, true }, { false, false } };
    }

    protected static Object[][] asCartesianProduct(List<?>... lists)
    {
        int size = 1;
        for (List<?> list : lists)
        {
            size *= list.size();
        }
        List<Object[]> result = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            Object[] row = new Object[lists.length];
            for (int j = 0, ii = i; j < lists.length; j++)
            {
                List<?> list = lists[j];
                int listSize = list.size();
                row[j] = list.get(ii % listSize);
                ii /= list.size();
            }
            result.add(row);
        }
        return result.toArray(new Object[0][]);
    }

    protected static Object[][] merge(Object[][]... results)
    {
        List<Object[]> mergedResults = new ArrayList<>();
        for (Object[][] result : results)
        {
            mergedResults.addAll(Arrays.asList(result));
        }
        return mergedResults.toArray(new Object[0][]);
    }

    private void initHotDeploymentFolder(String propertyKey, String folderPath) throws IOException
    {
        System.setProperty(propertyKey, folderPath);

        File folder = new File(folderPath);

        if (folder.exists())
        {
            FileUtils.cleanDirectory(folder);
        } else
        {
            folder.mkdirs();
        }
    }

    protected Script hotDeployPlugin(ScriptType pluginType, String pluginName, File pluginJarFile)
    {
        File hotDeploymentFolder = null;

        if (pluginType == ScriptType.DYNAMIC_PROPERTY)
        {
            hotDeploymentFolder = new File(HOT_DEPLOYMENT_DYNAMIC_PROPERTY_PLUGINS_FOLDER);
        } else if (pluginType == ScriptType.ENTITY_VALIDATION)
        {
            hotDeploymentFolder = new File(HOT_DEPLOYMENT_ENTITY_VALIDATION_PLUGINS_FOLDER);
        } else if (pluginType == ScriptType.MANAGED_PROPERTY)
        {
            hotDeploymentFolder = new File(HOT_DEPLOYMENT_MANAGED_PROPERTY_PLUGINS_FOLDER);
        } else
        {
            throw new IllegalArgumentException("Unsupported plugin type: " + pluginType);
        }

        SessionContextDTO session = commonServer.tryAuthenticate(TEST_USER, PASSWORD);

        List<Script> beforePlugins = commonServer.listScripts(session.getSessionToken(), pluginType, null);

        if (beforePlugins.stream().anyMatch(p -> p.getName().equals(pluginName)))
        {
            throw new IllegalArgumentException(String.format("Plugin '%s' already exists", pluginName));
        }

        try
        {
            Files.copy(pluginJarFile.toPath(), hotDeploymentFolder.toPath().resolve(pluginJarFile.getName()));
        } catch (IOException e)
        {
            throw new RuntimeException(
                    String.format("Could not hot deploy plugin with type '%s', name '%s' and jar file '%s'", pluginType, pluginName, pluginJarFile));
        }

        long timeoutMillis = System.currentTimeMillis() + 30 * 1000;

        while (System.currentTimeMillis() < timeoutMillis)
        {
            List<Script> afterPlugins = commonServer.listScripts(session.getSessionToken(), pluginType, null);

            Optional<Script> plugin = afterPlugins.stream().filter(p -> p.getName().equals(pluginName)).findFirst();

            if (plugin.isPresent())
            {
                return plugin.get();
            } else
            {
                sleep(100);
            }
        }

        throw new RuntimeException(String.format("Timed out waiting for hot deployment of plugin with type '%s', name '%s' and jar file '%s'",
                pluginType, pluginName, pluginJarFile));
    }

}
