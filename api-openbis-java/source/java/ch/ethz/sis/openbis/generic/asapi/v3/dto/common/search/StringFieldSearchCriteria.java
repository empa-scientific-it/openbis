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

@JsonObject("as.dto.common.search.StringFieldSearchCriteria")
public abstract class StringFieldSearchCriteria extends AbstractFieldSearchCriteria<AbstractStringValue>
{

    private static final long serialVersionUID = 1L;

    private boolean useWildcards = true;

    public StringFieldSearchCriteria(String fieldName, SearchFieldType fieldType)
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

    public void thatIsLessThan(String string)
    {
        setFieldValue(new StringLessThanValue(string));
    }

    public void thatIsLessThanOrEqualTo(String string)
    {
        setFieldValue(new StringLessThanOrEqualToValue(string));
    }

    public void thatIsGreaterThan(String string)
    {
        setFieldValue(new StringGreaterThanValue(string));
    }

    public void thatIsGreaterThanOrEqualTo(String string)
    {
        setFieldValue(new StringGreaterThanOrEqualToValue(string));
    }

    public StringFieldSearchCriteria withWildcards()
    {
        useWildcards = true;
        return this;
    }

    public StringFieldSearchCriteria withoutWildcards()
    {
        useWildcards = false;
        return this;
    }

    public boolean isUseWildcards()
    {
        return useWildcards;
    }

    @Override
    public String toString()
    {
        String result = super.toString();
        if (useWildcards == false)
        {
            result += " (not using wildcards)";
        }
        return result;
    }

}
