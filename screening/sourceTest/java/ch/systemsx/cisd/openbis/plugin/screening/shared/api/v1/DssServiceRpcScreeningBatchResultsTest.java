/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.multiplexer.BatchesResults;

/**
 * @author pkupczyk
 */
public class DssServiceRpcScreeningBatchResultsTest extends AssertJUnit
{

    @Test
    public void testNullResults()
    {
        BatchesResults<String> results = new BatchesResults<String>();
        results.addBatchResults(null);
        results.addBatchResults(null);

        assertTrue(results.withDuplicates().isEmpty());
        assertTrue(results.withoutDuplicates().isEmpty());
    }

    @Test
    public void testEmptyResults()
    {
        BatchesResults<String> results = new BatchesResults<String>();
        results.addBatchResults(new ArrayList<String>());
        results.addBatchResults(new ArrayList<String>());

        assertTrue(results.withDuplicates().isEmpty());
        assertTrue(results.withoutDuplicates().isEmpty());
    }

    @Test
    public void testNotEmptyResults()
    {
        BatchesResults<String> results = new BatchesResults<String>();
        results.addBatchResults(Arrays.asList("a", "c", "e"));
        results.addBatchResults(Arrays.asList("c", "d"));

        assertEquals(Arrays.asList("a", "c", "e", "c", "d"), results.withDuplicates());
        assertEquals(Arrays.asList("a", "c", "e", "d"), results.withoutDuplicates());
    }

}
