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
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public interface IHierarchicalContentFactory
{
    /**
     * Returns virtual hierarchy with merged nodes based on given list of components.
     */
    public IHierarchicalContent asVirtualHierarchicalContent(List<IHierarchicalContent> components);

    /**
     * Returns hierarchy based on given root file.
     * 
     * @param onCloseAction action that will be performed when returned hierarchy is closed
     */
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction);

    /**
     * Returns content node for given file. Different implementations may be returned depending on e.g. file extension.
     */
    public IHierarchicalContentNode asHierarchicalContentNode(IHierarchicalContent rootContent,
            File file);

}
