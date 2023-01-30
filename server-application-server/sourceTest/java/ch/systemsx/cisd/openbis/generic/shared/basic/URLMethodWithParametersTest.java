/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class URLMethodWithParametersTest extends AssertJUnit
{
    @Test
    public void test()
    {
        URLMethodWithParameters method =
                new URLMethodWithParameters("http://my.host:1234/The string & my@foo-bar");
        method.addParameter("p%rcentage", "1/10^2");
        method.addParameter("f?le", "att=chment.txt");
        assertEquals("http://my.host:1234/The+string+%26+my%40foo-bar?"
                + "p%25rcentage=1%2F10%5E2&f%3Fle=att%3Dchment.txt", method.toString());
    }
}
