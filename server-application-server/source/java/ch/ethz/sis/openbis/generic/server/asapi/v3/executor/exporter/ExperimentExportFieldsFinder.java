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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

public class ExperimentExportFieldsFinder extends AbstractExportFieldsFinder<ExperimentType>
{
    @Override 
    public SearchResult<ExperimentType> findEntityTypes(final Set<IPropertyTypeId> properties,
            final IApplicationServerInternalApi applicationServerApi, final String sessionToken)
    {
        // TODO: !!!!!!!!! Not sure if it's not a bug in the search engine!!!!!!!!
        // The following is produced:
        //
        // 2023-10-26 18:52:14,762 INFO  [main] OPERATION.AbstractSQLExecutor - QUERY: SELECT DISTINCT t0.id
        //FROM property_types t0
        //WHERE (t0.code IN (SELECT UNNEST(?)))
        //2023-10-26 18:52:14,762 INFO  [main] OPERATION.AbstractSQLExecutor - ARGS: [[DESCRIPTION, GENDER]]
        //2023-10-26 18:52:14,770 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS COUNT: 2
        //2023-10-26 18:52:14,770 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS: [{id=1}, {id=13}]
        //2023-10-26 18:52:14,771 INFO  [main] OPERATION.AbstractSQLExecutor - QUERY: SELECT DISTINCT t0.id
        //FROM sample_type_property_types t0 <--- why sample???????
        //WHERE (t0.prty_id IN (SELECT UNNEST(?)))
        //2023-10-26 18:52:14,771 INFO  [main] OPERATION.AbstractSQLExecutor - ARGS: [[1, 13]]
        //2023-10-26 18:52:14,771 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS COUNT: 3
        //2023-10-26 18:52:14,771 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS: [{id=3}, {id=4}, {id=17}]
        //2023-10-26 18:52:14,771 INFO  [main] OPERATION.AbstractSQLExecutor - QUERY: SELECT DISTINCT t0.id
        //FROM experiment_types t0
        //WHERE (t0.id IN (SELECT UNNEST(?))) <---- why t0.id??????
        //2023-10-26 18:52:14,771 INFO  [main] OPERATION.AbstractSQLExecutor - ARGS: [[17, 3, 4]]
        //2023-10-26 18:52:14,772 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS COUNT: 1
        //2023-10-26 18:52:14,772 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS: [{id=3}]

        final ExperimentTypeSearchCriteria typeSearchCriteria = new ExperimentTypeSearchCriteria();
        typeSearchCriteria.withPropertyAssignments().withPropertyType().withIds().thatIn(properties);

        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();

        return applicationServerApi.searchExperimentTypes(sessionToken, typeSearchCriteria, fetchOptions);
    }

    @Override 
    public String getPermId(final ExperimentType experimentType)
    {
        return experimentType.getPermId().getPermId();
    }
    
}
