/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.DynamicPropertyPluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.DynamicPropertyPluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.EntityValidationPluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.EntityValidationPluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.EvaluatePluginTestResources.TestDynamicPropertyHotDeployedPlugin;
import ch.ethz.sis.openbis.systemtest.asapi.v3.EvaluatePluginTestResources.TestEntityValidationHotDeployedPlugin;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * @author pkupczyk
 */
public class EvaluatePluginTest extends AbstractTest
{

    private static final String PROPERTY = "PLUGIN_EVALUATION_TEST_" + UUID.randomUUID().toString();

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Plugin evaluation options cannot be null.*")
    public void testEvaluteWithoutOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        v3api.evaluatePlugin(sessionToken, null);
    }

    @DataProvider
    private Object[][] providerTestEvaluteWithoutPluginIdAndPluginScript()
    {
        return new Object[][] { { new DynamicPropertyPluginEvaluationOptions() }, { new EntityValidationPluginEvaluationOptions() } };
    }

    @Test(dataProvider = "providerTestEvaluteWithoutPluginIdAndPluginScript", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Plugin id and plugin script cannot be both null.*")
    public void testEvaluteWithoutPluginIdAndPluginScript(PluginEvaluationOptions options)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        v3api.evaluatePlugin(sessionToken, options);
    }

    @DataProvider
    private Object[][] providerTestEvaluteWithBothPluginIdAndPluginScript()
    {
        return new Object[][] { { new DynamicPropertyPluginEvaluationOptions() }, { new EntityValidationPluginEvaluationOptions() } };
    }

    @Test(dataProvider = "providerTestEvaluteWithBothPluginIdAndPluginScript", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Plugin id and plugin script cannot be both specified.*")
    public void testEvaluteWithBothPluginIdAndPluginScript(PluginEvaluationOptions options)
    {
        options.setPluginId(new PluginPermId("some id"));
        options.setPluginScript("some script");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        v3api.evaluatePlugin(sessionToken, options);
    }

    @DataProvider
    private Object[][] providerTestEvaluteWithoutAccessRights()
    {
        return new Object[][] { { TEST_SPACE_USER, new DynamicPropertyPluginEvaluationOptions() },
                { TEST_INSTANCE_OBSERVER, new DynamicPropertyPluginEvaluationOptions() },
                { TEST_SPACE_USER, new EntityValidationPluginEvaluationOptions() },
                { TEST_INSTANCE_OBSERVER, new EntityValidationPluginEvaluationOptions() } };
    }

    @Test(dataProvider = "providerTestEvaluteWithoutAccessRights", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*None of method roles '\\[INSTANCE_ADMIN\\]' could be found in roles of user.*")
    public void testEvaluteWithoutAccessRights(String user, PluginEvaluationOptions options)
    {
        options.setPluginId(new PluginPermId("some id"));

        String sessionToken = v3api.login(user, PASSWORD);
        v3api.evaluatePlugin(sessionToken, options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Evaluation of dynamic property plugin failed.*")
    public void testEvaluteDynamicPropertyPluginWithWrongScript()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId objectTypeId = createTestObjectType();
        Sample object = createTestObject(objectTypeId, Collections.emptyMap());

        DynamicPropertyPluginEvaluationOptions options = new DynamicPropertyPluginEvaluationOptions();
        options.setObjectId(object.getPermId());
        options.setPluginScript("I am incorrect");

        v3api.evaluatePlugin(sessionToken, options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "'test plugin' is a dynamic property plugin. It requires 'DynamicPropertyPluginEvaluationOptions' evaluation options, but got 'EntityValidationPluginEvaluationOptions'.*")
    public void testEvaluteDynamicPropertyPluginWithWrongOptionsClass()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("test plugin");
        pluginCreation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        pluginCreation.setScript("def calculate():\n  pass");

        PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);

        EntityValidationPluginEvaluationOptions wrongOptions = new EntityValidationPluginEvaluationOptions();
        wrongOptions.setPluginId(pluginId);

        v3api.evaluatePlugin(sessionToken, wrongOptions);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Object id cannot be null.*")
    public void testEvaluteDynamicPropertyPluginWithoutObjectId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("test plugin");
        pluginCreation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        pluginCreation.setScript("def calculate():\n  pass");

        PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);

        DynamicPropertyPluginEvaluationOptions options = new DynamicPropertyPluginEvaluationOptions();
        options.setPluginId(pluginId);

        v3api.evaluatePlugin(sessionToken, options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*The plugin expects objects with entity kind 'DATA_SET' while the object has entity kind 'SAMPLE'.*")
    public void testEvaluteDynamicPropertyPluginWithWrongObjectKind()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId objectTypeId = createTestObjectType();
        Sample object = createTestObject(objectTypeId, Collections.emptyMap());

        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("test plugin");
        pluginCreation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        pluginCreation.setScript("def calculate():\n  pass");
        pluginCreation.setEntityKind(EntityKind.DATA_SET);

        PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);

        DynamicPropertyPluginEvaluationOptions options = new DynamicPropertyPluginEvaluationOptions();
        options.setPluginId(pluginId);
        options.setObjectId(object.getPermId());

        v3api.evaluatePlugin(sessionToken, options);
    }

    @DataProvider
    private Object[][] providerTestEvaluteDynamicPropertyPlugin()
    {
        return new Object[][] { { true }, { false } };
    }

    @Test(dataProvider = "providerTestEvaluteDynamicPropertyPlugin")
    public void testEvaluteDynamicPropertyPlugin(boolean createPlugin)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId objectTypeId = createTestObjectType(PROPERTY);
        Sample object = createTestObject(objectTypeId, Collections.singletonMap(PROPERTY, "testValue"));

        DynamicPropertyPluginEvaluationOptions options = new DynamicPropertyPluginEvaluationOptions();
        String script = "def calculate():\n  return entity.propertyValue('" + PROPERTY + "')";

        if (createPlugin)
        {
            PluginCreation pluginCreation = new PluginCreation();
            pluginCreation.setName("test plugin");
            pluginCreation.setPluginType(PluginType.DYNAMIC_PROPERTY);
            pluginCreation.setScript(script);

            PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);
            options.setPluginId(pluginId);
        } else
        {
            options.setPluginScript(script);
        }

        options.setObjectId(object.getPermId());

        DynamicPropertyPluginEvaluationResult result = (DynamicPropertyPluginEvaluationResult) v3api.evaluatePlugin(sessionToken, options);
        assertEquals(result.getValue(), "testValue");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testEvaluteDynamicPropertyPluginWithScriptAccessingSampleComponents()
    {
        // Given
        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("Children and Components");
        pluginCreation.setPluginType(PluginType.DYNAMIC_PROPERTY);
        pluginCreation.setScript("def calculate():\n"
                + "    components = [str(component.code()) for component in entity.contained()]\n"
                + "    children = [str(child.code()) for child in entity.children()]\n"
                + "    return \"%s: children: %s, components: %s\" % (entity.code(), children, components)");
        PluginPermId pluginId = v3api.createPlugins(systemSessionToken, Arrays.asList(pluginCreation)).get(0);

        PropertyTypeCreation pTypeCreation = new PropertyTypeCreation();
        pTypeCreation.setCode("CHILDREN_AND_COMPONENTS_DYNAMIC_PROPERTY");
        pTypeCreation.setDataType(DataType.VARCHAR);
        pTypeCreation.setLabel("Children and Components");
        pTypeCreation.setDescription("Chilrden & components");
        pTypeCreation.setMultiValue(false);
        PropertyTypePermId pTypeId = v3api.createPropertyTypes(systemSessionToken, Arrays.asList(pTypeCreation)).get(0);

        SampleTypeCreation sTypeCreation = new SampleTypeCreation();
        sTypeCreation.setCode("SAMPLE_WITH_DYNAMIC_PROPERTY");
        sTypeCreation.setGeneratedCodePrefix("SWDP-");
        sTypeCreation.setAutoGeneratedCode(true);
        sTypeCreation.setShowContainer(true);
        sTypeCreation.setShowParents(true);
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(pTypeId);
        assignmentCreation.setPluginId(pluginId);
        sTypeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation));
        EntityTypePermId sTypeId = v3api.createSampleTypes(systemSessionToken, Arrays.asList(sTypeCreation)).get(0);

        SpacePermId spaceId = new SpacePermId("CISD");
        SampleCreation childSampleCreation = new SampleCreation();
        childSampleCreation.setCode("CHILD");
        childSampleCreation.setTypeId(new EntityTypePermId("NORMAL"));
        childSampleCreation.setSpaceId(spaceId);
        SampleCreation componentSampleCreation = new SampleCreation();
        componentSampleCreation.setCode("COMPONENT");
        componentSampleCreation.setTypeId(new EntityTypePermId("NORMAL"));
        componentSampleCreation.setSpaceId(spaceId);
        List<SamplePermId> samples = v3api.createSamples(systemSessionToken, 
                Arrays.asList(childSampleCreation, componentSampleCreation));
        SamplePermId childSampleId = samples.get(0);
        SamplePermId componentSampleId = samples.get(1);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(sTypeId);
        sampleCreation.setSpaceId(spaceId);
        sampleCreation.setChildIds(Arrays.asList(childSampleId));
        sampleCreation.setComponentIds(Arrays.asList(componentSampleId));

        // When
        SamplePermId sampleId = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).get(0);

        // Then
        sleep(2000);
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();
        Sample sample = v3api.getSamples(systemSessionToken, Arrays.asList(sampleId), fo).get(sampleId);
        assertEquals(sample.getProperties().get(pTypeCreation.getCode()), 
                sample.getCode() + ": children: ['CHILD'], components: ['COMPONENT']");

        // Remove all stuff created in this test because there will be no roll back
        SampleDeletionOptions deletionOptions1 = new SampleDeletionOptions();
        deletionOptions1.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, 
                Arrays.asList(sampleId, childSampleId, componentSampleId), deletionOptions1);
        v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId));
        SampleTypeDeletionOptions deletionOptions2 = new SampleTypeDeletionOptions();
        deletionOptions2.setReason("test");
        v3api.deleteSampleTypes(systemSessionToken, Arrays.asList(sTypeId), deletionOptions2);
        PropertyTypeDeletionOptions deletionOptions3 = new PropertyTypeDeletionOptions();
        deletionOptions3.setReason("test");
        v3api.deletePropertyTypes(systemSessionToken, Arrays.asList(pTypeId), deletionOptions3);
        PluginDeletionOptions deletionOptions4 = new PluginDeletionOptions();
        deletionOptions4.setReason("test");
        v3api.deletePlugins(systemSessionToken, Arrays.asList(pluginId), deletionOptions4);
    }

    @Test
    public void testEvaluteDynamicPropertyPluginHotDeployed()
    {
        File pluginJar = new TestResources(getClass()).getResourceFile("test-dynamic-property-plugin.jar");

        try
        {
            String sessionToken = v3api.login(TEST_USER, PASSWORD);

            EntityTypePermId objectTypeId = createTestObjectType(PROPERTY);
            Sample object = createTestObject(objectTypeId, Collections.singletonMap(PROPERTY, "testHotDeployedValue"));

            hotDeployPlugin(ScriptType.DYNAMIC_PROPERTY, TestDynamicPropertyHotDeployedPlugin.PLUGIN_NAME, pluginJar);

            DynamicPropertyPluginEvaluationOptions options = new DynamicPropertyPluginEvaluationOptions();
            options.setPluginId(new PluginPermId(TestDynamicPropertyHotDeployedPlugin.PLUGIN_NAME));
            options.setObjectId(object.getPermId());

            DynamicPropertyPluginEvaluationResult result = (DynamicPropertyPluginEvaluationResult) v3api.evaluatePlugin(sessionToken, options);
            assertEquals(result.getValue(), "testHotDeployedValue");
        } finally
        {
            hotUndeployPlugin(ScriptType.DYNAMIC_PROPERTY, TestDynamicPropertyHotDeployedPlugin.PLUGIN_NAME, pluginJar.getName());
        }
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Evaluation of entity validation plugin failed.*")
    public void testEvaluteEntityValidationPluginWithWrongScript()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId objectTypeId = createTestObjectType();
        Sample object = createTestObject(objectTypeId, Collections.emptyMap());

        EntityValidationPluginEvaluationOptions options = new EntityValidationPluginEvaluationOptions();
        options.setObjectId(object.getPermId());
        options.setPluginScript("I am incorrect");

        v3api.evaluatePlugin(sessionToken, options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "'test plugin' is an entity validation plugin. It requires 'EntityValidationPluginEvaluationOptions' evaluation options, but got 'DynamicPropertyPluginEvaluationOptions'.*")
    public void testEvaluteEntityValidationPluginWithWrongOptionsClass()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("test plugin");
        pluginCreation.setPluginType(PluginType.ENTITY_VALIDATION);
        pluginCreation.setScript("def validate():\n  pass");

        PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);

        DynamicPropertyPluginEvaluationOptions wrongOptions = new DynamicPropertyPluginEvaluationOptions();
        wrongOptions.setPluginId(pluginId);

        v3api.evaluatePlugin(sessionToken, wrongOptions);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Object id cannot be null.*")
    public void testEvaluteEntityValidationPluginWithoutObjectId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("test plugin");
        pluginCreation.setPluginType(PluginType.ENTITY_VALIDATION);
        pluginCreation.setScript("def validate():\n  pass");

        PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);

        EntityValidationPluginEvaluationOptions options = new EntityValidationPluginEvaluationOptions();
        options.setPluginId(pluginId);

        v3api.evaluatePlugin(sessionToken, options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*The plugin expects objects with entity kind 'EXPERIMENT' while the object has entity kind 'SAMPLE'.*")
    public void testEvaluteEntityValidationPluginWithWrongObjectKind()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId objectTypeId = createTestObjectType();
        Sample object = createTestObject(objectTypeId, Collections.emptyMap());

        PluginCreation pluginCreation = new PluginCreation();
        pluginCreation.setName("test plugin");
        pluginCreation.setPluginType(PluginType.ENTITY_VALIDATION);
        pluginCreation.setScript("def validate():\n  pass");
        pluginCreation.setEntityKind(EntityKind.EXPERIMENT);

        PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);

        EntityValidationPluginEvaluationOptions options = new EntityValidationPluginEvaluationOptions();
        options.setPluginId(pluginId);
        options.setObjectId(object.getPermId());

        v3api.evaluatePlugin(sessionToken, options);
    }

    @DataProvider
    private Object[][] providerTestEvaluteEntityValidationPlugin()
    {
        return new Object[][] { { true, true, true }, { true, true, false }, { true, false, true }, { true, false, false },
                { false, true, true }, { false, true, false }, { false, false, true }, { false, false, false } };
    }

    @Test(dataProvider = "providerTestEvaluteEntityValidationPlugin")
    public void testEvaluteEntityValidationPlugin(boolean createPlugin, boolean requestValidation, boolean returnError)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId objectTypeId = createTestObjectType(PROPERTY);
        Sample object = createTestObject(objectTypeId, Collections.singletonMap(PROPERTY, "testError"));

        EntityValidationPluginEvaluationOptions options = new EntityValidationPluginEvaluationOptions();

        StringBuilder script = new StringBuilder();
        script.append("def validate(entity, isNew):\n");
        script.append("  if not isNew:\n");
        script.append("    raise Exception('isNew was false')\n");

        if (requestValidation)
        {
            script.append("  requestValidation(entity)\n");
        }

        if (returnError)
        {
            script.append("  return entity.propertyValue('" + PROPERTY + "')\n");
        } else
        {
            script.append("  return None\n");
        }

        if (createPlugin)
        {
            PluginCreation pluginCreation = new PluginCreation();
            pluginCreation.setName("test plugin");
            pluginCreation.setPluginType(PluginType.ENTITY_VALIDATION);
            pluginCreation.setScript(script.toString());

            PluginPermId pluginId = v3api.createPlugins(sessionToken, Arrays.asList(pluginCreation)).get(0);
            options.setPluginId(pluginId);
        } else
        {
            options.setPluginScript(script.toString());
        }

        options.setObjectId(object.getPermId());
        options.setNew(true);

        EntityValidationPluginEvaluationResult result = (EntityValidationPluginEvaluationResult) v3api.evaluatePlugin(sessionToken, options);

        if (requestValidation)
        {
            assertEquals(result.getRequestedValidations().toString(), "[" + object.getIdentifier() + "]");
        } else
        {
            assertEquals(result.getRequestedValidations().toString(), "[]");
        }

        if (returnError)
        {
            assertEquals(result.getError(), "testError");
        } else
        {
            assertEquals(result.getError(), null);
        }
    }

    @Test
    public void testEvaluteEntityValidationPluginHotDeployed()
    {
        File pluginJar = new TestResources(getClass()).getResourceFile("test-entity-validation-plugin.jar");

        try
        {
            String sessionToken = v3api.login(TEST_USER, PASSWORD);

            EntityTypePermId objectTypeId = createTestObjectType(PROPERTY);
            Sample object = createTestObject(objectTypeId, Collections.singletonMap(PROPERTY, "testHotDeployedError"));

            hotDeployPlugin(ScriptType.ENTITY_VALIDATION, TestEntityValidationHotDeployedPlugin.PLUGIN_NAME, pluginJar);

            EntityValidationPluginEvaluationOptions options = new EntityValidationPluginEvaluationOptions();
            options.setPluginId(new PluginPermId(TestEntityValidationHotDeployedPlugin.PLUGIN_NAME));
            options.setObjectId(object.getPermId());

            EntityValidationPluginEvaluationResult result = (EntityValidationPluginEvaluationResult) v3api.evaluatePlugin(sessionToken, options);
            assertEquals(result.getError(), "testHotDeployedError");
            assertEquals(result.getRequestedValidations(), Collections.singleton(object.getIdentifier()));
        } finally
        {
            hotUndeployPlugin(ScriptType.ENTITY_VALIDATION, TestEntityValidationHotDeployedPlugin.PLUGIN_NAME, pluginJar.getName());
        }
    }

    private EntityTypePermId createTestObjectType(String... propertyCodes)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        sampleTypeCreation.setCode("EVALUATION_TEST");
        sampleTypeCreation.setGeneratedCodePrefix("PREFIX_");
        sampleTypeCreation.setPropertyAssignments(new ArrayList<>());

        for (String propertyCode : propertyCodes)
        {
            PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
            propertyTypeCreation.setCode(propertyCode);
            propertyTypeCreation.setLabel(propertyCode);
            propertyTypeCreation.setDescription(propertyCode);
            propertyTypeCreation.setDataType(DataType.VARCHAR);
            propertyTypeCreation.setMultiValue(false);
            v3api.createPropertyTypes(sessionToken, Arrays.asList(propertyTypeCreation));

            PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
            propertyAssignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyCode));
            sampleTypeCreation.getPropertyAssignments().add(propertyAssignmentCreation);
        }

        return v3api.createSampleTypes(sessionToken, Arrays.asList(sampleTypeCreation)).get(0);
    }

    private Sample createTestObject(IEntityTypeId typeId, Map<String, Serializable> properties)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
        propertyAssignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(typeId);
        sampleCreation.setCode("EVALUATION_TEST");
        sampleCreation.setProperties(properties);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        return v3api.getSamples(sessionToken, sampleIds, new SampleFetchOptions()).values().iterator().next();
    }

}
