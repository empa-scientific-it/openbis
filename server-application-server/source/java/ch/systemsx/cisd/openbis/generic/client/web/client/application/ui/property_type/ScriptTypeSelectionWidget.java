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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * A {@link ComboBox} extension using simple strings for selecting a script type.
 * 
 * @author Piotr Buczek
 */
public final class ScriptTypeSelectionWidget extends SimpleComboBox<ScriptType>
{
    /** creates a combo box with all script types */
    public static ScriptTypeSelectionWidget createAllScriptTypes(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new ScriptTypeSelectionWidget(viewContext.getMessage(Dict.SCRIPT_TYPE),
                viewContext.getMessage(Dict.SCRIPT_TYPE), viewContext.getMessage(
                        Dict.COMBO_BOX_CHOOSE, "script type"), Arrays.asList(ScriptType.values()));
    }

    private ScriptTypeSelectionWidget(final String fieldLabel, final String toolTip,
            final String chooseText, final List<ScriptType> scriptTypes)
    {
        setFieldLabel(fieldLabel);
        setTriggerAction(TriggerAction.ALL);
        GWTUtils.setToolTip(this, toolTip);
        GWTUtils.setupAutoWidth(this);
        setEmptyText(chooseText);
        setEditable(false);
        setForceSelection(true);
        add(scriptTypes);
        GWTUtils.autoselect(this);
        setId("script-type-selection");
    }
}
