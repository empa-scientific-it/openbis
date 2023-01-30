/*
 * Copyright 2020 ETH Zuerich, SIS
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
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetSearchPropertyTest extends AbstractSearchPropertyTest
{

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, PropertyTypePermId propertyTypeId)
    {
        return createADataSetType(sessionToken, false, propertyTypeId);
    }

    @Override
    protected ObjectPermId createEntity(String sessionToken, String code, EntityTypePermId entityTypeId,
            String propertyType, String value)
    {
        DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(entityTypeId);
        dataSetCreation.setProperty(propertyType, value);
        return v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria()
    {
        return new DataSetSearchCriteria();
    }

    @Override
    protected List<? extends IPermIdHolder> search(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        return v3api.searchDataSets(sessionToken, (DataSetSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

}
