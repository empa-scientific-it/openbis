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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

/**
 * A class representing a permanent identifier in openBIS.
 * 
 * @since 1.1
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
public class PermanentIdentifier implements Serializable, IPermanentIdentifier
{
    private static final long serialVersionUID = 1L;

    private String permId;

    public PermanentIdentifier(String permId)
    {
        this.permId = permId;
    }

    @Override
    public String getPermId()
    {
        return permId;
    }

    @Override
    public String toString()
    {
        return getPermId();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permId == null) ? 0 : permId.hashCode());
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
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final PermanentIdentifier other = (PermanentIdentifier) obj;
        if (permId == null)
        {
            if (other.permId != null)
            {
                return false;
            }
        } else if (permId.equals(other.permId) == false)
        {
            return false;
        }
        return true;
    }

    //
    // JSON-RPC
    //

    private PermanentIdentifier()
    {
    }

    private void setPermId(String permId)
    {
        this.permId = permId;
    }

}
