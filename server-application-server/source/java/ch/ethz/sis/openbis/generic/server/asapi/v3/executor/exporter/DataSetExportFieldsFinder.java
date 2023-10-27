/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

public class DataSetExportFieldsFinder extends AbstractExportFieldsFinder<DataSetType>
{

    @Override
    public SearchResult<DataSetType> findEntityTypes(final Set<IPropertyTypeId> properties,
            final IApplicationServerInternalApi applicationServerApi, final String sessionToken)
    {
        final DataSetTypeSearchCriteria typeSearchCriteria = new DataSetTypeSearchCriteria();
        typeSearchCriteria.withPropertyAssignments().withPropertyType().withIds().thatIn(properties);

        final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();

        return applicationServerApi.searchDataSetTypes(sessionToken, typeSearchCriteria, fetchOptions);
    }

    @Override
    public String getPermId(final DataSetType dataSetType)
    {
        return dataSetType.getPermId().getPermId();
    }

}
