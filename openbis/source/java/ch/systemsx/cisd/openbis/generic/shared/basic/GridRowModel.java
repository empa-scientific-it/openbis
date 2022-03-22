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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores the original object which will be a basis to calculate a grid row together with calculated all custom columns values.
 * 
 * @author Tomasz Pylak
 */
public class GridRowModel<T> implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 1L;

    private T originalObject;

    /* <column id, value> */
    private Map<String, PrimitiveValue> calculatedColumnValues;

    public static <T> GridRowModel<T> createWithoutCustomColumns(T originalObject)
    {
        return new GridRowModel<T>(originalObject, new HashMap<String, PrimitiveValue>());
    }

    public GridRowModel(T originalObject, Map<String, PrimitiveValue> calculatedColumnMap)
    {
        this.originalObject = originalObject;
        this.calculatedColumnValues = calculatedColumnMap;
    }

    public T getOriginalObject()
    {
        return originalObject;
    }

    public Map<String, PrimitiveValue> getCalculatedColumnValues()
    {
        return calculatedColumnValues;
    }

    public PrimitiveValue findColumnValue(String columnId)
    {
        PrimitiveValue valueOrNull = calculatedColumnValues.get(columnId);
        if (valueOrNull != null)
        {
            return valueOrNull;
        } else
        {
            throw new IllegalStateException("Column not found: " + columnId);
        }
    }

    // GWT only
    @SuppressWarnings("unused")
    private GridRowModel()
    {
    }

    @Override
    public String toString()
    {
        return "GridRowModel [originalObject=" + originalObject + ", calculatedColumnValues="
                + calculatedColumnValues + "]";
    }
}
