/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

public class EdgeNodePair
{
    private final Edge edge;

    private final INode node;

    public EdgeNodePair(Edge edge, INode node)
    {
        this.edge = edge;
        this.node = node;
    }

    public Edge getEdge()
    {
        return edge;
    }

    public INode getNode()
    {
        return node;
    }
}
