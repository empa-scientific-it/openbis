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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

/**
 * @author Jakub Straszewski
 */
public interface IMetaprojectImmutable
{

    /**
     * @return the name of this metaproject
     */
    String getName();

    /**
     * @return the description of this metaproject
     */
    String getDescription();

    /**
     * @return the username of the owner of this metaproject
     */
    String getOwnerId();

    /**
     * @return true if this is the already existing metaproject
     */
    boolean isExistingMetaproject();

}
