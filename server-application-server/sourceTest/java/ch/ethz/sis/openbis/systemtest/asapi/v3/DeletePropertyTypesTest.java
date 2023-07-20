/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class DeletePropertyTypesTest extends AbstractTest
{

    @DataProvider
    private Object[][] providerTestDeleteAuthorization()
    {
        return new Object[][] {
                { "NEW_NON_INTERNAL", SYSTEM_USER, SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_USER, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [NEW_NON_INTERNAL]" },

                { "NEW_NON_INTERNAL", TEST_USER, SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [NEW_NON_INTERNAL]" },

                { "$NEW_INTERNAL", SYSTEM_USER, SYSTEM_USER, null },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_USER, "Access denied to object with PropertyTypePermId = [$NEW_INTERNAL]" },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [$NEW_INTERNAL]" },
        };
    }

    @Test(dataProvider = "providerTestDeleteAuthorization")
    public void testDeleteAuthorization(String propertyTypeCode, String propertyTypeRegistrator, String propertyTypeDeleter, String expectedError)
    {
        String registratorSessionToken =
                propertyTypeRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyTypeRegistrator, PASSWORD);
        String deleterSessionToken = propertyTypeDeleter.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyTypeDeleter, PASSWORD);

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode(propertyTypeCode);
        creation.setDataType(DataType.VARCHAR);
        creation.setLabel("Test label");
        creation.setDescription("Test description");
        creation.setManagedInternally(propertyTypeCode.startsWith("$"));
        creation.setMultiValue(false);
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(registratorSessionToken, Arrays.asList(creation));
        assertEquals(ids.size(), 1);

        PropertyTypeDeletionOptions options = new PropertyTypeDeletionOptions();
        options.setReason("testing");

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.deletePropertyTypes(deleterSessionToken, ids, options);
                }
            }, expectedError);
    }

    @Test
    public void testDeletion()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = createAPropertyType(sessionToken, DataType.VARCHAR);
        PropertyTypeDeletionOptions deletionOptions = new PropertyTypeDeletionOptions();
        deletionOptions.setReason("testing property type deletion");

        // When
        v3api.deletePropertyTypes(sessionToken, Arrays.asList(id), deletionOptions);

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        Map<IPropertyTypeId, PropertyType> result = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions);
        assertEquals(result.size(), 0);
        v3api.logout(sessionToken);
    }

    @Test
    public void testDeleteUsedPropertyTyerm()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeDeletionOptions deletionOptions = new PropertyTypeDeletionOptions();
        deletionOptions.setReason("test");
        PropertyTypePermId id = new PropertyTypePermId("COMMENT");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.deletePropertyTypes(sessionToken, Arrays.asList(id), deletionOptions);
                }
            },
                // Then
                "Property Type 'COMMENT' is being used. Delete all connected data  first.");

        v3api.logout(sessionToken);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Deletion reason cannot be null.*")
    public void testDeleteWithoutReason()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = createAPropertyType(sessionToken, DataType.VARCHAR);
        PropertyTypeDeletionOptions deletionOptions = new PropertyTypeDeletionOptions();

        // When
        v3api.deletePropertyTypes(sessionToken, Arrays.asList(id), deletionOptions);

        // Then, see method annotation
    }

    @Test(dataProvider = "usersNotAllowedToDeletePropertyTypes")
    public void testDeleteWithUserCausingAuthorizationFailure(final String user)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = createAPropertyType(sessionToken, DataType.VARCHAR);
        v3api.logout(sessionToken);
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PropertyTypeDeletionOptions deletionOptions = new PropertyTypeDeletionOptions();
                    deletionOptions.setReason("test");
                    v3api.deletePropertyTypes(sessionToken, Arrays.asList(id), deletionOptions);
                }
            }, id);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyTypeDeletionOptions o = new PropertyTypeDeletionOptions();
        o.setReason("test-reason");

        v3api.deletePropertyTypes(sessionToken, Arrays.asList(new PropertyTypePermId("TEST-LOGGING-1"), new PropertyTypePermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-property-types  PROPERTY_TYPES_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('PropertyTypeDeletionOptions[reason=test-reason]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToDeletePropertyTypes()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

}
