/*
 * Copyright 2010 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * This class uses a variant of a trick described in the GWT discussion forum to support autofill. Browsers do not support autofill on input fields
 * that are generated on the client by javascript, so it is necessary for the page to statically contain the input fields we want to autofill. These
 * fields are unhidden and used on the login page.
 * 
 * @see <a href ="http://groups.google.com/group/Google-Web-Toolkit/browse%5Fthread/thread/2b2ce0b6aaa82461">GWT Discussion Forum</a>
 * @author Chandrasekhar Ramakrishnan
 */
// TODO 2010-03-10, CR: This implementation currently supports Firefox, but not Safari or Chrome. To
// support Safari, we cannot use javascript in the action, instead we need to have the login post
// data to a server.
public class LoginPanelAutofill extends VerticalPanel
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final FormPanel formPanel;

    private static final String PREFIX = "login_";

    private static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private static final String LOGIN_FORM_ID = ID_PREFIX + "form";

    // Used by the tests
    public static final String USERNAME_ID = ID_PREFIX + "username";

    public static final String PASSWORD_ID = ID_PREFIX + "password";

    public static final String SUBMIT_ID = ID_PREFIX + "submit";

    private static LoginPanelAutofill singleton = null;

    /**
     * Method to get the singleton instance of the login autofill panel
     */
    public static LoginPanelAutofill get(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        if (singleton == null)
            singleton = new LoginPanelAutofill(viewContext);
        return singleton;
    }

    private LoginPanelAutofill(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;

        setSpacing(10);
        setBorders(false);
        add(new Text(viewContext.getMessage(Dict.LOGIN_INVITATION)));

        Element formElement = Document.get().getElementById(LOGIN_FORM_ID);
        if (formElement == null)
        {
            // This is an error and should not happen
            formPanel = null;
            return;
        }
        formPanel = FormPanel.wrap(formElement, false);

        formPanel.addSubmitHandler(new SubmitHandler()
            {
                @Override
                public void onSubmit(SubmitEvent event)
                {
                    if (!isUserInputValid())
                        event.cancel();
                    else
                        doLogin();
                }

            });

        add(formPanel);
    }

    private final boolean isUserInputValid()
    {
        // If we are testing, always accept input
        if (GWTUtils.isTesting())
            return true;

        // Validate the input -- neither field can be blank
        String username = getUsernameElement().getValue();
        String password = getPasswordElement().getValue();
        if (username == null || username.trim().length() == 0)
            return false;
        if (password == null || password.trim().length() == 0)
            return false;

        return true;
    }

    private void giveFocusToFirstField()
    {
        getUsernameElement().focus();
    }

    @Override
    protected final void onLoad()
    {
        super.onLoad();
        // WORKAROUND Skip the rest if we are testing
        if (GWTUtils.isTesting())
            return;
        getButtonElement().setDisabled(false);
        giveFocusToFirstField();
    }

    private final void doLogin()
    {
        getButtonElement().setDisabled(true);

        InputElement usernameElement = getUsernameElement();
        InputElement passwordElement = getPasswordElement();

        final String user = usernameElement.getValue();
        final String password = passwordElement.getValue();

        viewContext.getService().tryToLogin(user, password, new LoginCallback(viewContext));
    }

    public InputElement getPasswordElement()
    {
        return InputElement.as(Document.get().getElementById(PASSWORD_ID));
    }

    public InputElement getUsernameElement()
    {
        return InputElement.as(Document.get().getElementById(USERNAME_ID));
    }

    public final InputElement getButtonElement()
    {
        return InputElement.as(Document.get().getElementById(SUBMIT_ID));
    }

    //
    // Helper classes
    //

    // public only for tests
    public final class LoginCallback extends BasicLoginCallback
    {
        private LoginCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext, Dict.LOGIN_FAILED);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void finishOnFailure(final Throwable caught)
        {
            getPasswordElement().setValue(getPasswordElement().getDefaultValue());
            getButtonElement().setDisabled(false);
        }

        @Override
        protected void cleanup()
        {
            // Clear the password
            getPasswordElement().setValue(getPasswordElement().getDefaultValue());
        }
    }
}
