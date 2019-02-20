/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.filetransfer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Franz-Josef Elmer
 *
 */
abstract class AbstractId
{

    private String id;

    public AbstractId(String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }

        AbstractId other = (AbstractId) obj;
        return new EqualsBuilder().append(id, other.id).isEquals();
    }

    @Override
    public String toString()
    {
        return new ToString(this).append("id", id).toString();
    }

}
