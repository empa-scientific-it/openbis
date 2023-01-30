/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetUtils
{
    private static final class Vertex implements Comparable<Vertex>
    {
        private final Long id;

        private final boolean inside;

        private Set<Vertex> containers = new HashSet<Vertex>();

        Vertex(Long id, boolean inside)
        {
            this.id = id;
            this.inside = inside;
        }

        boolean isInsideAndAllAncestorContainersAreInside()
        {
            if (inside == false)
            {
                return false;
            }
            for (Vertex container : containers)
            {
                if (container.isInsideAndAllAncestorContainersAreInside() == false)
                {
                    return false;
                }
            }
            return true;
        }

        void addContainer(Vertex container)
        {
            containers.add(container);
        }

        @Override
        public int compareTo(Vertex v)
        {
            return id.intValue() - v.id.intValue();
        }
    }

    /**
     * Returns the technical ids of all data sets which fulfill the following criteria:
     * <ol>
     * <li>The data set is from the graph created by all descendants of type component with the specified list of data sets as root nodes.
     * <li>All ancestors of type container of the data set are part of the graph. Those ancestors are ignored for the specified data sets if
     * <code>isOriginalDeletion == true</code>.
     * </ol>
     */
    public static List<TechId> getAllDeletableComponentsRecursively(List<TechId> dataSetIds,
            boolean isOriginalDeletion, IDatasetLister datasetLister, IDAOFactory daoFactory)
    {
        Set<Long> dataSetIdsAsSet = new HashSet<Long>(TechId.asLongs(dataSetIds));
        Set<TechId> allIds = getAllPotentiallyDeletableComponentsRecursively(dataSetIds, daoFactory);
        if (allIds.isEmpty())
        {
            return new ArrayList<TechId>();
        }
        List<Long> allIdsAsLongs = TechId.asLongs(allIds);
        Map<Long, Set<Long>> containerIds = datasetLister.listContainerIds(allIdsAsLongs);
        for (Long id : allIdsAsLongs)
        {
            if (containerIds.containsKey(id) == false || (isOriginalDeletion && dataSetIdsAsSet.contains(id)))
            {
                containerIds.put(id, Collections.<Long> emptySet());
            }
        }
        List<Vertex> graph = createGraph(containerIds);
        List<TechId> result = new ArrayList<TechId>();
        for (Vertex vertex : graph)
        {
            if (vertex.isInsideAndAllAncestorContainersAreInside())
            {
                result.add(new TechId(vertex.id));
            }
        }
        return result;
    }

    private static Set<TechId> getAllPotentiallyDeletableComponentsRecursively(List<TechId> dataSetIds,
            IDAOFactory daoFactory)
    {
        IRelationshipTypeDAO relationshipTypeDAO = daoFactory.getRelationshipTypeDAO();
        Long relationshipTypeId = RelationshipUtils.getContainerComponentRelationshipType(relationshipTypeDAO).getId();
        IDataDAO dataDAO = daoFactory.getDataDAO();
        Set<TechId> allIds = new LinkedHashSet<TechId>();
        Set<TechId> containedDataSetIds = new LinkedHashSet<TechId>();
        containedDataSetIds.addAll(dataSetIds);
        while (allIds.addAll(containedDataSetIds))
        {
            containedDataSetIds = dataDAO.findChildrenIds(containedDataSetIds, relationshipTypeId);
        }
        return allIds;
    }

    private static List<Vertex> createGraph(Map<Long, Set<Long>> containerIdsByComponentIds)
    {
        Map<Long, Vertex> verticesById = new LinkedHashMap<Long, Vertex>();
        Set<Long> componentIds = containerIdsByComponentIds.keySet();
        Set<Entry<Long, Set<Long>>> entrySet = containerIdsByComponentIds.entrySet();
        for (Entry<Long, Set<Long>> entry : entrySet)
        {
            Long componentId = entry.getKey();
            Vertex componentVertex = getOrCreateVertex(componentId, componentIds, verticesById);
            Set<Long> containerIds = entry.getValue();
            for (Long containerId : containerIds)
            {
                Vertex containerVertex = getOrCreateVertex(containerId, componentIds, verticesById);
                componentVertex.addContainer(containerVertex);
            }
        }
        List<Vertex> graph = new ArrayList<Vertex>(verticesById.values());
        Collections.sort(graph);
        return graph;
    }

    private static Vertex getOrCreateVertex(Long id, Set<Long> componentIds, Map<Long, Vertex> verticesById)
    {
        Vertex vertex = verticesById.get(id);
        if (vertex == null)
        {
            vertex = new Vertex(id, componentIds.contains(id));
            verticesById.put(id, vertex);
        }
        return vertex;
    }

}
