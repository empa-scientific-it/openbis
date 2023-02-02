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

package ch.systemsx.cisd.common.multiplexer;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class BatchesResultsTest extends AssertJUnit
{

    @Test
    public void testNullResults()
    {
        BatchesResults<Integer, String> results = new BatchesResults<Integer, String>();
        results.addBatchResults(null);
        results.addBatchResults(null);

        assertTrue(results.getMergedBatchResultsWithDuplicates().isEmpty());
        assertTrue(results.getMergedBatchResultsWithoutDuplicates().isEmpty());
    }

    @Test
    public void testEmptyResults()
    {
        BatchesResults<Integer, String> results = new BatchesResults<Integer, String>();
        results.addBatchResults(new BatchResults<Integer, String>(1, new ArrayList<String>()));
        results.addBatchResults(new BatchResults<Integer, String>(2, new ArrayList<String>()));

        assertTrue(results.getMergedBatchResultsWithDuplicates().isEmpty());
        assertTrue(results.getMergedBatchResultsWithoutDuplicates().isEmpty());
    }

    @Test
    public void testNotEmptyResults()
    {
        BatchesResults<Integer, String> results = new BatchesResults<Integer, String>();
        results.addBatchResults(new BatchResults<Integer, String>(1, Arrays.asList("a", "c", "e")));
        results.addBatchResults(new BatchResults<Integer, String>(2, Arrays.asList("c", "d")));

        assertEquals(Arrays.asList("a", "c", "e", "c", "d"), results.getMergedBatchResultsWithDuplicates());
        assertEquals(Arrays.asList("a", "c", "e", "d"), results.getMergedBatchResultsWithoutDuplicates());
    }

}
