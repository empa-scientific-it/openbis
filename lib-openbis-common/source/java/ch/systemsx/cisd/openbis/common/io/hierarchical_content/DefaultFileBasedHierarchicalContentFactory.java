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

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * The default implementation of {@link IHierarchicalContentFactory} using file system as source of information.
 * 
 * @author Piotr Buczek
 */
public class DefaultFileBasedHierarchicalContentFactory implements IHierarchicalContentFactory
{

    @Override
    public IHierarchicalContent asVirtualHierarchicalContent(
            List<IHierarchicalContent> components)
    {
        return new VirtualHierarchicalContent(components);
    }

    @Override
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        return new DefaultFileBasedHierarchicalContent(this, file, onCloseAction);
    }

    @Override
    public IHierarchicalContentNode asHierarchicalContentNode(IHierarchicalContent rootContent,
            File file)
    {
        if (FileUtilities.isHDF5ContainerFile(file))
        {
            return new HDF5ContainerBasedHierarchicalContentNode(rootContent, file);
        }
        return new DefaultFileBasedHierarchicalContentNode(this, rootContent, file);
    }

}
