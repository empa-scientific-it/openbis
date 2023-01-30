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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IDataSetType extends IDataSetTypeImmutable, IEntityTypeMutable
{

    /**
     * Set the description for this data set type.
     */
    public void setDescription(String description);

    /**
     * Set the main data set pattern for this data set type.
     */
    public void setMainDataSetPattern(String mainDataSetPattern);

    /**
     * Set the main data set path for this data set type.
     */
    public void setMainDataSetPath(String mainDataSetPath);

    /**
     * Sets to <code>true</code> if deletion of data sets of this type should be disallowed.
     */
    public void setDeletionDisallowed(boolean deletionDisallowed);

}
