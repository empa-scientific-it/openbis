/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;

public class EntityGraph<N extends INode>
{
    private final Map<String, INode> nodes;

    private final Map<INode, List<EdgeNodePair>> adjacencyMap;

    // the following flags make sure then we don't end up in a cycle when a sample is the component of a its child sample
    // or when a data set is the component of its child data set
    private final Set<NodeIdentifier> visitedParents = new HashSet<>();

    private final Set<NodeIdentifier> visitedContainers = new HashSet<>();

    public EntityGraph()
    {
        this.nodes = new HashMap<String, INode>();
        this.adjacencyMap = new HashMap<INode, List<EdgeNodePair>>();
    }

    public boolean isVisitedAsParent(NodeIdentifier identifier)
    {
        return visitedParents.contains(identifier);
    }

    public boolean isVisitedAsContainer(NodeIdentifier identifier)
    {
        return visitedContainers.contains(identifier);
    }

    public void markAsVisitedAsParent(NodeIdentifier identifier)
    {
        visitedParents.add(identifier);
    }

    public void markAsVisitedAsContainer(NodeIdentifier identifier)
    {
        visitedContainers.add(identifier);
    }

    public void addEdge(INode startNode, INode endNode, Edge edge)
    {
        List<EdgeNodePair> adjacencyList = adjacencyMap.get(startNode);
        if (adjacencyList == null)
        {
            adjacencyList = new ArrayList<EdgeNodePair>();
            adjacencyMap.put(startNode, adjacencyList);
        } else
        {
            // do not add the edge-node pair if it has already been added
            // happens when for a sub sample of a sample with multiple parent
            for (EdgeNodePair pair : adjacencyList)
            {
                if (pair.getNode().equals(endNode))
                {
                    return;
                }
            }
        }
        EdgeNodePair enPair = new EdgeNodePair(edge, endNode);
        startNode.addConnection(enPair);
        adjacencyList.add(enPair);
        addNode(endNode);
    }

    public void addNode(INode node)
    {
        String identifier = node.getIdentifier().toString();
        if (nodes.containsKey(identifier) == false)
        {
            adjacencyMap.put(node, new ArrayList<EdgeNodePair>());
            nodes.put(identifier, node);
        }
    }

    public List<INode> getNodes()
    {
        return new ArrayList<INode>(nodes.values());
    }

    public String getEdgesForDOTRepresentation()
    {
        return getEdgesForNode(false);
    }

    public String getEdgesForTest()
    {
        return getEdgesForNode(true);
    }

    private String getEdgesForNode(boolean forTest)
    {
        StringBuffer sb = new StringBuffer();
        for (INode node : getNodes())
        {
            List<EdgeNodePair> list = adjacencyMap.get(node);
            if (list.isEmpty() && node.getEntityKind().equals(SyncEntityKind.DATA_SET) == false)
            {
                sb.append(getRightHandNodeRep(node, forTest));
                // if(node.getTableMapper().equals("PROJECT")) {
                // sb.append(" [shape=box]");
                // }
                sb.append(";");
                sb.append(System.getProperty("line.separator"));
                continue;
            }
            for (EdgeNodePair edgeNodePair : list)
            {
                INode neighbourNode = edgeNodePair.getNode();
                sb.append("\"" + node.getCode() + "(" + getDifferentiatorStr(node, forTest) + ")\" -> "
                        + getRightHandNodeRep(neighbourNode, forTest));
                if (edgeNodePair.getEdge().getType().equals(Edge.COMPONENT))
                {
                    sb.append(" [style=dotted, color=red]");
                } else if (edgeNodePair.getEdge().getType().equals(Edge.CHILD))
                {
                    sb.append(" [style=dashed, color= blue]");
                }
                sb.append(";");
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }

    private String getRightHandNodeRep(INode node, boolean forTest)
    {
        return "\"" + node.getCode() + "(" + getDifferentiatorStr(node, forTest) + ")\"";
    }

    private String getDifferentiatorStr(INode node, boolean forTest)
    {
        if (forTest == false)
        {
            String differentiatorStr = "";
            // in order
            // to differentiate between experiments/projects in the same space but under different
            // projects/spaces
            if (node.getEntityKind().equals(SyncEntityKind.EXPERIMENT) || node.getEntityKind().equals(SyncEntityKind.PROJECT))
            {
                differentiatorStr = node.getIdentifier().getEntityIdentifier();
                ;
            } else
            {
                differentiatorStr = node.getEntityKind().getAbbreviation();
            }
            return differentiatorStr;
        }
        Map<String, Serializable> propertiesOrNull = node.getPropertiesOrNull();
        String s = "props =";
        if (propertiesOrNull != null)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("props = ");
            for (String property : propertiesOrNull.keySet())
            {
                sb.append(property);
                sb.append(":");
                sb.append(propertiesOrNull.get(property));
                sb.append(",");
            }
            s = new String(sb);
        }
        return s;
    }
}
