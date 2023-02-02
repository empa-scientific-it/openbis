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

package ch.systemsx.cisd.common.security;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.security.PasswordHasher;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Test cases of the {@link PasswordHasher}.
 * 
 * @author Bernd Rinn
 */
public class PasswordHasherTest
{

    private final static String PASSWORD = "The Passw0rd";

    @Test
    public void testSaltedHashesDifferent()
    {
        final String hash1 = PasswordHasher.computeSaltedHash(PASSWORD);
        final String hash2 = PasswordHasher.computeSaltedHash(PASSWORD);
        assertFalse(hash1.equals(hash2));
    }

    @Test
    public void testIsPasswordCorrectRightPassword()
    {
        final String hash = PasswordHasher.computeSaltedHash(PASSWORD);
        assertTrue(PasswordHasher.isPasswordCorrect(PASSWORD, hash));
    }

    @Test
    public void testIsPasswordCorrectWrongPassword()
    {
        final String hash = PasswordHasher.computeSaltedHash(PASSWORD);
        assertFalse(PasswordHasher.isPasswordCorrect(PASSWORD.replace('0', 'o'), hash));
    }
}
