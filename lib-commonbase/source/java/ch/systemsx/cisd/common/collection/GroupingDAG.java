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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * The implementation of a DAG, that returns the group of items, where the first group includes the items without dependencies, the second group
 * contain only dependencies to the first group etc.
 * 
 * @author Jakub Straszewski
 */
public class GroupingDAG<T>
{

    /**
     * The algorithm works on three internal structures.
     * <p>
     * <code>graph</code> as the adjacency list, where vertice from A to B means that a must be executed before B. <code> dependenciesCount </code> is
     * the priority table, which keeps information for each item, how many items should be still taken before this one. <code>queue </code> is the
     * priority queue, that uses <code> dependenciesCount </code> as the priority key.
     * <p>
     * At each level algorithm takes the items that have zero dependencies, then updates the dependency counts, and repeats the procedure until there
     * are no more vertices. If it cannot proceed at some stage - it means that there is a circular dependency and the exception is being thrown.
     */
    private final Map<T, Integer> dependenciesCount;

    private final Map<T, Collection<T>> graph;

    private final PriorityQueue<PriorityItem> queue;

    private final List<List<T>> sortedGroups;

    private class PriorityItem implements Comparable<PriorityItem>
    {
        final T item;

        final Integer priority;

        /**
         * Cretes the object with priority at the time of the object creation
         */
        public PriorityItem(T item)
        {
            this.item = item;
            this.priority = dependenciesCount.get(item);
        }

        @Override
        public int compareTo(PriorityItem o)
        {
            return priority.compareTo(o.priority);
        }

        @Override
        public String toString()
        {
            return "<" + item + ", " + priority + ">";
        }
    }

    /**
     * @param graph must be non-empty graph
     */
    private GroupingDAG(Map<T, Collection<T>> graph)
    {
        this.graph = graph;
        this.dependenciesCount = new HashMap<T, Integer>();
        this.queue = new PriorityQueue<PriorityItem>();
        this.sortedGroups = new LinkedList<List<T>>();
        initialize();
        sort();
    }

    /**
     * Return the items in the list of groups, where the earlier groups are independent on the latter ones.
     * 
     * @param graph the connection graph(A).contains(B) means that A must be scheduled BEFORE B
     */
    public static <T> List<List<T>> groupByDepencies(Map<T, Collection<T>> graph)
    {
        if (graph.size() == 0)
        {
            return Collections.emptyList();
        }

        GroupingDAG<T> dag = new GroupingDAG<T>(graph);
        return dag.sortedGroups;
    }

    private void addNoDependency(T item)
    {
        if (false == dependenciesCount.containsKey(item))
        {
            dependenciesCount.put(item, 0);
        }
    }

    private void addDependency(T dependant)
    {
        int count = 0;
        if (dependenciesCount.containsKey(dependant))
        {
            count = dependenciesCount.get(dependant);
        }
        dependenciesCount.put(dependant, count + 1);
    }

    private void initialize()
    {
        for (Map.Entry<T, Collection<T>> entry : graph.entrySet())
        {
            addNoDependency(entry.getKey());
            for (T dependant : entry.getValue())
            {
                addDependency(dependant);
            }
        }

        for (T item : graph.keySet())
        {
            queue.add(new PriorityItem(item));
        }
    }

    // because the implementation of the priority queue does not allow to change priorities of items
    // in the queue, instead we insert the same items several times.
    // if the dependency count of the item is -1 - it means, that we have already used it
    private void sort()
    {
        while (false == queue.isEmpty())
        {
            List<T> levelItems = new LinkedList<T>();

            while (false == queue.isEmpty() && peekCount() < 0)
            {
                queue.poll(); // remove the elements that are duplicated in the queue
            }

            if (peekCount() > 0)
            {
                T cycleRoot = queue.peek().item;
                Collection<T> cycle = graph.get(queue.peek().item);
                throw new CycleFoundException(cycleRoot, cycle);
            }

            while (false == queue.isEmpty() && peekCount() <= 0)
            {
                T item = queue.poll().item;

                if (dependenciesCount.get(item) == 0)
                {
                    levelItems.add(item);
                    dependenciesCount.put(item, -1);
                }
            }
            sortedGroups.add(levelItems);

            if (false == queue.isEmpty())
            {
                // we don't need to clean if we know, that this is the last loop
                updateQueueAfterTheLevelCompleted(levelItems);
            }
        }
    }

    private Integer peekCount()
    {
        return dependenciesCount.get(queue.peek().item);
    }

    /**
     * after all items that have no dependencies have been taken, we remove further dependencies to those items
     */
    private void updateQueueAfterTheLevelCompleted(List<T> levelItems)
    {
        HashSet<T> allSonsInTheLevel = new HashSet<T>();

        for (T item : levelItems)
        {
            Collection<T> items = graph.get(item);
            if (items != null)
            {
                for (T son : items)
                {
                    allSonsInTheLevel.add(son);
                    dependenciesCount.put(son, dependenciesCount.get(son) - 1);
                }
            }
        }
        for (T son : allSonsInTheLevel)
        {
            queue.add(new PriorityItem(son));
        }
    }
}
