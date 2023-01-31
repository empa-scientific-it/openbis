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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * A naming strategy for symbolic links in hiearchical storages.
 * 
 * @author Kaloyan Enimanev
 */
public interface IHierarchicalStorageLinkNamingStrategy
{

    /**
     * For a given {@link AbstractExternalData} creates relevant path part e.g. <code>Instance_AAA/Group_BBB/Project_CCC...</code> There can be
     * multiple paths because dataset can be a component in multiple containers.
     */
    public Set<HierarchicalPath> createHierarchicalPaths(AbstractExternalData data);

    /**
     * Returns all data set paths located under <code>root</code>.
     */
    public Set<String> extractPaths(File root);
}
