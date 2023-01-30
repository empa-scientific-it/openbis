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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * {@link ListBox} with RoleSets.
 * 
 * @author Izabela Adamczyk
 */
public class RoleListBox extends ListBox
{

    private IViewContext<ICommonClientServiceAsync> viewContext;

    private List<RoleWithHierarchy> roles;

    public RoleListBox(IViewContext<ICommonClientServiceAsync> viewContext, final SpaceSelectionWidget groupWidget,
            final ProjectSelectionWidget projectWidget)
    {
        this.viewContext = viewContext;

        for (RoleWithHierarchy visibleRoleCode : getRoles())
        {
            addItem(visibleRoleCode.toString());
        }
        setVisibleItemCount(1);
        updateWidgetsVisibility(groupWidget, projectWidget);

        addChangeHandler(new ChangeHandler()
            {

                @Override
                public final void onChange(final ChangeEvent sender)
                {
                    updateWidgetsVisibility(groupWidget, projectWidget);
                }
            });
    }

    public final RoleWithHierarchy getValue()
    {
        return getRoles().get(getSelectedIndex());
    }

    private void updateWidgetsVisibility(final SpaceSelectionWidget group, final ProjectSelectionWidget project)
    {
        int index = getSelectedIndex();

        if (index < 0 || index >= getRoles().size())
        {
            return;
        }

        RoleWithHierarchy role = getRoles().get(index);
        boolean spaceLevel = role.isSpaceLevel();
        boolean projectLevel = role.isProjectLevel();

        FieldUtil.setMandatoryFlag(group, spaceLevel);
        group.setVisible(spaceLevel);

        FieldUtil.setMandatoryFlag(project, projectLevel);
        project.setVisible(projectLevel);
    }

    private List<RoleWithHierarchy> getRoles()
    {
        if (roles == null)
        {
            boolean projectLevelAuthorizationEnabled = viewContext.getModel().getApplicationInfo().isProjectLevelAuthorizationEnabled();

            roles = new ArrayList<RoleWithHierarchy>();
            for (RoleWithHierarchy role : RoleWithHierarchy.values())
            {
                if (RoleWithHierarchy.INSTANCE_DISABLED.equals(role))
                {
                    continue;
                } else if (role.isProjectLevel() && false == projectLevelAuthorizationEnabled)
                {
                    continue;
                } else
                {
                    roles.add(role);
                }
            }
        }
        return roles;
    }

}
