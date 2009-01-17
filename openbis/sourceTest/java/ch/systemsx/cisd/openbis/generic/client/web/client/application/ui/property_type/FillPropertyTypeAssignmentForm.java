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

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for property type assignment.
 * 
 * @author Izabela Adamczyk
 */
public final class FillPropertyTypeAssignmentForm extends AbstractDefaultTestCommand
{
    private static final String USER_PROPERTY_PREFIX = "USER.";

    private final String entityTypeCode;

    private final String propertyTypeCode;

    private final boolean isMandatory;

    private String defaultValue;

    private String entityKindName;

    private String widgetId;

    public FillPropertyTypeAssignmentForm(final boolean isMandatory, final String propertyTypeCode,
            final String entityTypeCode, String defaultValue, String entityKindName)
    {
        this.isMandatory = isMandatory;
        this.propertyTypeCode = propertyTypeCode;
        this.entityTypeCode = entityTypeCode;
        this.defaultValue = defaultValue;
        this.entityKindName = entityKindName;
        widgetId = PropertyTypeAssignmentForm.ID_PREFIX + entityKindName;
        addCallbackClass(PropertyTypeSelectionWidget.ListPropertyTypesCallback.class);
        addEntityTypeCallback();
    }

    private void addEntityTypeCallback()
    {
        if (entityKindName.equals("EXPERIMENT"))
        {
            addCallbackClass(ExperimentTypeSelectionWidget.ListExperimentTypesCallback.class);
        } else if (entityKindName.equals("SAMPLE"))
        {
            addCallbackClass(SampleTypeSelectionWidget.ListSampleTypesCallback.class);
        } else
        {
            throw new IllegalArgumentException();
        }

    }

    public final void execute()
    {

        final PropertyTypeSelectionWidget propertyTypeSelector = choosePropertyType();
        setDefaultValue(propertyTypeSelector.tryGetSelectedPropertyTypeCode());
        chooseEntityType();
        chooseIfMandatory();
        clickSaveButton();
    }

    private PropertyTypeSelectionWidget choosePropertyType()
    {
        final PropertyTypeSelectionWidget propertyTypeSelector =
                (PropertyTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(PropertyTypeSelectionWidget.ID + widgetId
                                + PropertyTypeAssignmentForm.PROPERTY_TYPE_ID_SUFFIX);
        GWTUtils.setSelectedItem(propertyTypeSelector, ModelDataPropertyNames.CODE,
                propertyTypeCode);
        return propertyTypeSelector;
    }

    private void setDefaultValue(final String selectedPropertyTypeCode)
    {
        final Widget widget =
                GWTTestUtil.getWidgetWithID(widgetId
                        + PropertyTypeAssignmentForm.DEFAULT_VALUE_ID_PART
                        + isInternalNamespace(selectedPropertyTypeCode)
                        + getSimpleCode(selectedPropertyTypeCode));
        assertTrue(widget instanceof Field);
        if (defaultValue != null)
        {
            ((Field<?>) widget).setRawValue(defaultValue);
        }
    }

    private void chooseIfMandatory()
    {
        final CheckBox mandatoryCheckbox =
                (CheckBox) GWTTestUtil.getWidgetWithID(widgetId
                        + PropertyTypeAssignmentForm.MANDATORY_CHECKBOX_ID_SUFFIX);
        mandatoryCheckbox.setValue(isMandatory);
    }

    private void chooseEntityType()
    {
        if (entityKindName.equals("EXPERIMENT"))
        {
            final ComboBox<ExperimentTypeModel> experimentTypeSelector =
                    (ExperimentTypeSelectionWidget) GWTTestUtil
                            .getWidgetWithID(ExperimentTypeSelectionWidget.ID
                                    + PropertyTypeAssignmentForm.EXPERIMENT_TYPE_ID_SUFFIX);
            GWTUtils.setSelectedItem(experimentTypeSelector, ModelDataPropertyNames.CODE,
                    entityTypeCode);
            assertEquals(((ExperimentTypeSelectionWidget) experimentTypeSelector)
                    .tryGetSelectedExperimentType().getCode(), entityTypeCode);
        } else if (entityKindName.equals("SAMPLE"))
        {
            final ComboBox<SampleTypeModel> sampleTypeSelector =
                    (SampleTypeSelectionWidget) GWTTestUtil
                            .getWidgetWithID(SampleTypeSelectionWidget.ID
                                    + PropertyTypeAssignmentForm.SAMPLE_TYPE_ID_SUFFIX);
            GWTUtils.setSelectedItem(sampleTypeSelector, ModelDataPropertyNames.CODE,
                    entityTypeCode);
            assertEquals(((SampleTypeSelectionWidget) sampleTypeSelector)
                    .tryGetSelectedSampleType().getCode(), entityTypeCode);
        } else
        {
            throw new IllegalArgumentException();
        }
    }

    private void clickSaveButton()
    {
        GWTTestUtil.clickButtonWithID(widgetId + PropertyTypeAssignmentForm.SAVE_BUTTON_ID_SUFFIX);
    }

    private String getSimpleCode(String code)
    {
        if (isInternalNamespace(code))
        {
            return code;
        } else
        {
            return code.substring(USER_PROPERTY_PREFIX.length());
        }
    }

    private boolean isInternalNamespace(String code)
    {
        return code.startsWith(USER_PROPERTY_PREFIX) == false;
    }

}
