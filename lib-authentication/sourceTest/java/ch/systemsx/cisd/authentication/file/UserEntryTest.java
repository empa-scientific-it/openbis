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

package ch.systemsx.cisd.authentication.file;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.security.PasswordHasher;

/**
 * Test cases for {@link UserEntry}.
 * 
 * @author Bernd Rinn
 */
public class UserEntryTest
{
    private static final String PASSWORD = "passw0rd";

    private static final String PASSWORD_HASH = PasswordHasher.computeSaltedHash(PASSWORD);

    private static final String PASSWORD_LINE = "id:email@dot.org:First:Last:" + PASSWORD_HASH;

    @Test
    public void testRoundtrip()
    {
        final String line = PASSWORD_LINE;
        final UserEntry entry = new UserEntry(line);
        assertEquals(line, entry.asPasswordLine());
    }

    @Test
    public void testGetters()
    {
        final UserEntry entry = new UserEntry(PASSWORD_LINE);
        assertEquals("id", entry.getUserId());
        assertEquals("First", entry.getFirstName());
        assertEquals("Last", entry.getLastName());
        assertEquals("email@dot.org", entry.getEmail());
        assertTrue(entry.isPasswordCorrect(PASSWORD));
        assertFalse(entry.isPasswordCorrect(PASSWORD.replace('0', 'o')));
    }

    @Test
    public void testConstructor()
    {
        final String id = "ID";
        final String firstName = "first";
        final String lastName = "laST";
        final String email = "a@b.edu";
        final UserEntry entry = new UserEntry(id, email, firstName, lastName, PASSWORD);
        assertEquals(id, entry.getUserId());
        assertEquals(firstName, entry.getFirstName());
        assertEquals(lastName, entry.getLastName());
        assertEquals(email, entry.getEmail());
        assertTrue(entry.isPasswordCorrect(PASSWORD));
        assertFalse(entry.isPasswordCorrect(PASSWORD.replace('0', 'o')));
    }

    @Test
    public void testSetters()
    {
        final String id = "id";
        final String firstName = "first1";
        final String lastName = "laST2";
        final String email = "a@b.edu";
        final UserEntry entry = new UserEntry(PASSWORD_LINE);
        assertEquals(id, entry.getUserId());
        entry.setFirstName(firstName);
        assertEquals(firstName, entry.getFirstName());
        entry.setLastName(lastName);
        assertEquals(lastName, entry.getLastName());
        entry.setEmail(email);
        assertEquals(email, entry.getEmail());
        entry.setPassword(PASSWORD.replace('0', 'o'));
        assertFalse(entry.isPasswordCorrect(PASSWORD));
        assertTrue(entry.isPasswordCorrect(PASSWORD.replace('0', 'o')));
        final String line = String.format("%s:%s:%s:%s:", id, email, firstName, lastName);
        assertTrue(entry.asPasswordLine().startsWith(line));
    }

    @Test
    public void testAsPrincial()
    {
        final UserEntry entry = new UserEntry(PASSWORD_LINE);
        final Principal principal = entry.asPrincipal();
        assertEquals("id", principal.getUserId());
        assertEquals("First", principal.getFirstName());
        assertEquals("Last", principal.getLastName());
        assertEquals("email@dot.org", principal.getEmail());
    }

}
