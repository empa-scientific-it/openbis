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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IProjectImmutable
{
    /**
     * Returns the permanent id of this project.
     */
    String getPermId();

    /**
     * Return the identifier for this project.
     */
    String getProjectIdentifier();

    /**
     * Returns the code of the space this project belongs to.
     */
    String getSpaceCode();

    /**
     * Returns the code of this project.
     */
    String getCode();

    /**
     * Return the description for this project.
     */
    String getDescription();

    /**
     * Return true if the project exists in the database.
     */
    boolean isExistingProject();
}
