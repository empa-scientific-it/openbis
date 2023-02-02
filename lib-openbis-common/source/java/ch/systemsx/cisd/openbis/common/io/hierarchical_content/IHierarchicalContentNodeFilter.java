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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * A filter for abstract {@link IHierarchicalContentNode}.
 * 
 * @author Piotr Buczek
 */
public interface IHierarchicalContentNodeFilter
{
    /**
     * a filter accepting all nodes.
     */
    public static final IHierarchicalContentNodeFilter MATCH_ALL =
            new IHierarchicalContentNodeFilter()
                {
                    @Override
                    public boolean accept(IHierarchicalContentNode node)
                    {
                        return true;
                    }
                };

    /**
     * Tests whether or not the specified abstract node should be included in a node list.
     * 
     * @param node The abstract node to be tested
     * @return <code>true</code> if and only if <code>node</code> should be included
     */
    boolean accept(IHierarchicalContentNode node);

}
