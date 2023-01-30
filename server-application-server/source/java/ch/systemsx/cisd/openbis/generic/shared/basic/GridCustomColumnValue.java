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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores grid custom column id and value.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumnValue implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 1L;

    private String columnId;

    private PrimitiveValue value;

    public String getColumnId()
    {
        return columnId;
    }

    public void setColumnId(String columnId)
    {
        this.columnId = columnId;
    }

    public PrimitiveValue getValue()
    {
        return value;
    }

    public void setValue(PrimitiveValue value)
    {
        this.value = value;
    }
}