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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

/**
 * Provides required information about entities.
 * <p>
 * <b>All methods of this enum are part of the Managed Properties API.</b>
 * 
 * @author Piotr Buczek
 */
public interface IEntityInformationProvider
{
    /**
     * @return identifier of entity specified by given link, <code>null</code> if such an entity
     *         doesn't exist
     */
    String getIdentifier(IEntityLinkElement entityLink);

    /**
     * @return permId of entity specified by given space and sample, <code>null</code> if such an
     *         entity doesn't exist
     */
    String getSamplePermId(String spaceCode, String sampleCode);

    /**
     * @return permId of entity specified by given identifier, <code>null</code> if such an entity
     *         doesn't exist
     */
    String getSamplePermId(String sampleIdentifier);

}
