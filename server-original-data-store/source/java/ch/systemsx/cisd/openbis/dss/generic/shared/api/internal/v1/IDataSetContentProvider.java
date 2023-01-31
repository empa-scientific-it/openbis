/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;

/**
 * Interface for classes providing {@link IHierarchicalContent} of a data set.
 *
 * @author Franz-Josef Elmer
 */
public interface IDataSetContentProvider
{
    /**
     * Returns the content of the specified data set.
     * 
     * @throws IllegalArgumentException if data set doesn't exist.
     */
    public IHierarchicalContent getContent(String dataSetCode);
}
