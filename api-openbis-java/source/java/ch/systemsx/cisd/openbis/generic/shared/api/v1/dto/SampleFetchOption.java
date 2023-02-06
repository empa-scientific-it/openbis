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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FetchOption;

/**
 * Fetch options for {@link IGeneralInformationService#searchForSamples(String, SearchCriteria, java.util.EnumSet)}. The {@link Sample} objects return
 * by the search method also contain a fetch option ( {@link Sample#getRetrievedFetchOptions()}) which tells which attributes are filled and which
 * not.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("SampleFetchOption")
public enum SampleFetchOption implements FetchOption
{
    /**
     * Samples will have only basic attributes (id, code, type, space code, experiment identifier, registrator, registration date, modification date)
     * but no properties.
     */
    BASIC,
    /**
     * Samples contain basic attributes and all properties.
     */
    PROPERTIES,
    /**
     * Samples contain also their parent samples.
     */
    PARENTS,
    /**
     * Samples contain also their children samples.
     */
    CHILDREN,
    /**
     * Ask for all ancestors.
     */
    ANCESTORS,
    /**
     * Ask for all descendants.
     */
    DESCENDANTS,
    /**
     * Ask for contained samples. This is not supported in search operations
     */
    CONTAINED,
    /**
     * Ask for metaprojects this sample belongs to.
     */
    METAPROJECTS
}
