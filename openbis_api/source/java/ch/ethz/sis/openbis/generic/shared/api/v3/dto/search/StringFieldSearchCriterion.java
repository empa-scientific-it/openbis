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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.search;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.search.StringFieldSearchCriterion")
public class StringFieldSearchCriterion extends AbstractFieldSearchCriterion<AbstractStringValue>
{

    private static final long serialVersionUID = 1L;

    StringFieldSearchCriterion(String fieldName, SearchFieldType fieldType)
    {
        super(fieldName, fieldType);
        setFieldValue(new AnyStringValue());
    }

    public void thatEquals(String string)
    {
        setFieldValue(new StringEqualToValue(string));
    }

    public void thatStartsWith(String prefix)
    {
        setFieldValue(new StringStartsWithValue(prefix));
    }

    public void thatEndsWith(String suffix)
    {
        setFieldValue(new StringEndsWithValue(suffix));
    }

    public void thatContains(String string)
    {
        setFieldValue(new StringContainsValue(string));
    }

}