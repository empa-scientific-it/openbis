/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author Franz-Josef Elmer
 */
public class TableModelRowWithObject<T extends Serializable> extends TableModelRow implements
        IIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static <T extends Serializable> List<T> getObjects(List<TableModelRowWithObject<T>> rows)
    {
        ArrayList<T> list = new ArrayList<T>();
        for (TableModelRowWithObject<T> row : rows)
        {
            list.add(row.getObjectOrNull());
        }
        return list;
    }

    private T objectOrNull;

    private Long id;

    public TableModelRowWithObject(T objectOrNull, List<ISerializableComparable> values)
    {
        super(values);
        this.objectOrNull = objectOrNull;
        if (objectOrNull instanceof IIdHolder)
        {
            id = ((IIdHolder) objectOrNull).getId();
        } else
        {
            for (ISerializableComparable value : values)
            {
                if (value instanceof SerializableComparableIDDecorator)
                {
                    id = ((SerializableComparableIDDecorator) value).getID();
                    break;
                }
            }
        }
    }

    // GWT only
    @SuppressWarnings("unused")
    private TableModelRowWithObject()
    {
    }

    public T getObjectOrNull()
    {
        return objectOrNull;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((objectOrNull == null) ? 0 : objectOrNull.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (false == (obj instanceof TableModelRowWithObject))
        {
            return false;
        }

        TableModelRowWithObject<?> other = (TableModelRowWithObject<?>) obj;
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        } else if (false == id.equals(other.id))
        {
            return false;
        }

        if (objectOrNull == null)
        {
            if (other.objectOrNull != null)
            {
                return false;
            }
        } else if (false == objectOrNull.equals(other.objectOrNull))
        {
            return false;
        }
        return true;
    }

}
