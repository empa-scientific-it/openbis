/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Properties;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * Test cases for the {@link ThreadParameters}.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = ThreadParameters.class)
public final class ThreadParametersTest
{

    @Test
    public final void testTryGetGroupCode()
    {
        final Properties properties = new Properties();
        assertNull(ThreadParameters.tryGetGroupCode(properties));
        properties.setProperty(ThreadParameters.GROUP_CODE_KEY, "");
        assertNull(ThreadParameters.tryGetGroupCode(properties));
        final String groupCode = "G1";
        properties.setProperty(ThreadParameters.GROUP_CODE_KEY, groupCode);
        assertEquals(groupCode, ThreadParameters.tryGetGroupCode(properties));
    }
}
