/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;

/**
 * Abstract super class of all dialogs adding a new entity type.
 * 
 * @author Tomasz Pylak
 */
public abstract class AddTypeDialog<T extends AbstractType> extends AbstractRegistrationDialog
{
    public static final String DIALOG_ID = GenericConstants.ID_PREFIX + "add-type-dialog";

    public static final String DESCRIPTION_FIELD_ID = DIALOG_ID + "-description-field";

    /** Registers a new entity type and calls the specified callback at the end */
    abstract protected void register(T entityType, AsyncCallback<Void> registrationCallback);

    private final TextField<String> codeField;

    private final DescriptionField descriptionField;

    protected final T newEntityType;

    public AddTypeDialog(final IViewContext<ICommonClientServiceAsync> viewContext, String title,
            final IDelegatedAction postRegistrationCallback, T newEntityType)
    {
        super(viewContext, title, postRegistrationCallback);
        setId(DIALOG_ID);

        this.newEntityType = newEntityType;

        codeField = createCodeField(viewContext);
        addField(codeField);

        descriptionField = createDescriptionField(viewContext);
        descriptionField.setId(DESCRIPTION_FIELD_ID);
        addField(descriptionField);
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        newEntityType.setCode(codeField.getValue());
        newEntityType.setDescription(descriptionField.getValue());
        register(newEntityType, registrationCallback);
    }
}
