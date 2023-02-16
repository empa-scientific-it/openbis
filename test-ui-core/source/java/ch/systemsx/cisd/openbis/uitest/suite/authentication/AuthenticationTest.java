/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.suite.authentication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.InvalidPasswordDialog;
import ch.systemsx.cisd.openbis.uitest.page.RoleAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.UserSettingsDialog;

public class AuthenticationTest extends SeleniumTest
{

    @BeforeTest
    public void fixture()
    {
        useGui();
        enableLegacyUi();
    }

    protected void enableLegacyUi()
    {
        login(ADMIN_USER, ADMIN_PASSWORD);
        UserSettingsDialog settings = browser().goTo(userSettings());
        settings.setLegacyUi();
        settings.save();
        logout();
    }

//    @Test
    public void loginFailsWithInvalidUserName() throws Exception
    {
        login("invalid", SeleniumTest.ADMIN_PASSWORD);

        assertThat(browser(), displays(InvalidPasswordDialog.class));

        assumePage(InvalidPasswordDialog.class).dismiss();
    }

//    @Test
    public void loginSucceedsWithValidCredentials() throws Exception
    {
        login(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD);

        assertThat(loggedInAs(), is(SeleniumTest.ADMIN_USER));

        logout();
    }

    @Test
    public void adminCanOpenRoleAssignmentBrowser() throws Exception
    {
        login(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD);
        assumePage(TopBar.class).admin();
        assumePage(AdminMenu.class).roles();

        assertThat(browser(), displays(RoleAssignmentBrowser.class));

        logout();
    }
}