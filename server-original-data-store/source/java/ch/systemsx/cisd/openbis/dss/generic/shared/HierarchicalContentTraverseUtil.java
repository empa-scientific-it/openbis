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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * A utility class to abstract traversing of {@link IHierarchicalContentNode}-s.
 * 
 * @author Kaloyan Enimanev
 */
public class HierarchicalContentTraverseUtil
{

    /**
     * Traverses all {@link IHierarchicalContentNode}-s within a data set specified by a data set code.
     * 
     * @param provider a provider class used to construct {@link IHierarchicalContent}.
     * @param dataSetCode the code of the data set to be traversed
     * @param visitor a visitor that will be invoked for every traversed {@link IHierarchicalContentNode}.
     */
    public static void traverse(IHierarchicalContentProvider provider, String dataSetCode,
            IHierarchicalContentNodeVisitor visitor)
    {
        IHierarchicalContent content = null;
        try
        {
            content = provider.asContent(dataSetCode);
            visit(content.getRootNode(), visitor);
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }

    }

    private static void visit(IHierarchicalContentNode node, IHierarchicalContentNodeVisitor visitor)
    {
        visitor.visit(node);
        if (node.isDirectory())
        {
            List<IHierarchicalContentNode> children = node.getChildNodes();
            HierarchicalContentUtils.sortNodes(children);
            for (IHierarchicalContentNode child : children)
            {
                visit(child, visitor);
            }
        }
    }

}
