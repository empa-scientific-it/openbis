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

package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;

/**
 * The {@link ICorePluginResourceLoader} abstracts the physical representation of a {@link CorePlugin} on the file system.
 * 
 * @author Kaloyan Enimanev
 */
public interface ICorePluginResourceLoader
{

    /**
     * Locate a path within a given plugin and attempt to read its contents as string. If the path does not exist <code>NULL</code> is returned.
     */
    String tryLoadToString(CorePlugin plugin, String path);

    /**
     * Locate a path within a given plugin.
     */
    String getPath(CorePlugin plugin, String path);

}
