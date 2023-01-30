/*

 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchCriterion implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SERVER_TIMEZONE = "server";

    private DetailedSearchField field;

    private CompareType type;

    private Collection<String> values;

    private String timezone;

    private boolean negated;

    public DetailedSearchCriterion()
    {

    }

    public DetailedSearchCriterion(DetailedSearchField field, String value)
    {
        this.field = field;
        this.values = Arrays.asList(value);
        this.type = CompareType.EQUALS;
    }

    public DetailedSearchCriterion(DetailedSearchField field, Collection<String> values)
    {
        this.field = field;
        this.values = values;
        this.type = CompareType.EQUALS;
    }

    public DetailedSearchCriterion(DetailedSearchField field, CompareType type, String date)
    {
        this(field, type, date, SERVER_TIMEZONE);
    }

    public DetailedSearchCriterion(DetailedSearchField field, CompareType type, Number value)
    {
        this.field = field;
        this.values = Arrays.asList(value.toString());
        this.type = type;
    }

    public DetailedSearchCriterion(DetailedSearchField field, CompareType type, String value, String timezoneOrNull)
    {
        this.field = field;
        this.values = Arrays.asList(value);
        this.type = type;
        this.timezone = timezoneOrNull;
    }

    public DetailedSearchField getField()
    {
        return field;
    }

    public void setField(DetailedSearchField field)
    {
        this.field = field;
    }

    public String getValue()
    {
        return values != null && values.size() > 0 ? values.iterator().next() : null;
    }

    public void setValue(String value)
    {
        this.values = Arrays.asList(value);
    }

    public Collection<String> getValues()
    {
        return values;
    }

    public void setValues(Collection<String> values)
    {
        this.values = values;
    }

    public CompareType getType()
    {
        return this.type;
    }

    public String getTimeZone()
    {
        return this.timezone;
    }

    public boolean isNegated()
    {
        return negated;
    }

    public DetailedSearchCriterion negate()
    {
        this.negated = true;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (negated)
        {
            sb.append("not ");
        }
        sb.append(getField());
        sb.append(": ");
        if (values == null || values.size() == 1)
        {
            sb.append(getValue());
        } else
        {
            sb.append(getValues().toString());
        }
        return sb.toString();
    }
}
