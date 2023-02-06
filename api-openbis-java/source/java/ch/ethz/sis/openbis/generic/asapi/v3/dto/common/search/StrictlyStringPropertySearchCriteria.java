/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.common.search.StrictlyStringPropertySearchCriteria")
public class StrictlyStringPropertySearchCriteria extends StringFieldSearchCriteria
{

    private static final long serialVersionUID = 1L;

    StrictlyStringPropertySearchCriteria()
    {
        super(null, SearchFieldType.PROPERTY);
    }

    StrictlyStringPropertySearchCriteria(final String fieldName)
    {
        super(fieldName, SearchFieldType.PROPERTY);
    }

    public void thatMatches(final String string)
    {
        setFieldValue(new StringMatchesValue(string));
    }

}
