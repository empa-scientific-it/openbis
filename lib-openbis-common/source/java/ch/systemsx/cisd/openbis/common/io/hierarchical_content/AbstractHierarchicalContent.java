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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Abstract super class of {@link IHierarchicalContent} classes which implements {@link #listMatchingNodes(String)} and
 * {@link #listMatchingNodes(String, String)}.
 *
 * @author Piotr Buczek
 * @author Franz-Josef Elmer
 */
public abstract class AbstractHierarchicalContent implements IHierarchicalContent
{
    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(final String relativePathPattern)
    {
        final IHierarchicalContentNode startingNode = getRootNode();
        final Pattern compiledPattern = Pattern.compile(relativePathPattern);
        final IHierarchicalContentNodeFilter relativePathFilter =
                new IHierarchicalContentNodeFilter()
                    {
                        @Override
                        public boolean accept(IHierarchicalContentNode node)
                        {
                            return compiledPattern.matcher(node.getRelativePath()).matches();
                        }
                    };

        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        findMatchingNodes(startingNode, relativePathFilter, result);
        return result;
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(final String startingPath, final String fileNamePattern)
    {
        final IHierarchicalContentNode startingNode = getNode(startingPath);
        final Pattern compiledPattern = Pattern.compile(fileNamePattern);
        final IHierarchicalContentNodeFilter fileNameFilter = new IHierarchicalContentNodeFilter()
            {
                @Override
                public boolean accept(IHierarchicalContentNode node)
                {
                    return compiledPattern.matcher(node.getName()).matches();
                }
            };

        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        findMatchingNodes(startingNode, fileNameFilter, result);
        return result;
    }

    /**
     * Recursively browses hierarchical content looking for nodes accepted by given <code>filter</code> and adding them to <code>result</code> list.
     */
    private void findMatchingNodes(IHierarchicalContentNode dirNode,
            IHierarchicalContentNodeFilter filter, List<IHierarchicalContentNode> result)
    {
        assert dirNode.isDirectory() : "expected a directory node, got: " + dirNode;
        for (IHierarchicalContentNode childNode : dirNode.getChildNodes())
        {
            if (filter.accept(childNode))
            {
                result.add(childNode);
            }
            if (childNode.isDirectory())
            {
                findMatchingNodes(childNode, filter, result);
            }
        }
    }

}