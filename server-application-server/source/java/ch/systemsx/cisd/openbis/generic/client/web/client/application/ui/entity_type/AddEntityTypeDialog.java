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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * Abstract super class of all dialogs adding a new entity type.
 * 
 * @author Tomasz Pylak
 */
public abstract class AddEntityTypeDialog<T extends EntityType> extends AddTypeDialog<T>
{
    private ScriptChooserField scriptChooser;

    public AddEntityTypeDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            String title,
            final IDelegatedAction postRegistrationCallback, T newEntityType, EntityKind entityKind)
    {
        super(viewContext, title, postRegistrationCallback, newEntityType);
        scriptChooser =
                createScriptChooserField(viewContext, null, true,
                        ScriptType.ENTITY_VALIDATION, entityKind);
        addField(scriptChooser);
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        Script script = new Script();
        script.setName(scriptChooser.getValue());
        newEntityType.setValidationScript(script);
        super.register(registrationCallback);
    }

    private ScriptChooserField createScriptChooserField(
            final IViewContext<ICommonClientServiceAsync> viewContext, String initialValue,
            boolean visible, ScriptType scriptTypeOrNull, EntityKind entityKindOrNull)
    {
        ScriptChooserField field =
                ScriptChooserField.create(viewContext.getMessage(Dict.VALIDATION_SCRIPT),
                        false,
                        initialValue,
                        viewContext, scriptTypeOrNull, entityKindOrNull);
        FieldUtil.setVisibility(visible, field);
        field.setId("openbis_add-" + entityKindOrNull + "-type-dialog-script-chooser");
        return field;
    }
}
