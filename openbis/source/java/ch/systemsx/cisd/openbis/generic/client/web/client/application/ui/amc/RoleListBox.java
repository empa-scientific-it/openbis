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

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;

/**
 * {@link ListBox} with RoleSets.
 * 
 * @author Izabela Adamczyk
 */
public class RoleListBox extends ListBox
{
    public RoleListBox(final TextField<String> groupField)
    {
        RoleSetCode[] values = RoleSetCode.values();
        for (RoleSetCode visibleRoleCode : values)
        {
            addItem(visibleRoleCode.toString());
        }
        setVisibleItemCount(1);

        addChangeListener(new ChangeListener()
            {
                //
                // ChangeListener
                //

                public final void onChange(final Widget sender)
                {
                    boolean groupLevel = RoleSetCode.values()[getSelectedIndex()].isGroupLevel();
                    FieldUtil.setMandatoryFlag(groupField, groupLevel);
                    groupField.setVisible(groupLevel);
                }
            });

    }

    public final RoleSetCode getValue()
    {
        return RoleSetCode.values()[getSelectedIndex()];
    }
}
