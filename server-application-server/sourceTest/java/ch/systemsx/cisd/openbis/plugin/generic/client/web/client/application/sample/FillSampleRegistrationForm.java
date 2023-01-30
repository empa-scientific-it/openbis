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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating sample.
 * 
 * @author Christian Ribeaud
 */
public final class FillSampleRegistrationForm extends AbstractDefaultTestCommand
{
    private static String FORM_ID =
            GenericSampleRegistrationForm.createId((TechId) null, EntityKind.SAMPLE);

    private final String code;

    private final String groupNameOrNull;

    private final List<PropertyField> properties;

    private String parentsOrNull;

    private String container;

    private String experimentIdentifier;

    public FillSampleRegistrationForm(final String groupNameOrNull, final String code)
    {
        this.groupNameOrNull = groupNameOrNull;
        this.code = code;
        this.properties = new ArrayList<PropertyField>();
    }

    public final FillSampleRegistrationForm parents(final String parents)
    {
        this.parentsOrNull = parents;
        return this;
    }

    public final FillSampleRegistrationForm container(final String containerFieldValue)
    {
        this.container = containerFieldValue;
        return this;
    }

    public final FillSampleRegistrationForm addProperty(final PropertyField property)
    {
        assert property != null : "Unspecified property";
        properties.add(property);
        return this;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {

        GWTTestUtil.setTextField(FORM_ID + AbstractGenericEntityRegistrationForm.ID_SUFFIX_CODE,
                code);

        final SpaceSelectionWidget groupSelector =
                (SpaceSelectionWidget) GWTTestUtil.getWidgetWithID(DropDownList.ID
                        + SpaceSelectionWidget.SUFFIX + FORM_ID);
        GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupNameOrNull);

        if (StringUtils.isBlank(parentsOrNull) == false)
        {
            final SampleChooserField parentField =
                    (SampleChooserField) GWTTestUtil.getWidgetWithID(FORM_ID
                            + ParentSamplesArea.ID_SUFFIX_PARENT_SAMPLES);
            parentField.setValue(parentsOrNull);
        }
        if (StringUtils.isBlank(container) == false)
        {
            final SampleChooserField containerField =
                    (SampleChooserField) GWTTestUtil.getWidgetWithID(FORM_ID
                            + AbstractGenericSampleRegisterEditForm.ID_SUFFIX_CONTAINER);
            containerField.setValue(container);
        }
        if (StringUtils.isBlank(experimentIdentifier) == false)
        {
            final ExperimentChooserField expField =
                    (ExperimentChooserField) GWTTestUtil.getWidgetWithID(FORM_ID
                            + AbstractGenericSampleRegisterEditForm.ID_SUFFIX_EXPERIMENT);
            expField.setValue(experimentIdentifier);
        }
        for (final PropertyField property : properties)
        {
            GWTTestUtil.setPropertyFieldValue(property);
        }
        GWTTestUtil.clickButtonWithID(FORM_ID + AbstractRegistrationForm.SAVE_BUTTON);
    }

    public final FillSampleRegistrationForm experiment(final String experiment)
    {
        this.experimentIdentifier = experiment;
        return this;
    }
}
