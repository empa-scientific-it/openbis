/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author juanf
 */
@JsonObject("as.dto.common.fetchoptions.SortParameter")
public enum SortParameter
{

    FULL_MATCH_CODE_BOOST, PARTIAL_MATCH_CODE_BOOST, FULL_MATCH_TYPE_BOOST, FULL_MATCH_PROPERTY_BOOST,
    PARTIAL_MATCH_PROPERTY_BOOST, MATCH_VALUE, PREFIX_MATCH_VALUE

}
