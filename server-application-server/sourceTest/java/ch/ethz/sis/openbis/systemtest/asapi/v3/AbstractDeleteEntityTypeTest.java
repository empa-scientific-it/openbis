/*
 * Copyright 2017 ETH Zuerich, SIS
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.AbstractObjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public abstract class AbstractDeleteEntityTypeTest extends AbstractTest
{

    protected abstract EntityTypePermId createEntityType(String sessionToken, String entityTypeCode);

    protected abstract IObjectId createEntity(String sessionToken, IEntityTypeId entityTypeId);

    protected abstract AbstractObjectDeletionOptions<?> createEntityTypeDeletionOptions();

    protected abstract ICodeHolder getEntityType(String sessionToken, IEntityTypeId entityTypeId);

    protected abstract void deleteEntityType(String sessionToken, List<IEntityTypeId> entityTypeIds, AbstractObjectDeletionOptions<?> options);

    @Test
    public void testDeleteExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String entityTypeCode = "TEST_TYPE_" + System.currentTimeMillis();
        EntityTypePermId entityTypeId = createEntityType(sessionToken, entityTypeCode);

        ICodeHolder entityType = getEntityType(sessionToken, entityTypeId);
        assertNotNull(entityType);
        assertEquals(entityType.getCode(), entityTypeCode);

        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();
        options.setReason("test reason");

        deleteEntityType(sessionToken, Arrays.asList(entityTypeId), options);

        entityType = getEntityType(sessionToken, entityTypeId);
        assertNull(entityType);
    }

    @Test
    public void testDeleteNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String entityTypeCode = "TEST_TYPE_" + System.currentTimeMillis();
        EntityTypePermId entityTypeId = new EntityTypePermId(entityTypeCode);

        ICodeHolder entityType = getEntityType(sessionToken, entityTypeId);
        assertNull(entityType);

        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();
        options.setReason("test reason");

        deleteEntityType(sessionToken, Arrays.asList(entityTypeId), options);

        entityType = getEntityType(sessionToken, entityTypeId);
        assertNull(entityType);
    }

    @Test
    public void testDeleteUsed()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId entityTypeId = createEntityType(sessionToken, "TEST_TYPE_" + System.currentTimeMillis());
        createEntity(sessionToken, entityTypeId);

        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();
        options.setReason("test reason");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteEntityType(sessionToken, Arrays.asList(entityTypeId), options);
                }
            }, "'" + entityTypeId.getPermId() + "' is being used");
    }

    @Test
    public void testDeleteWithoutIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();
        options.setReason("test reason");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteEntityType(sessionToken, null, options);
                }
            }, "Entity ids cannot be null");
    }

    @Test
    public void testDeleteWithoutOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId entityTypeId = createEntityType(sessionToken, "TEST_TYPE_" + System.currentTimeMillis());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteEntityType(sessionToken, Arrays.asList(entityTypeId), null);
                }
            }, "Deletion options cannot be null");
    }

    @Test
    public void testDeleteWithoutReason()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId entityTypeId = createEntityType(sessionToken, "TEST_TYPE_" + System.currentTimeMillis());
        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteEntityType(sessionToken, Arrays.asList(entityTypeId), options);
                }
            }, "Deletion reason cannot be null");
    }

    @Test
    public void testDeleteAsInstanceAdmin()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String entityTypeCode = "TEST_TYPE_" + System.currentTimeMillis();
        EntityTypePermId entityTypeId = createEntityType(sessionToken, entityTypeCode);

        ICodeHolder entityType = getEntityType(sessionToken, entityTypeId);
        assertNotNull(entityType);
        assertEquals(entityType.getCode(), entityTypeCode);

        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();
        options.setReason("test reason");

        deleteEntityType(sessionToken, Arrays.asList(entityTypeId), options);

        entityType = getEntityType(sessionToken, entityTypeId);
        assertNull(entityType);
    }

    @Test
    public void testDeleteAsNonInstanceAdmin()
    {
        String instanceAdminSessionToken = v3api.login(TEST_USER, PASSWORD);
        String nonInstanceAdminSessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        String entityTypeCode = "TEST_TYPE_" + System.currentTimeMillis();
        EntityTypePermId entityTypeId = createEntityType(instanceAdminSessionToken, entityTypeCode);

        ICodeHolder entityType = getEntityType(instanceAdminSessionToken, entityTypeId);
        assertNotNull(entityType);
        assertEquals(entityType.getCode(), entityTypeCode);

        AbstractObjectDeletionOptions<?> options = createEntityTypeDeletionOptions();
        options.setReason("test reason");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteEntityType(nonInstanceAdminSessionToken, Arrays.asList(entityTypeId), options);
                }
            }, entityTypeId);
    }

}
