/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.lang.reflect.Method;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class IEncapsulatedOpenBISServiceTest extends AssertJUnit
{

    @Test
    public void testAllMethodsDeclareManagedAuthentication()
    {
        for (Method m : IEncapsulatedOpenBISService.class.getMethods())
        {
            String error =
                    String.format(
                            "The methods of %s should be annotated with @ManagedAuthentication to"
                                    + " enable transparent handling of (re)authentication against openBIS,"
                                    + " but method [%s] was not. If [%s] does not access the remote openBIS"
                                    + " server, then you must adjust this unit test.",
                            IEncapsulatedOpenBISService.class.getName(), m.getName(), m.getName());
            assertTrue(error, m.isAnnotationPresent(ManagedAuthentication.class));

        }
    }
}
