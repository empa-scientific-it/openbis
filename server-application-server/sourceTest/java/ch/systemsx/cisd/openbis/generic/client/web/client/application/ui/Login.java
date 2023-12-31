/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * Command for login.
 * 
 * @author Franz-Josef Elmer
 */
public class Login extends AbstractDefaultTestCommand
{
    private final String user;

    private final String password;

    public Login(final String user, final String password)
    {
        this.user = user;
        this.password = password;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        TextField<String> textField =
                GWTTestUtil.<String> getTextFieldWithID(LoginWidget.USER_FIELD_ID);
        textField.setValue(user);
        assertEquals(user, textField.getValue());
        textField = GWTTestUtil.<String> getTextFieldWithID(LoginWidget.PASSWORD_FIELD_ID);
        textField.setValue(password);
        assertEquals(StringUtils.isBlank(password) ? null : password, textField.getValue());
        GWTTestUtil.clickButtonWithID(LoginWidget.BUTTON_ID);
    }
}
