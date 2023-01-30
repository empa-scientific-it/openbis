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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.TextArea;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating experiment.
 * 
 * @author Izabela Adamczyk
 */
public final class FillExperimentRegistrationForm extends AbstractDefaultTestCommand
{
    private static String FORM_ID =
            GenericExperimentRegistrationForm.createId((TechId) null, EntityKind.EXPERIMENT);

    private static String FORM_SIMPLE_ID = FORM_ID.substring(GenericConstants.ID_PREFIX.length());

    private final String code;

    private final String projectNameOrNull;

    private final List<PropertyField> properties;

    private final String samples;

    public FillExperimentRegistrationForm(final String project, final String code,
            final String samples)
    {
        this.projectNameOrNull = project;
        this.code = code;
        this.samples = samples;
        this.properties = new ArrayList<PropertyField>();
    }

    public final FillExperimentRegistrationForm addProperty(final PropertyField property)
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

        final ProjectSelectionWidget projectSelector =
                (ProjectSelectionWidget) GWTTestUtil.getWidgetWithID(DropDownList.ID
                        + ProjectSelectionWidget.SUFFIX + FORM_SIMPLE_ID);
        GWTUtils.setSelectedItem(projectSelector, ModelDataPropertyNames.CODE, projectNameOrNull);

        final TextArea samplesField =
                (TextArea) GWTTestUtil.getWidgetWithID(ExperimentSamplesArea.createId(FORM_ID));
        samplesField.setRawValue(samples);

        for (final PropertyField property : properties)
        {
            GWTTestUtil.setPropertyFieldValue(property);
        }
        GWTTestUtil.clickButtonWithID(FORM_ID + AbstractRegistrationForm.SAVE_BUTTON);
    }
}
