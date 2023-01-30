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

import java.util.List;

/**
 * Provides required information about entities.
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IEntityInformationProvider
{
    /**
     * @return identifier of entity specified by given link, <code>null</code> if such an entity doesn't exist
     */
    String getIdentifier(IEntityLinkElement entityLink);

    /**
     * @return permId of sample specified by given space and code, <code>null</code> if such a sample doesn't exist
     */
    String getSamplePermId(String spaceCode, String sampleCode);

    /**
     * @return permId of project sample specified by given space, project and code, <code>null</code> if such a sample doesn't exist
     */
    String getProjectSamplePermId(String spaceCode, String projectCode, String sampleCode);
    
    /**
     * @return permId of sample specified by given identifier, <code>null</code> if such a sample doesn't exist
     */
    String getSamplePermId(String sampleIdentifier);

    /**
     * @return list of permIds of parents of a sample with given space and code
     */
    List<String> getSampleParentPermIds(String spaceCode, String sampleCode);

    /**
     * @return list of permIds of parents of a project sample with given space, project and code
     */
    List<String> getProjectSampleParentPermIds(String spaceCode, String projectCode, String sampleCode);
    
    /**
     * @return list of permIds of parents of a sample with given permId
     */
    List<String> getSampleParentPermIds(String permId);

    /**
     * @return value of a property with given code of a sample with given permIds, empty string if the property doesn't exist
     */
    String getSamplePropertyValue(String permId, String propertyCode);
}
