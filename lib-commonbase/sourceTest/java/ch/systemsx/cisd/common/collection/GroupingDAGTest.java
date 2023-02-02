/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Jakub Straszewski
 */
public class GroupingDAGTest extends AssertJUnit
{

    @Test
    public void testPerformance()
    {
        int n = 100000;
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        List<String> superParentAdjacencyList = new LinkedList<String>();
        for (int i = 0; i < n - 1; i++)
        {
            String childCode = "CHILD" + i;
            superParentAdjacencyList.add(childCode);
            adjacencyMap.put(childCode, new ArrayList<String>());
        }
        adjacencyMap.put("PARENT", superParentAdjacencyList);

        adjacencyMap.put("CHAIN_0", new ArrayList<String>());

        for (int i = 1; i < n; i++)
        {
            String childCode = "CHAIN_" + i;
            adjacencyMap.put(childCode, Arrays.asList("CHAIN_" + (i - 1)));
        }

        List<List<String>> groups = sortTopologically(adjacencyMap);

        assertAllEntitiesPresent(adjacencyMap.keySet(), groups);

        assertEquals(n, groups.size());

        assertEquals(2, groups.get(0).size()); // parent and first chain
        groups.remove(0);

        assertEquals(n, groups.get(0).size()); // children and one chain
        groups.remove(0);

        for (List<String> group : groups)
        {
            assertEquals(1, group.size());
        }
    }

    @Test
    public void testIndependentWithOneExtraPair()
    {
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        adjacencyMap.put("A", new ArrayList<String>());
        adjacencyMap.put("B", Arrays.asList("A"));
        adjacencyMap.put("C", Arrays.asList("A", "B"));
        adjacencyMap.put("D", Arrays.asList("C", "B", "A"));
        adjacencyMap.put("E", Arrays.asList("C", "D"));
        adjacencyMap.put("X", Arrays.asList("Z"));

        List<List<String>> groups = sortTopologically(adjacencyMap);

        assertEquals("[[E, X], [D, Z], [C], [B], [A]]", groups.toString());
    }

    @Test
    public void testIndependent()
    {
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        adjacencyMap.put("A", new ArrayList<String>());
        adjacencyMap.put("B", Arrays.asList("A"));
        adjacencyMap.put("C", Arrays.asList("A", "B"));
        adjacencyMap.put("D", Arrays.asList("C", "B", "A"));
        adjacencyMap.put("E", Arrays.asList("C", "D"));

        List<List<String>> groups = sortTopologically(adjacencyMap);

        assertAllEntitiesPresent(adjacencyMap.keySet(), groups);

        assertEquals("[[E], [D], [C], [B], [A]]", groups.toString());
    }

    @Test
    public void testWithParent()
    {
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        adjacencyMap.put("P1", Arrays.asList("A1", "A2", "A3"));
        adjacencyMap.put("P2", Arrays.asList("A1", "A2", "A3"));
        adjacencyMap.put("A1", new ArrayList<String>());
        adjacencyMap.put("A2", new ArrayList<String>());
        adjacencyMap.put("A3", new ArrayList<String>());
        adjacencyMap.put("I", new ArrayList<String>());

        List<List<String>> groups = sortTopologically(adjacencyMap);

        assertAllEntitiesPresent(adjacencyMap.keySet(), groups);

        for (List<String> list : groups)
        {
            Collections.sort(list);
        }

        assertEquals("[[I, P1, P2], [A1, A2, A3]]", groups.toString());
    }

    @Test
    public void testWithParentofParent()
    {
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        adjacencyMap.put("P", Arrays.asList("A"));
        adjacencyMap.put("A", Arrays.asList("B"));
        adjacencyMap.put("B", new ArrayList<String>());

        List<List<String>> groups = sortTopologically(adjacencyMap);

        assertAllEntitiesPresent(adjacencyMap.keySet(), groups);

        for (List<String> list : groups)
        {
            Collections.sort(list);
        }

        assertEquals("[[P], [A], [B]]", groups.toString());
    }

    @Test
    public void testEmpty()
    {
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        List<List<String>> groups = sortTopologically(adjacencyMap);

        assertEquals(0, groups.size());
    }

    private void assertAllEntitiesPresent(Collection<String> original, List<List<String>> groups)
    {
        HashSet<String> allItems = new HashSet<String>(original);

        for (List<String> group : groups)
        {
            for (String item : group)
            {
                if (!allItems.contains(item))
                {
                    fail("The items in original and groped lists do not match! (" + item + ") "
                            + original + " " + groups);
                }
                allItems.remove(item);
            }
        }
        if (allItems.size() > 0)
        {
            fail("The items in original and groped lists do not match! " + original + " " + groups);
        }
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCyclicGraph()
    {
        HashMap<String, Collection<String>> adjacencyMap =
                new HashMap<String, Collection<String>>();

        adjacencyMap.put("A", Arrays.asList("B"));
        adjacencyMap.put("B", Arrays.asList("C"));
        adjacencyMap.put("C", Arrays.asList("A"));

        sortTopologically(adjacencyMap);
    }

    private List<List<String>> sortTopologically(final Map<String, Collection<String>> adjacencyMap)
    {
        return GroupingDAG.groupByDepencies(adjacencyMap);
    }
}
