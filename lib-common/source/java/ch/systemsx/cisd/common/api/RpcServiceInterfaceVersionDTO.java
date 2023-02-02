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

/**
 * Describes an RPC interface supported by the server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RpcServiceInterfaceVersionDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String interfaceName;

    private final String urlSuffix;

    private final int majorVersion;

    private final int minorVersion;

    public RpcServiceInterfaceVersionDTO(String name, String urlSuffix, int majorVersion,
            int minorVersion)
    {
        this.interfaceName = name;
        this.urlSuffix = urlSuffix;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    /**
     * The name of this interface used for identification.
     */
    public String getInterfaceName()
    {
        return interfaceName;
    }

    /**
     * The suffix added to the server's URL to produce the URL for this interface. Used by a service factory to create a proxy to the service.
     */
    public String getUrlSuffix()
    {
        return urlSuffix;
    }

    /**
     * The major version of the interface. E.g., an interface with version 2.11 has major version 2.
     */
    public int getMajorVersion()
    {
        return majorVersion;
    }

    /**
     * The major version of the interface. E.g., an interface with version 2.11 has minor version 11.
     */
    public int getMinorVersion()
    {
        return minorVersion;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (false == obj instanceof RpcServiceInterfaceVersionDTO)
            return false;

        RpcServiceInterfaceVersionDTO other = (RpcServiceInterfaceVersionDTO) obj;
        return getInterfaceName().equals(other.getInterfaceName())
                && getUrlSuffix().equals(other.getUrlSuffix())
                && getMajorVersion() == other.getMajorVersion()
                && getMinorVersion() == other.getMinorVersion();
    }

    @Override
    public int hashCode()
    {
        int hash = getInterfaceName().hashCode();
        hash = hash * 31 + getUrlSuffix().hashCode();
        hash = hash * 31 + getMajorVersion();
        hash = hash * 31 + getMinorVersion();
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("RpcServiceInterfaceVersionDTO[");
        sb.append(getInterfaceName());
        sb.append(",");
        sb.append(getUrlSuffix());
        sb.append(",v.");
        sb.append(getMajorVersion());
        sb.append(".");
        sb.append(getMinorVersion());
        sb.append("]");
        return sb.toString();
    }
}
