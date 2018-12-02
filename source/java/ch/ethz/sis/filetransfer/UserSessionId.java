/*
 * Copyright 2018 ETH Zuerich, CISD
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author pkupczyk
 */
public class UserSessionId implements IUserSessionId
{

    private static final long serialVersionUID = 1L;

    private String id;

    public UserSessionId(String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    @Override
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

        UserSessionId other = (UserSessionId) obj;
        return new EqualsBuilder().append(id, other.id).isEquals();
    }

    @Override
    public String toString()
    {
        return new ToString(this).append("id", id).toString();
    }

}
