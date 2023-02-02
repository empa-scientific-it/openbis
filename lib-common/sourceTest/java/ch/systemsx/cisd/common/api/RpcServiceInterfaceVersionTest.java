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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class RpcServiceInterfaceVersionTest extends AssertJUnit
{
    @Test
    public void testToString()
    {
        final RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO("DSS Generic", "/rpc/dss-generic/v1", 1, 7);
        assertEquals("RpcServiceInterfaceVersionDTO[DSS Generic,/rpc/dss-generic/v1,v.1.7]",
                ifaceVersion.toString());
    }

    @Test
    public void testEquality()
    {
        final int majorVersion = (int) (Math.random() * 10);
        final int minorVersion = (int) (Math.random() * 100);
        final RpcServiceInterfaceVersionDTO ifaceVersion1 =
                new RpcServiceInterfaceVersionDTO("DSS Generic", "/rpc/dss-generic/v1",
                        majorVersion, minorVersion);

        final RpcServiceInterfaceVersionDTO ifaceVersion2 =
                new RpcServiceInterfaceVersionDTO("DSS Generic", "/rpc/dss-generic/v1",
                        majorVersion, minorVersion);

        assertTrue(ifaceVersion1 != ifaceVersion2);
        assertEquals(ifaceVersion1, ifaceVersion2);
        assertEquals(ifaceVersion1.hashCode(), ifaceVersion2.hashCode());
    }
}
