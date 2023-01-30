/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.page.LoginPage;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class LoginGui implements Command<Void>
{

    @Inject
    private Pages pages;

    private String user;

    private String password;

    public LoginGui(String user, String password)
    {
        this.user = user;
        this.password = password;
    }

    @Override
    public Void execute()
    {
        pages.load(LoginPage.class).loginAs(user, password);
        return null;
    }
}
