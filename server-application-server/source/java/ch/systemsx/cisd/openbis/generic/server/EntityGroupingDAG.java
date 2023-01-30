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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.common.collection.GroupingDAG;

/**
 * Implementation of the topological sort to group entities in the groups of items.
 * <p>
 * This class is designed for inheritance. It is required to provide a <code>getCode()</code> method, that calculates the code of the entity. For the
 * purpose of creating dependency graph it is essential to implement <code>getDependent</code> and <code>getDependencies</code>, but please have in
 * mind, that each dependency should be listed only once.
 * 
 * @author Jakub Straszewski
 */
public abstract class EntityGroupingDAG<T>
{
    private Collection<T> items;

    private HashMap<String, T> identifierMap;

    private HashMap<String, Collection<String>> adjacencyGraph;

    private List<List<T>> resultGroups;

    public EntityGroupingDAG(Collection<T> items)
    {
        this.items = items;
    }

    /**
     * Calculate dependency groups, or return them if they already have been calculated
     */
    public List<List<T>> getDependencyGroups()
    {
        if (resultGroups == null)
        {
            buildGraph();
            calculateResults();
        }
        return resultGroups;
    }

    /**
     * Get the unique string representation of the <code>item</code>. The value of this function must be the same as values returned from
     * <code>getDependent</code> and <code>getDependencies</code>
     */
    protected abstract String getCode(T item);

    /**
     * List entities that must be grouped AFTER the <code>item</code>
     */
    protected abstract Collection<String> getDependent(T item);

    /**
     * List entities that must be grouped BEFORE the <code>item</code>
     */
    protected abstract Collection<String> getDependencies(T item);

    private void buildGraph()
    {
        identifierMap = new HashMap<String, T>();
        adjacencyGraph = new HashMap<String, Collection<String>>();
        for (T item : items)
        {
            String code = getCode(item);
            identifierMap.put(code, item);
            adjacencyGraph.put(code, new LinkedList<String>());
        }

        for (T item : items)
        {
            String code = getCode(item);

            Collection<String> dependent = getDependent(item);
            if (dependent != null)
            {
                for (String depCode : dependent)
                {
                    if (identifierMap.containsKey(depCode))
                    {
                        adjacencyGraph.get(code).add(depCode);
                    }
                }
            }

            Collection<String> dependencies = getDependencies(item);
            if (dependencies != null)
            {
                for (String depCode : dependencies)
                {
                    if (identifierMap.containsKey(depCode))
                    {
                        adjacencyGraph.get(depCode).add(code);
                    }
                }
            }
        }
    }

    private void calculateResults()
    {
        List<List<String>> identifierGroups = GroupingDAG.groupByDepencies(adjacencyGraph);
        resultGroups = new LinkedList<List<T>>();

        for (List<String> listOfIdentifiers : identifierGroups)
        {
            List<T> listOfItems = new LinkedList<T>();

            for (String identifier : listOfIdentifiers)
            {
                listOfItems.add(identifierMap.get(identifier));
            }
            resultGroups.add(listOfItems);
        }
    }
}
