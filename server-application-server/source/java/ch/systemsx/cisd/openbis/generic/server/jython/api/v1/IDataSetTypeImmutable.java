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
 * Read-only interface to an data set type.
 * 
 * @author Kaloyan Enimanev
 */
public interface IDataSetTypeImmutable extends IEntityType
{

    /**
     * Get the description for this data set type.
     */
    @Override
    public String getDescription();

    /**
     * Get the main data set pattern for this data set type.
     */
    public String getMainDataSetPattern();

    /**
     * Get the main data set path for this data set type.
     */
    public String getMainDataSetPath();

    /**
     * Returns <code>true</code> if deletion of data sets of this type are disallowed.
     */
    public boolean isDeletionDisallowed();
}
