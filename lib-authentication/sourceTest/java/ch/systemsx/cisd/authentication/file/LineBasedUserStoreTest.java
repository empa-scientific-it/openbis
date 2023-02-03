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

import static org.testng.AssertJUnit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Test cases for the {@link LineBasedUserStore}.
 * 
 * @author Bernd Rinn
 */
public class LineBasedUserStoreTest
{
    private Mockery context;

    private ILineStore lineStore;

    private IUserStore<UserEntry> userStore;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        lineStore = context.mock(ILineStore.class);
        userStore = FileAuthenticationService.createUserStore(lineStore);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testGetId()
    {
        final String id = "Some Identifier";
        context.checking(new Expectations()
            {
                {
                    one(lineStore).getId();
                    will(returnValue(id));
                }
            });
        assertEquals(id, userStore.getId());
        context.assertIsSatisfied();
    }

    @Test
    public void testCheck()
    {
        context.checking(new Expectations()
            {
                {
                    one(lineStore).check();
                }
            });
        userStore.check();
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckFailed()
    {
        final String errorMessage = "My Message";
        context.checking(new Expectations()
            {
                {
                    one(lineStore).check();
                    will(throwException(new ConfigurationFailureException(errorMessage)));
                }
            });
        try
        {
            userStore.check();
            fail("Failed to signal error condition");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(errorMessage, ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testListUsers()
    {
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        final List<UserEntry> list = userStore.listUsers();
        assertEquals(lines.size(), list.size());
        assertEquals(u1, list.get(0));
        assertEquals(u2, list.get(1));
        assertEquals(u3, list.get(2));
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetUserFailedEmptyStore()
    {
        final List<String> lines = Collections.emptyList();
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        assertNull(userStore.tryGetUserById("uid"));
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetUserFailedNoStoreFile()
    {
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(false));
                }
            });
        assertNull(userStore.tryGetUserById("uid"));
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetUserFailed()
    {
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        assertNull(userStore.tryGetUserById("non-existent"));
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetUserSuccess()
    {
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        assertEquals(u1, userStore.tryGetUserById("uid1"));
        context.assertIsSatisfied();
    }

    @Test
    public void testIsPasswordCorrectSuccess()
    {
        final String uid2 = "uid2";
        final String pwd2 = "pwd2";
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry(uid2, "email2", "first2", "last2", pwd2);
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        assertTrue(userStore.isPasswordCorrect(uid2, pwd2));
        context.assertIsSatisfied();
    }

    @Test
    public void testIsPasswordCorrectFailure()
    {
        final String uid2 = "uid2";
        final String pwd2 = "pwd2";
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry(uid2, "email2", "first2", "last2", pwd2);
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        assertFalse(userStore.isPasswordCorrect(uid2, pwd2.toUpperCase()));
        context.assertIsSatisfied();
    }

    @Test
    public void testAddFirstUser()
    {
        final UserEntry user = new UserEntry("uid", "email", "first", "last", "pwd");
        final String userLine = user.asPasswordLine();
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(new ArrayList<String>()));
                    one(lineStore).writeLines(Collections.singletonList(userLine));
                }
            });
        userStore.addOrUpdateUser(user);
        context.assertIsSatisfied();
    }

    @Test
    public void testAddSecondUser()
    {
        final UserEntry oldUser = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry newUser = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final String oldUserLine = oldUser.asPasswordLine();
        final String newUserLine = newUser.asPasswordLine();
        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(Arrays.asList(oldUserLine)));
                    one(lineStore).writeLines(Arrays.asList(oldUserLine, newUserLine));
                }
            });
        userStore.addOrUpdateUser(newUser);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateUser()
    {
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());

        final UserEntry u3Updated = new UserEntry("uid3", "email3U", "first3U", "last3U", "pwd3U");
        final List<String> linesUpdated =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3Updated.asPasswordLine());

        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                    one(lineStore).writeLines(linesUpdated);
                }
            });
        userStore.addOrUpdateUser(u3Updated);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateUserStoreChanged()
    {
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> linesOld =
                Arrays.asList(u1.asPasswordLine(), u3.asPasswordLine());
        final List<String> linesNew =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());

        final UserEntry u3Updated = new UserEntry("uid3", "email3U", "first3U", "last3U", "pwd3U");
        final List<String> linesUpdated =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3Updated.asPasswordLine());

        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(linesOld));
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(linesNew));
                    one(lineStore).writeLines(linesUpdated);
                }
            });
        assertEquals(Arrays.asList(u1, u3), userStore.listUsers());
        userStore.addOrUpdateUser(u3Updated);
        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveUser()
    {
        final String uid1 = "uid1";
        final UserEntry u1 = new UserEntry(uid1, "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                new ArrayList<String>(Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3
                        .asPasswordLine()));

        final List<String> linesUpdated = Arrays.asList(u2.asPasswordLine(), u3.asPasswordLine());

        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                    one(lineStore).writeLines(linesUpdated);
                }
            });
        userStore.removeUser(uid1);
        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveUserStoreChanged()
    {
        final String uid1 = "uid1";
        final UserEntry u1 = new UserEntry(uid1, "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> linesOld =
                new ArrayList<String>(Arrays.asList(u1.asPasswordLine(), u3.asPasswordLine()));
        final List<String> linesNew =
                new ArrayList<String>(Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3
                        .asPasswordLine()));

        final List<String> linesUpdated = Arrays.asList(u2.asPasswordLine(), u3.asPasswordLine());

        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(linesOld));
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(linesNew));
                    one(lineStore).writeLines(linesUpdated);
                }
            });
        assertEquals(Arrays.asList(u1, u3), userStore.listUsers());
        userStore.removeUser(uid1);
        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveNonExistingUser()
    {
        final UserEntry u1 = new UserEntry("uid1", "email1", "first1", "last1", "pwd1");
        final UserEntry u2 = new UserEntry("uid2", "email2", "first2", "last2", "pwd2");
        final UserEntry u3 = new UserEntry("uid3", "email3", "first3", "last3", "pwd3");
        final List<String> lines =
                Arrays.asList(u1.asPasswordLine(), u2.asPasswordLine(), u3.asPasswordLine());

        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                }
            });
        userStore.removeUser("non-existing");
        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveLastUser()
    {
        final String uid1 = "uid1";
        final UserEntry u1 = new UserEntry(uid1, "email1", "first1", "last1", "pwd1");
        final List<String> lines = new ArrayList<String>(Arrays.asList(u1.asPasswordLine()));

        context.checking(new Expectations()
            {
                {
                    one(lineStore).hasChanged();
                    will(returnValue(true));
                    one(lineStore).readLines();
                    will(returnValue(lines));
                    one(lineStore).writeLines(Collections.<String> emptyList());
                }
            });
        userStore.removeUser(uid1);
        context.assertIsSatisfied();
    }
}
