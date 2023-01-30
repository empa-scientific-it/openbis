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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.ScriptTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * @author Izabela Adamczyk
 */
public class ScriptRegistrationForm extends AbstractScriptEditRegisterForm
{

    public static ScriptRegistrationForm create(
            IViewContext<ICommonClientServiceAsync> viewContext, EntityKind entityKindOrNull)
    {
        ScriptTypeSelectionWidget scriptTypeChooser =
                ScriptTypeSelectionWidget.createAllScriptTypes(viewContext);
        return new ScriptRegistrationForm(viewContext, scriptTypeChooser, entityKindOrNull);
    }

    protected ScriptRegistrationForm(IViewContext<ICommonClientServiceAsync> viewContext,
            ScriptTypeSelectionWidget scriptTypeChooser, EntityKind entityKindOrNull)
    {
        super(viewContext, scriptTypeChooser, entityKindOrNull);
        setResetButtonVisible(true);
        onPluginOrScriptTypeChanged(PluginType.JYTHON, scriptTypeChooser.getSimpleValue());
    }

    @Override
    protected void saveScript()
    {
        Script newScript = getScript();
        viewContext.getService().registerScript(newScript,
                new ScriptRegistrationCallback(viewContext, newScript));
    }

    @Override
    public Script getScript()
    {
        PluginType pluginType = PluginType.JYTHON;

        Script newScript = new Script();
        newScript.setDescription(descriptionField.getValue());
        newScript.setScript(scriptField.getValue());
        newScript.setName(nameField.getValue());

        newScript.setScriptType(scriptTypeChooserOrNull.getSimpleValue());
        newScript.setPluginType(pluginType);
        newScript.setEntityKind(entityKindField.tryGetEntityKind() == null ? null
                : new EntityKind[]
                { entityKindField.tryGetEntityKind() });
        newScript.setAvailable(true);
        return newScript;
    }

    private final class ScriptRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        private final Script script;

        ScriptRegistrationCallback(final IViewContext<?> viewContext, final Script script)
        {
            super(viewContext);
            this.script = script;
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(Void result)
        {
            return Arrays.asList(new HtmlMessageElement("Script <b>" + script.getName().toUpperCase() + "</b> successfully registered."));
        }
    }

    public static String createId()
    {
        return AbstractScriptEditRegisterForm.createId(null);
    }

    @Override
    protected void setValues()
    {
    }

    @Override
    protected void loadForm()
    {
        initGUI();
    }
}
