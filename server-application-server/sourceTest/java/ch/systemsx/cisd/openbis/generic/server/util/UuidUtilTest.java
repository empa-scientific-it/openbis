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

package ch.systemsx.cisd.openbis.generic.server.util;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.UUID;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Test cases for the {@link UuidUtil}.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = UuidUtil.class)
public final class UuidUtilTest
{

    @Test
    public final void testIsValidUUIDRainyDay()
    {
        boolean fail = true;
        try
        {
            UuidUtil.isValidUUID(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertFalse(UuidUtil.isValidUUID("1"));
        assertFalse(UuidUtil.isValidUUID("----"));
        assertFalse(UuidUtil.isValidUUID("1-2-3--4"));
    }

    @Test(invocationCount = 10)
    public final void testIsValidHappyCase()
    {
        assertTrue(UuidUtil.isValidUUID(UUID.randomUUID().toString()));
        assertTrue(UuidUtil.isValidUUID(UuidUtil.generateUUID()));
    }
}
