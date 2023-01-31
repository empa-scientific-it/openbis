/*
 * Copyright 2017 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Decorator of {@link IHierarchicalContent} which knows data set behind.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetAwareHierarchicalContent implements IHierarchicalContent
{
    private final IDatasetLocation dataSet;
    private final IHierarchicalContent content;

    public DataSetAwareHierarchicalContent(IDatasetLocation dataSet, IHierarchicalContent content)
    {
        this.dataSet = dataSet;
        this.content = content;
    }
    
    public IDatasetLocation getDataSet()
    {
        return dataSet;
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        return decorate(dataSet, content.getRootNode());
    }
    
    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        return decorate(dataSet, content.getNode(relativePath));
    }
    
    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        return decorate(dataSet, content.tryGetNode(relativePath));
    }
    
    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        return decorate(dataSet, content.listMatchingNodes(relativePathPattern));
    }
    
    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath, String fileNamePattern)
    {
        return decorate(dataSet, content.listMatchingNodes(startingPath, fileNamePattern));
    }
    
    @Override
    public void close()
    {
        content.close();
    }
    
    static List<IHierarchicalContentNode> decorate(IDatasetLocation dataSet, List<IHierarchicalContentNode> nodes)
    {
        List<IHierarchicalContentNode> result = new ArrayList<>();
        for (IHierarchicalContentNode node : nodes)
        {
            result.add(decorate(dataSet, node));
        }
        return result;
    }
    
    private static IHierarchicalContentNode decorate(IDatasetLocation dataSet, IHierarchicalContentNode node)
    {
        return node == null ? null : new DataSetAwareHierarchicalContentNode(dataSet, node);
    }
}
