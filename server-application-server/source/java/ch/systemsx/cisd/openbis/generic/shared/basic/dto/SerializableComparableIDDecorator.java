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

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Decorator of {@link ISerializableComparable} with a {@link TechId}.
 *
 * @author Franz-Josef Elmer
 */
public class SerializableComparableIDDecorator implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ISerializableComparable serializableComparable;

    private Long id;

    public SerializableComparableIDDecorator(ISerializableComparable serializableComparable, Long idOrNull)
    {
        assert serializableComparable != null : "Unspecified ISerializableComparable";
        this.serializableComparable = serializableComparable;
        this.id = idOrNull;
    }

    public Long getID()
    {
        return id;
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        if (o instanceof SerializableComparableIDDecorator)
        {
            SerializableComparableIDDecorator decorator = (SerializableComparableIDDecorator) o;
            return serializableComparable.compareTo(decorator.serializableComparable);
        }
        return serializableComparable.compareTo(o);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof SerializableComparableIDDecorator == false)
        {
            return false;
        }
        SerializableComparableIDDecorator decorator = (SerializableComparableIDDecorator) obj;
        return serializableComparable.equals(decorator.serializableComparable);
    }

    @Override
    public int hashCode()
    {
        return serializableComparable.hashCode();
    }

    @Override
    public String toString()
    {
        return serializableComparable.toString();
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private SerializableComparableIDDecorator()
    {
    }
}
