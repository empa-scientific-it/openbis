/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class PasswordEditorCommandTest {

	/**
	 * Changing one field (e.g. password) should not change other fields.
	 */
    @Test
    public void testPartialChange()
    {
    	// given
    	UserEntry existingUser = new UserEntry("markwatney", "watney@mars.com", "Mark", "Watney", "oldPassword");
    	Parameters params = new Parameters(new String[] {"change", "markwatney", "-p", "newPassword"}, false);

    	// when
    	PasswordEditorCommand.applyParamsToExistingUser(params, existingUser);

    	// then
    	assertEquals("Mark", existingUser.getFirstName());
    	assertEquals("Watney", existingUser.getLastName());
    	assertEquals("watney@mars.com", existingUser.getEmail());
    }

	/**
	 * Changing a field to an empty string should work.
	 */
    @Test
    public void testChangeToEmpty()
    {
    	// given
    	UserEntry existingUser = new UserEntry("markwatney", "watney@mars.com", "Mark", "Watney", "oldPassword");
    	Parameters params = new Parameters(new String[] {"change", "markwatney", "-f", ""}, false);

    	// when
    	PasswordEditorCommand.applyParamsToExistingUser(params, existingUser);

    	// then
    	assertEquals("", existingUser.getFirstName());
    	assertEquals("Watney", existingUser.getLastName());
    	assertEquals("watney@mars.com", existingUser.getEmail());
    }

}
