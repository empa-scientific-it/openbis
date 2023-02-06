/*
 * Copyright 2016 ETH Zuerich, CISD
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

@JsonObject("as.dto.common.search.TextAttributeSearchCriteria")
public class TextAttributeSearchCriteria extends AbstractSearchCriteria
{

    private static final long serialVersionUID = 1L;

    private AbstractStringValue fieldValue;

    public void thatMatches(final String string)
    {
        fieldValue = new StringMatchesValue(string);
    }

    public AbstractStringValue getFieldValue()
    {
        return fieldValue;
    }

    @Override
    public String toString()
    {
        return "with any text attribute '" +
                (getFieldValue() == null ? "" : getFieldValue().toString()) + "'";
    }

}
