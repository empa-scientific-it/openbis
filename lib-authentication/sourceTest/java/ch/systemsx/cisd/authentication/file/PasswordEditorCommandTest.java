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
