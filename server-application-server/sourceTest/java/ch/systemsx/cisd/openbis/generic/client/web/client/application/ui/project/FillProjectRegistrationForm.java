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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for filling {@link AbstractProjectEditRegisterForm}.
 * 
 * @author Izabela Adamczyk
 */
public final class FillProjectRegistrationForm extends AbstractDefaultTestCommand
{
    private final String code;

    private final String groupName;

    private final String description;

    public FillProjectRegistrationForm(final String projectCode, final String groupName,
            final String description)
    {
        assert projectCode != null : "Unspecified code.";
        assert groupName != null : "Unspecified space.";

        this.code = projectCode;
        this.groupName = groupName;
        this.description = description;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        GWTTestUtil.setTextField(ProjectRegistrationForm.createId() + "_code", code);
        GWTTestUtil.setTextField(ProjectRegistrationForm.createId() + "_description", description);
        final SpaceSelectionWidget groupSelector =
                (SpaceSelectionWidget) GWTTestUtil.getWidgetWithID(DropDownList.ID
                        + SpaceSelectionWidget.SUFFIX
                        + AbstractProjectEditRegisterForm.createId(null));
        GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupName);

        GWTTestUtil.clickButtonWithID(ProjectRegistrationForm.createId()
                + AbstractRegistrationForm.SAVE_BUTTON);
    }

}
