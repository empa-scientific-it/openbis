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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property;

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedComboBoxInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class ManagedPropertyFormHelper
{
    private final IViewContext<?> viewContext;

    private final FormPanel formPanel;

    private final Map<String, TextField<?>> inputFieldsByCode;

    public ManagedPropertyFormHelper(IViewContext<?> viewContext, FormPanel formPanel,
            Map<String, TextField<?>> inputFieldsByCode)
    {
        this.viewContext = viewContext;
        this.formPanel = formPanel;
        this.inputFieldsByCode = inputFieldsByCode;

    }

    public void fillForm(List<IManagedInputWidgetDescription> inputWidgetDescriptions)
    {
        for (IManagedInputWidgetDescription inputDescription : inputWidgetDescriptions)
        {
            trySetBoundedValue(inputDescription);
            TextField<?> field;
            switch (inputDescription.getManagedInputFieldType())
            {
                case TEXT:
                    field = createTextField(inputDescription);
                    break;
                case MULTILINE_TEXT:
                    field = createMultilineTextField(inputDescription);
                    break;
                case COMBO_BOX:
                    field = createComboBoxField(inputDescription);
                    break;
                default:
                    throw new UnsupportedOperationException(); // can't happen
            }
            final String label = inputDescription.getLabel();
            if (label == null)
            {
                throwFailToCreateContentException("Label is not set in input widget description");
            }
            field.setFieldLabel(label);

            if (inputDescription.getDescription() != null)
            {
                AbstractImagePrototype infoIcon =
                        AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
                FieldUtil.addInfoIcon(field, inputDescription.getDescription(),
                        infoIcon.createImage());
            }
            FieldUtil.setMandatoryFlag(field, inputDescription.isMandatory());

            inputFieldsByCode.put(inputDescription.getCode(), field);
            formPanel.add(field);
        }

    }

    protected void trySetBoundedValue(IManagedInputWidgetDescription inputDescription)
    {
    }

    private TextField<?> createTextField(IManagedInputWidgetDescription inputDescription)
    {
        final TextField<String> field = new TextField<String>();
        if (inputDescription.getValue() != null)
        {
            FieldUtil.setValueWithUnescaping(field, inputDescription.getValue());
            field.updateOriginalValue(field.getValue());
        }
        return field;
    }

    private TextField<?> createMultilineTextField(IManagedInputWidgetDescription inputDescription)
    {
        final TextField<String> field =
                new MultilineVarcharField(inputDescription.getLabel(), false);
        if (inputDescription.getValue() != null)
        {
            FieldUtil.setValueWithUnescaping(field, inputDescription.getValue());
            field.updateOriginalValue(field.getValue());
        }
        return field;
    }

    private TextField<?> createComboBoxField(IManagedInputWidgetDescription inputDescription)
    {
        final SimpleComboBox<String> comboBox = new SimpleComboBox<String>();
        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setEditable(false);
        comboBox.setForceSelection(true);
        if (inputDescription instanceof ManagedComboBoxInputWidgetDescription)
        {
            final ManagedComboBoxInputWidgetDescription comboBoxDescription =
                    (ManagedComboBoxInputWidgetDescription) inputDescription;
            comboBox.add(comboBoxDescription.getOptions());

            if (inputDescription.getValue() != null)
            {
                comboBox.setSimpleValue(inputDescription.getValue());
                comboBox.updateOriginalValue(comboBox.getValue());
            }
            return comboBox;
        } else
        {
            throwFailToCreateContentException("'" + inputDescription.getLabel()
                    + "' description should be a subclass of ManagedComboBoxInputWidgetDescription");
            return null;
        }
    }

    private void throwFailToCreateContentException(String detailedErrorMsg)
            throws UserFailureException
    {
        throw new UserFailureException("Failed to create content.", detailedErrorMsg);
    }

}
