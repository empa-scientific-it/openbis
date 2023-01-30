/*
 * Copyright 2018 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.AbstractObjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;

/**
 * @author pkupczyk
 */
public class DeleteDataSetTypeTest extends AbstractDeleteEntityTypeTest
{

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode)
    {
        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode(entityTypeCode);

        List<EntityTypePermId> permIds = v3api.createDataSetTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected IObjectId createEntity(String sessionToken, IEntityTypeId entityTypeId)
    {
        String code = "TEST_DATA_SET_" + System.currentTimeMillis();

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("test/location/" + code);
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setDataSetKind(DataSetKind.PHYSICAL);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);
        creation.setTypeId(entityTypeId);

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected AbstractObjectDeletionOptions<?> createEntityTypeDeletionOptions()
    {
        return new DataSetTypeDeletionOptions();
    }

    @Override
    protected ICodeHolder getEntityType(String sessionToken, IEntityTypeId entityTypeId)
    {
        return v3api.getDataSetTypes(sessionToken, Collections.singletonList(entityTypeId), new DataSetTypeFetchOptions()).get(entityTypeId);
    }

    @Override
    protected void deleteEntityType(String sessionToken, List<IEntityTypeId> entityTypeIds, AbstractObjectDeletionOptions<?> options)
    {
        v3api.deleteDataSetTypes(sessionToken, entityTypeIds, (DataSetTypeDeletionOptions) options);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeDeletionOptions o = new DataSetTypeDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteDataSetTypes(sessionToken,
                Arrays.asList(new EntityTypePermId("TEST-LOGGING-1"), new EntityTypePermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-data-set-types  DATA_SET_TYPE_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('DataSetTypeDeletionOptions[reason=test-reason]')");
    }

}
