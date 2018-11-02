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

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author pkupczyk
 */
public class DownloadSessionId implements Serializable
{

    private static final long serialVersionUID = 1L;

    private UUID uuid;

    public DownloadSessionId()
    {
        this.uuid = UUID.randomUUID();
    }

    public UUID getUuid()
    {
        return uuid;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(uuid).toHashCode();
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

        DownloadSessionId other = (DownloadSessionId) obj;
        return new EqualsBuilder().append(uuid, other.uuid).isEquals();
    }

    @Override
    public String toString()
    {
        return new ToString(this).append("uuid", uuid).toString();
    }

}
