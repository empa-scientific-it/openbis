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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.RealField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.PlateLayouterModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.IRangeType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.Range;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.RangeType;

/**
 * Dialog for choosing the range to be shown in numerical heat maps.
 * 
 * @author Franz-Josef Elmer
 */
public class HeatMapRangeChooser extends Dialog
{
    private final Map<CheckBoxField, RangeType> checkBoxToType =
            new HashMap<CheckBoxField, RangeType>();

    private final ScreeningViewContext viewContext;

    private Component checkedComponent;

    private FieldSet fieldSet;

    private NumberField fromField;

    private NumberField untilField;

    private FormPanel formPanel;

    HeatMapRangeChooser(ScreeningViewContext viewContext, PlateLayouterModel model,
            IDelegatedAction acceptAction)
    {
        this.viewContext = viewContext;
        setHeading(viewContext.getMessage(Dict.HEAT_MAP_RANGE_CHOOSER_TITLE));
        setWidth(500);
        setHeight(300);
        setButtons(""); // no default buttons
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
        setModal(true);

        createAndAddForm();
        addButtons(model, acceptAction);
        initComponents(model.getRangeType());
    }

    private void createAndAddForm()
    {
        formPanel = new FormPanel();
        formPanel.setLabelWidth(200);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);
        formPanel.add(new Label("Choose one of range types:"));
        formPanel.add(new Label());
        RangeType[] values = RangeType.values();
        for (RangeType rangeType : values)
        {
            CheckBoxField checkBoxField =
                    new CheckBoxField(
                            viewContext.getMessage(Dict.HEAT_MAP_RANGE_CHOOSER_TYPE_LABEL_PREFIX
                                    + rangeType), false);
            checkBoxField.setToolTip(viewContext
                    .getMessage(Dict.HEAT_MAP_RANGE_CHOOSER_TYPE_TOOLTIP_PREFIX + rangeType));
            addListenerTo(checkBoxField);
            formPanel.add(checkBoxField);
            checkBoxToType.put(checkBoxField, rangeType);
        }
        createAndAddFieldSetForFixedType();
        add(formPanel, new BorderLayoutData(LayoutRegion.CENTER));
    }

    private void createAndAddFieldSetForFixedType()
    {
        fieldSet = new FieldSet();
        fieldSet.setHeading(viewContext.getMessage(Dict.HEAT_MAP_RANGE_CHOOSER_FIXED_TYPE_LABEL));
        fieldSet.setCheckboxToggle(true);
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(190);
        fieldSet.setLayout(layout);
        fieldSet.collapse();
        addListenerTo(fieldSet);
        Validator validator = createValidatorForFixedTypeFields();
        fromField =
                createNumberField(Dict.HEAT_MAP_RANGE_CHOOSER_FIXED_TYPE_LOWEST_SCALE_LABEL,
                        validator);
        untilField =
                createNumberField(Dict.HEAT_MAP_RANGE_CHOOSER_FIXED_TYPE_HIGHEST_SCALE_LABEL,
                        validator);
        // The scale is ordered top to bottom, use the same order for the input fields
        fieldSet.add(untilField);
        fieldSet.add(fromField);
        formPanel.add(fieldSet);
    }

    private NumberField createNumberField(String labelKey, Validator validator)
    {
        RealField field = new RealField(viewContext.getMessage(labelKey), false)
            {
                @Override
                protected boolean validateValue(String fieldValue)
                {
                    if (validator != null)
                    {
                        String msg = validator.validate(this, fieldValue);
                        if (msg != null)
                        {
                            markInvalid(msg);
                            return false;
                        }
                    }
                    return super.validateValue(fieldValue);
                }
            };
        field.setValidator(validator);
        return field;
    }

    private Validator createValidatorForFixedTypeFields()
    {
        Validator validator = new Validator()
            {
                @Override
                public String validate(Field<?> field, String value)
                {
                    if (fieldSet.isExpanded())
                    {
                        if (value == null || value.isEmpty())
                        {
                            return "Unspecified value";
                        }
                        Number fromValue = fromField.getValue();
                        Number untilValue = untilField.getValue();
                        if (fromValue != null && untilValue != null)
                        {
                            if (fromValue.doubleValue() == untilValue.doubleValue())
                            {
                                return viewContext
                                        .getMessage(Dict.HEAT_MAP_RANGE_CHOOSER_FIXED_TYPE_SAME_VALUE_VALIDATION_MSG);
                            }
                        }
                    }
                    return null;
                }
            };
        return validator;
    }

    private void addButtons(final PlateLayouterModel model, final IDelegatedAction acceptAction)
    {
        addButton(new Button("OK", new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        IRangeType rangeType = getRangeType();
                        if (rangeType != null)
                        {
                            model.setRangeType(rangeType);
                            acceptAction.execute();
                        }
                        hide();
                    }
                }
            }));
        addButton(new Button(
                viewContext
                        .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.BUTTON_CANCEL),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public final void componentSelected(ButtonEvent ce)
                        {
                            hide();
                        }
                    }));
    }

    private void initComponents(IRangeType rangeType)
    {
        Set<Entry<CheckBoxField, RangeType>> entrySet = checkBoxToType.entrySet();
        for (Entry<CheckBoxField, RangeType> entry : entrySet)
        {
            if (entry.getValue().equals(rangeType))
            {
                CheckBoxField checkBox = entry.getKey();
                checkBox.setValue(true);
                checkedComponent = checkBox;
                return;
            }
        }
        if (rangeType instanceof Range)
        {
            Range range = (Range) rangeType;
            fieldSet.expand();
            fromField.setValue(range.getFrom());
            untilField.setValue(range.getUntil());
            checkedComponent = fieldSet;
        }
    }

    private void addListenerTo(final CheckBox checkBox)
    {
        checkBox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    buttonPressed(checkBox);
                }
            });
    }

    private void addListenerTo(final FieldSet fSet)
    {
        fSet.addListener(Events.Expand, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    buttonPressed(fSet);
                }
            });
    }

    private void buttonPressed(Component component)
    {
        if (component == checkedComponent)
            return;

        if (checkedComponent != null)
        {
            if (checkedComponent instanceof CheckBox)
            {
                CheckBox checkBox = (CheckBox) checkedComponent;
                checkBox.setValue(false);
            } else if (checkedComponent instanceof FieldSet)
            {
                FieldSet fSet = (FieldSet) checkedComponent;
                fSet.collapse();
            }
        }

        checkedComponent = component;
    }

    private IRangeType getRangeType()
    {
        if (checkedComponent == null)
        {
            return null;
        }
        if (checkedComponent == fieldSet)
        {
            return new Range(fromField.getValue().floatValue(), untilField.getValue().floatValue());
        }
        return checkBoxToType.get(checkedComponent);
    }
}
