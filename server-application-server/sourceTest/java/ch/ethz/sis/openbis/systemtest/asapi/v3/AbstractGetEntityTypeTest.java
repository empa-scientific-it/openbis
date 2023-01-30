/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public abstract class AbstractGetEntityTypeTest extends AbstractTest
{

    protected abstract EntityKind getEntityKind();

    protected abstract EntityTypePermId createEntityType(String sessionToken, String entityTypeCode, List<PropertyAssignmentCreation> properties,
            IPluginId validationPluginId);

    protected abstract FetchOptions<?> createFetchOptions(boolean withProperties, boolean withValidationPlugin);

    protected abstract Map<IEntityTypeId, ? extends IEntityType> getEntityTypes(String sessionToken, List<? extends IEntityTypeId> entityTypeIds,
            FetchOptions<?> fetchOptions);

    @Test(dataProvider = PROVIDER_BOOLEAN_BOOLEAN)
    public void testGetByPermId(boolean withEntityKind, boolean withUpperCase)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        createEntityType(sessionToken, "ENTITY_TEST_TYPE_1", null, null);
        createEntityType(sessionToken, "ENTITY_TEST_TYPE_2", null, null);

        EntityTypePermId permId1 =
                new EntityTypePermId(withUpperCase ? "ENTITY_TEST_TYPE_1" : "entity_test_TYPE_1", withEntityKind ? getEntityKind() : null);
        EntityTypePermId permId1WithUpperCase = new EntityTypePermId(permId1.getPermId().toUpperCase(), permId1.getEntityKind());
        EntityTypePermId permId1WithEntityKind = new EntityTypePermId(permId1.getPermId(), getEntityKind());
        EntityTypePermId permId1WithEntityKindWithUpperCase = new EntityTypePermId(permId1.getPermId().toUpperCase(), getEntityKind());

        EntityTypePermId permId2 =
                new EntityTypePermId(withUpperCase ? "ENTITY_TEST_TYPE_2" : "ENTITY_TEST_type_2", withEntityKind ? getEntityKind() : null);
        EntityTypePermId permId2WithUpperCase = new EntityTypePermId(permId2.getPermId().toUpperCase(), permId2.getEntityKind());
        EntityTypePermId permId2WithEntityKind = new EntityTypePermId(permId2.getPermId(), getEntityKind());
        EntityTypePermId permId2WithEntityKindWithUpperCase = new EntityTypePermId(permId2.getPermId().toUpperCase(), getEntityKind());

        Map<IEntityTypeId, ? extends IEntityType> map =
                getEntityTypes(sessionToken, Arrays.asList(permId1, permId2), createFetchOptions(false, false));

        assertEquals(2, map.size());

        Iterator<? extends IEntityType> iter = map.values().iterator();

        // ids in the returned types always contain entity kind (case does not matter)

        IEntityType type1 = iter.next();
        assertEquals(type1.getPermId(), permId1WithEntityKind);
        assertEquals(type1.getPermId(), permId1WithEntityKindWithUpperCase);

        IEntityType type2 = iter.next();
        assertEquals(type2.getPermId(), permId2WithEntityKind);
        assertEquals(type2.getPermId(), permId2WithEntityKindWithUpperCase);

        // keys in the result map are always the same as ids in the "get" call (case does not matter)

        type1 = map.get(permId1);
        assertEquals(type1.getPermId(), permId1WithEntityKind);
        assertEquals(type1.getPermId(), permId1WithEntityKindWithUpperCase);

        type1 = map.get(permId1WithUpperCase);
        assertEquals(type1.getPermId(), permId1WithEntityKind);
        assertEquals(type1.getPermId(), permId1WithEntityKindWithUpperCase);

        type2 = map.get(permId2);
        assertEquals(type2.getPermId(), permId2WithEntityKind);
        assertEquals(type2.getPermId(), permId2WithEntityKindWithUpperCase);

        type2 = map.get(permId2WithUpperCase);
        assertEquals(type2.getPermId(), permId2WithEntityKind);
        assertEquals(type2.getPermId(), permId2WithEntityKindWithUpperCase);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsMatchingCodeButNonMatchingKind()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String code = "ENTITY_TEST_TYPE";
        EntityTypePermId permId = createEntityType(sessionToken, code, null, new PluginPermId("validateOK"));
        final EntityTypePermId permIdMatchingCodeButNonMatchingKind;
        final EntityKind nonMatchingKind;

        if (EntityKind.SAMPLE.equals(getEntityKind()))
        {
            ExperimentTypeCreation creation = new ExperimentTypeCreation();
            creation.setCode(code);
            creation.setValidationPluginId(new PluginPermId("validateFAIL"));

            nonMatchingKind = EntityKind.EXPERIMENT;
            permIdMatchingCodeButNonMatchingKind = v3api.createExperimentTypes(sessionToken, Arrays.asList(creation)).get(0);
        } else
        {
            SampleTypeCreation creation = new SampleTypeCreation();
            creation.setCode(code);
            creation.setValidationPluginId(new PluginPermId("validateFAIL"));

            nonMatchingKind = EntityKind.SAMPLE;
            permIdMatchingCodeButNonMatchingKind = v3api.createSampleTypes(sessionToken, Arrays.asList(creation)).get(0);
        }

        Map<IEntityTypeId, ? extends IEntityType> map =
                getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(false, true));

        assertEquals(1, map.size());
        IEntityType result = (IEntityType) map.get(permId);
        assertEquals(result.getCode(), code);
        assertEquals(result.getValidationPlugin().getName(), "validateOK");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    getEntityTypes(sessionToken, Arrays.asList(permIdMatchingCodeButNonMatchingKind), createFetchOptions(false, false));
                }
            }, "Incorrect entity type entity kind. Expected '" + getEntityKind() + "' but was '" + nonMatchingKind + "'");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId permId1 = createEntityType(sessionToken, "ENTITY_TEST_TYPE_1", null, null);
        EntityTypePermId permId2 = new EntityTypePermId("IDONTEXIST");
        EntityTypePermId permId3 = createEntityType(sessionToken, "ENTITY_TEST_TYPE_2", null, null);

        Map<IEntityTypeId, ? extends IEntityType> map =
                getEntityTypes(sessionToken, Arrays.asList(permId1, permId2, permId3), createFetchOptions(false, false));

        assertEquals(2, map.size());

        Iterator<? extends IEntityType> iter = map.values().iterator();

        EntityTypePermId resultPermId1 = (EntityTypePermId) iter.next().getPermId();
        EntityTypePermId resultPermId2 = (EntityTypePermId) iter.next().getPermId();
        assertEquals(resultPermId1.getPermId(), permId1.getPermId());
        assertEquals(resultPermId2.getPermId(), permId3.getPermId());

        resultPermId1 = (EntityTypePermId) map.get(permId1).getPermId();
        resultPermId2 = (EntityTypePermId) map.get(permId3).getPermId();
        assertEquals(resultPermId1.getPermId(), permId1.getPermId());
        assertEquals(resultPermId2.getPermId(), permId3.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId permId1 = createEntityType(sessionToken, "ENTITY_TEST_TYPE", null, null);

        Map<IEntityTypeId, ? extends IEntityType> map =
                getEntityTypes(sessionToken, Arrays.asList(permId1, permId1), createFetchOptions(false, false));

        assertEquals(1, map.size());

        Iterator<? extends IEntityType> iter = map.values().iterator();

        EntityTypePermId resultPermId1 = (EntityTypePermId) iter.next().getPermId();
        assertEquals(resultPermId1.getPermId(), permId1.getPermId());

        resultPermId1 = (EntityTypePermId) map.get(permId1).getPermId();
        assertEquals(resultPermId1.getPermId(), permId1.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId permId = createEntityType(sessionToken, "ENTITY_TEST_TYPE", null, null);

        Map<IEntityTypeId, ? extends IEntityType> map = getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(false, false));

        IEntityType type = map.get(permId);

        assertEquals(((EntityTypePermId) type.getPermId()).getPermId(), permId.getPermId());
        assertEquals(type.getCode(), permId.getPermId());

        assertPropertyAssignmentsNotFetched(type);
        assertValidationPluginNotFetched(type);
    }

    @Test
    public void testGetByIdsWithPropertyAssignments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        assignmentCreation.setPluginId(new PluginPermId("properties"));

        EntityTypePermId permId = createEntityType(sessionToken, "ENTITY_TEST_TYPE", Arrays.asList(assignmentCreation), null);

        Map<IEntityTypeId, ? extends IEntityType> map = getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(true, false));

        IEntityType type = map.get(permId);

        assertEquals(((EntityTypePermId) type.getPermId()).getPermId(), permId.getPermId());
        assertEquals(type.getCode(), permId.getPermId());
        assertPropertyAssignments(type.getPropertyAssignments(), "properties", "ENTITY_TEST_TYPE.DESCRIPTION");

        assertValidationPluginNotFetched(type);
    }

    @Test
    public void testGetByIdsWithInternalPropertyAssignments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String entityTypeCode = "ENTITY_TEST_TYPE";
        String propertyTypeCodeWithDolar = "$PLATE_GEOMETRY";

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyTypeCodeWithDolar));
        assignmentCreation.setPluginId(new PluginPermId("properties"));

        EntityTypePermId permId = createEntityType(sessionToken, entityTypeCode, Arrays.asList(assignmentCreation), null);

        Map<IEntityTypeId, ? extends IEntityType> map = getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(true, false));

        IEntityType type = map.get(permId);

        assertEquals(((EntityTypePermId) type.getPermId()).getPermId(), permId.getPermId());
        assertEquals(type.getCode(), permId.getPermId());

        assertEquals(type.getPropertyAssignments().size(), 1);
        PropertyAssignment assignment = type.getPropertyAssignments().get(0);
        assertEquals(assignment.getPermId().getEntityTypeId(), new EntityTypePermId(entityTypeCode, getEntityKind()));
        assertEquals(((PropertyTypePermId) assignment.getPermId().getPropertyTypeId()).getPermId(), propertyTypeCodeWithDolar);
        assertEquals(assignment.getPropertyType().getCode(), propertyTypeCodeWithDolar);
        assertEquals(((PropertyTypePermId) assignment.getPropertyType().getPermId()).getPermId(), propertyTypeCodeWithDolar);

        assertValidationPluginNotFetched(type);
    }

    @Test
    public void testGetByIdsWithValidationPlugin()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId permId = createEntityType(sessionToken, "ENTITY_TEST_TYPE", null, new PluginPermId("validateOK"));

        Map<IEntityTypeId, ? extends IEntityType> map = getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(false, true));

        IEntityType type = map.get(permId);

        assertEquals(((EntityTypePermId) type.getPermId()).getPermId(), permId.getPermId());
        assertEquals(type.getCode(), permId.getPermId());
        assertEquals(type.getValidationPlugin().getName(), "validateOK");

        assertPropertyAssignmentsNotFetched(type);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        EntityTypePermId permId = createEntityType(adminSessionToken, "ENTITY_TEST_TYPE", null, null);

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(false, false));
                    }
                });
        } else
        {
            Map<IEntityTypeId, ? extends IEntityType> map = getEntityTypes(sessionToken, Arrays.asList(permId), createFetchOptions(false, false));
            assertEquals(map.size(), 1);

            EntityTypePermId resultPermId = (EntityTypePermId) map.get(permId).getPermId();
            assertEquals(resultPermId.getPermId(), permId.getPermId());
        }

        v3api.logout(sessionToken);
    }

}
