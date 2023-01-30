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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * Selects filter with given name.
 * 
 * @author Izabela Adamczyk
 */
public class ApplyFilterCommand extends AbstractDefaultTestCommand
{

    private final String filterName;

    private final String gridId;

    public ApplyFilterCommand(String gridId, String filterName)
    {
        this.gridId = gridId;
        this.filterName = filterName;
    }

    @Override
    public void execute()
    {
        final ComboBox<FilterModel> filterSelectionWidget =
                (FilterSelectionWidget) GWTTestUtil.getWidgetWithID(DropDownList.ID
                        + FilterSelectionWidget.SUFFIX + gridId);
        GWTUtils.setSelectedItem(filterSelectionWidget, ModelDataPropertyNames.NAME, filterName);
        GWTTestUtil.clickTextToolItemWithID(FilterToolbar.createId(FilterToolbar.APPLY_ID, gridId));
    }

}
