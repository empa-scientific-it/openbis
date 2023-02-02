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

package ch.systemsx.cisd.common.api;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Describes an RPC interface supported by the server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RpcServiceInterfaceDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String interfaceName;

    private final ArrayList<RpcServiceInterfaceVersionDTO> versions =
            new ArrayList<RpcServiceInterfaceVersionDTO>();

    public RpcServiceInterfaceDTO(String name)
    {
        interfaceName = name;
    }

    /**
     * The name of this interface used for identification.
     */
    public String getInterfaceName()
    {
        return interfaceName;
    }

    public ArrayList<RpcServiceInterfaceVersionDTO> getVersions()
    {
        return versions;
    }

    public void addVersion(RpcServiceInterfaceVersionDTO ifaceVersion)
    {
        versions.add(ifaceVersion);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (false == obj instanceof RpcServiceInterfaceDTO)
            return false;

        RpcServiceInterfaceDTO other = (RpcServiceInterfaceDTO) obj;
        return getInterfaceName().equals(other.getInterfaceName())
                && getVersions().equals(other.getVersions());
    }

    @Override
    public int hashCode()
    {
        int hash = getInterfaceName().hashCode();
        hash = hash * 31 + getVersions().hashCode();
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("RpcServiceInterfaceDTO[");
        sb.append(getInterfaceName());
        sb.append(" ");
        sb.append(getVersions().toString());
        sb.append("]");
        return sb.toString();
    }
}
