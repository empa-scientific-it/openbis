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

import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ControlledVocabullaryField.VocabularyList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for editing experiment.
 * 
 * @author Izabela Adamczyk
 */
public final class FillExperimentEditForm extends AbstractDefaultTestCommand
{

    private final String projectNameOrNull;

    private final List<PropertyField> properties;

    private static String DUMMY_ID =
            GenericExperimentRegistrationForm.createId(null, EntityKind.EXPERIMENT);

    public FillExperimentEditForm(final String project)
    {
        this.projectNameOrNull = project;
        this.properties = new ArrayList<PropertyField>();
        addCallbackClass(ProjectSelectionWidget.ListProjectsCallback.class);
    }

    public final FillExperimentEditForm addProperty(final PropertyField property)
    {
        assert property != null : "Unspecified property";
        properties.add(property);
        return this;
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {

        final ProjectSelectionWidget projectSelector =
                (ProjectSelectionWidget) GWTTestUtil.getWidgetWithID(ProjectSelectionWidget.ID
                        + ProjectSelectionWidget.SUFFIX + DUMMY_ID);
        if (projectNameOrNull != null)
        {
            GWTUtils.setSelectedItem(projectSelector, ModelDataPropertyNames.CODE,
                    projectNameOrNull);
        }

        for (final PropertyField property : properties)
        {
            final Widget widget = GWTTestUtil.getWidgetWithID(property.getPropertyFieldId());
            if (widget instanceof Field)
            {
                ((Field<?>) widget).setRawValue(property.getPropertyFieldValue());
            } else if (widget instanceof VocabularyList)
            {
                ListBox list = (VocabularyList) widget;
                GWTUtils.setSelectedItem(list, property.getPropertyFieldValue());
            } else
            {
                throw new IllegalStateException("Wrong widget type");
            }
        }
        GWTTestUtil.clickButtonWithID(DUMMY_ID + AbstractRegistrationForm.SAVE_BUTTON);
    }
}
