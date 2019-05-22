/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.PostgresSearchDAO;
import org.testng.annotations.Test;

import java.util.Set;

public class SampleSearchManagerDevTest
{
    @Test
    public void testPipeline()
    {
        final Long userId = 2L; // Default ETL Server that is supposed to see everything
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        final SampleFetchOptions sampleFetchOption = new SampleFetchOptions();
        final PostgresSearchDAO searchDAO = new PostgresSearchDAO();
        final SortAndPage sortAndPage = new SortAndPage();
        final SampleSearchManager sampleSearchManager = new SampleSearchManager(searchDAO, sortAndPage);
        final Set<Long> unSortedResults = sampleSearchManager.searchForIDs(userId, sampleSearchCriteria);
        // List<Long> sortedResults = sampleSearchManager.sortAndPage(unSortedResults, sampleSearchCriteria, sampleFetchOption);
    }
}
