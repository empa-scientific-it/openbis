/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AuthorizationGroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * Wait until all fields are loaded and fill role assignment form.
 * 
 * @author Izabela Adamczyk
 */
public class FillRoleAssignmentForm extends AbstractDefaultTestCommand
{

    private final String groupNameOrNull;

    private final String grantee;

    private final String roleNameOrNull;

    private final boolean personRole;

    public static final FillRoleAssignmentForm fillPersonRole(final String groupNameOrNull,
            final String grantee, final String roleNameOrNull)
    {
        return new FillRoleAssignmentForm(true, groupNameOrNull, grantee, roleNameOrNull);
    }

    public static final FillRoleAssignmentForm fillAuthorizationGroupRole(
            final String groupNameOrNull, final String grantee, final String roleNameOrNull)
    {
        return new FillRoleAssignmentForm(false, groupNameOrNull, grantee, roleNameOrNull);
    }

    private FillRoleAssignmentForm(final boolean personRole, final String groupNameOrNull,
            final String grantee, final String roleNameOrNull)
    {
        super();
        this.personRole = personRole;
        assert groupNameOrNull == null && roleNameOrNull == null || groupNameOrNull != null
                && roleNameOrNull != null;
        this.groupNameOrNull = groupNameOrNull;
        this.grantee = grantee;
        this.roleNameOrNull = roleNameOrNull;
    }

    @Override
    public void execute()
    {
        final RoleListBox listBox =
                (RoleListBox) GWTTestUtil.getListBoxWithID(AddRoleAssignmentDialog.ROLE_FIELD_ID);
        if (groupNameOrNull != null)
        {
            String groupSelectorId =
                    DropDownList.ID + SpaceSelectionWidget.SUFFIX
                            + AddRoleAssignmentDialog.PREFIX;
            GWTTestUtil.selectValueInSelectionWidget(groupSelectorId, ModelDataPropertyNames.CODE,
                    groupNameOrNull);
            GWTUtils.setSelectedItem(listBox, roleNameOrNull);
        } else
        {
            GWTUtils.setSelectedItem(listBox, RoleWithHierarchy.INSTANCE_ADMIN.toString());
        }
        if (personRole == false)
        {
            GWTTestUtil.setRadioValue(AddRoleAssignmentDialog.AUTH_GROUP_RADIO, true);
            GWTTestUtil.selectValueInSelectionWidget(DropDownList.ID
                    + AuthorizationGroupSelectionWidget.SUFFIX + AddRoleAssignmentDialog.PREFIX,
                    ModelDataPropertyNames.CODE, grantee);
        } else
        {
            GWTTestUtil.selectValueInSelectionWidget(DropDownList.ID
                    + PersonSelectionWidget.SUFFIX + AddRoleAssignmentDialog.PREFIX,
                    ModelDataPropertyNames.CODE, grantee);
        }
        GWTTestUtil.clickButtonWithID(AbstractSaveDialog.SAVE_BUTTON_ID);
    }
}
