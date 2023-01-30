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

import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * A {@link AbstractDefaultTestCommand} extension for filling {@link PropertyTypeRegistrationForm}.
 * 
 * @author Christian Ribeaud
 */
public final class FillPropertyTypeRegistrationForm extends AbstractDefaultTestCommand
{
    private final String code;

    private final String label;

    private final String description;

    private final DataTypeCode dataTypeCode;

    public FillPropertyTypeRegistrationForm(final String code, final String label,
            final String description, final DataTypeCode dataTypeCode)
    {
        assert code != null : "Unspecified code.";
        assert label != null : "Unspecified label.";
        assert description != null : "Unspecified description.";
        assert dataTypeCode != null : "Unspecified data type code.";

        this.code = code;
        this.label = label;
        this.description = description;
        this.dataTypeCode = dataTypeCode;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        GWTTestUtil.setTextField(PropertyTypeRegistrationForm.ID + "_code", code);
        GWTTestUtil.setTextField(PropertyTypeRegistrationForm.ID + "_label", label);
        GWTTestUtil.setTextField(PropertyTypeRegistrationForm.ID + "_description", description);
        final Widget widgetWithID =
                GWTTestUtil.getWidgetWithID(DropDownList.ID
                        + DataTypeSelectionWidget.SUFFIX);
        assertTrue(widgetWithID instanceof DataTypeSelectionWidget);
        final ComboBox<DataTypeModel> dataTypeSelectionWidget =
                (DataTypeSelectionWidget) widgetWithID;
        GWTUtils.setSelectedItem(dataTypeSelectionWidget, ModelDataPropertyNames.CODE, dataTypeCode
                .name());
        GWTTestUtil.clickButtonWithID(PropertyTypeRegistrationForm.ID
                + AbstractRegistrationForm.SAVE_BUTTON);
    }

}
