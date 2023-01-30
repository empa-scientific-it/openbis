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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * Abstract super class of dialogs editing an entity type.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractEditEntityTypeDialog<T extends EntityType> extends
        AbstractRegistrationDialog
{
    public static final String DIALOG_ID = GenericConstants.ID_PREFIX + "edit-type-dialog";

    private final T type;

    private final DescriptionField descriptionField;

    private ScriptChooserField scriptChooser;

    private final EntityKind entityKind;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    protected AbstractEditEntityTypeDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String title, IDelegatedAction postRegistrationCallback, EntityKind entityKind,
            T entityType)
    {
        super(viewContext, title, postRegistrationCallback);
        this.viewContext = viewContext;
        this.entityKind = entityKind;
        setId(DIALOG_ID);
        this.type = entityType;
        descriptionField = createDescriptionField(viewContext);
        descriptionField.setId(DIALOG_ID + "-description");
        FieldUtil.setValueWithUnescaping(descriptionField, entityType.getDescription());
        addField(descriptionField);

        scriptChooser =
                createScriptChooserField(viewContext, null, true,
                        ScriptType.ENTITY_VALIDATION, entityKind);
        scriptChooser.setId(DIALOG_ID + "-script-chooser");
        addField(scriptChooser);
        scriptChooser.setValue(entityType.getValidationScript() != null ? entityType
                .getValidationScript().getName() : "");

    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        type.setDescription(descriptionField.getValue());

        Script script = new Script();
        script.setName(scriptChooser.getValue());
        type.setValidationScript(script);

        setSpecificAttributes(type);
        viewContext.getCommonService().updateEntityType(entityKind, type, registrationCallback);
    }

    abstract protected void setSpecificAttributes(T entityType);

    private ScriptChooserField createScriptChooserField(
            final IViewContext<ICommonClientServiceAsync> context, String initialValue,
            boolean visible, ScriptType scriptTypeOrNull, EntityKind entityKindOrNull)
    {
        ScriptChooserField field =
                ScriptChooserField.create(context.getMessage(Dict.VALIDATION_SCRIPT),
                        false,
                        initialValue,
                        context, scriptTypeOrNull, entityKindOrNull);
        FieldUtil.setVisibility(visible, field);
        return field;
    }

}
