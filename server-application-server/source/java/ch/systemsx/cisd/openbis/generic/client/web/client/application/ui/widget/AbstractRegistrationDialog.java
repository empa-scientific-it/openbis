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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField.CodeFieldKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * Abstract {@link Window} with Save and Cancel buttons, useful when simple registration operation is needed.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractRegistrationDialog extends AbstractSaveDialog
{

    public static final String CODE_FIELD_ID = GenericConstants.ID_PREFIX + "dialog-code-field";

    protected abstract void register(AsyncCallback<Void> registrationCallback);

    @Override
    protected final void save(AsyncCallback<Void> saveCallback)
    {
        register(saveCallback);
    }

    public AbstractRegistrationDialog(final IViewContext<?> viewContext, String title,
            final IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, title, postRegistrationCallback);
    }

    /** Creates simple text field with given title and mandatory flag set false. */
    public static TextField<String> createTextField(String title)
    {
        return createTextField(title, false);
    }

    /** Creates simple text field with given title and given mandatory flag. */
    public static TextField<String> createTextField(String title, boolean mandatory)
    {
        return new VarcharField(title, mandatory);
    }

    /** Creates multiline text field for description with mandatory flag set false. */
    public static DescriptionField createDescriptionField(IMessageProvider messageProvider)
    {
        return createDescriptionField(messageProvider, false);
    }

    /** Creates multiline text field for description with mandatory flag set false. */
    public static DescriptionField createDescriptionField(IMessageProvider messageProvider,
            boolean mandatory)
    {
        return new DescriptionField(messageProvider, mandatory);
    }

    public static TextField<String> createCodeField(IMessageProvider messageProvider)
    {
        return createCodeField(messageProvider, CodeFieldKind.BASIC_CODE);
    }

    public static TextField<String> createCodeField(IMessageProvider messageProvider,
            CodeFieldKind fieldKind)
    {
        final TextField<String> codeField =
                new CodeField(messageProvider, messageProvider.getMessage(Dict.CODE), fieldKind);
        codeField.setWidth(100);
        codeField.setId(CODE_FIELD_ID);
        return codeField;
    }
}
