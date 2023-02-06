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

@JsonObject("as.dto.common.search.AbstractFieldSearchCriteria")
public abstract class AbstractFieldSearchCriteria<T> extends AbstractSearchCriteria
{

    private static final long serialVersionUID = 1L;

    private final String fieldName;

    private final SearchFieldType fieldType;

    private T fieldValue;

    public AbstractFieldSearchCriteria(String fieldName, SearchFieldType fieldType)
    {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public SearchFieldType getFieldType()
    {
        return fieldType;
    }

    public T getFieldValue()
    {
        return fieldValue;
    }

    public void setFieldValue(T value)
    {
        this.fieldValue = value;
    }

    @Override
    public String toString()
    {
        String descriptor = "";
        switch (getFieldType())
        {
            case PROPERTY:
                descriptor = "with property '" + getFieldName() + "'";
                break;
            case ATTRIBUTE:
                descriptor = "with attribute '" + getFieldName() + "'";
                break;
            case ANY_PROPERTY:
                descriptor = "any property";
                break;
            case ANY_FIELD:
                descriptor = "any field";
                break;
        }
        return descriptor + " " + (getFieldValue() == null ? "" : getFieldValue().toString());
    }

}