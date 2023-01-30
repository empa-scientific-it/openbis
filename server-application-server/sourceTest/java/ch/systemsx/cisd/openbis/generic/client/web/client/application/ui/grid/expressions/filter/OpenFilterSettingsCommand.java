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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnSettingsDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * After the data from table with given id are loaded, the settings dialog is opened and filters tab selected. The command activates loading data with
 * callback id: <code> FilterGrid.createGridId(gridDisplayId)</code>.
 * 
 * @author Izabela Adamczyk
 */
public class OpenFilterSettingsCommand extends CheckTableCommand
{

    private final String gridId;

    private final String gridDisplayId;

    public OpenFilterSettingsCommand(String gridId, String gridDisplayId)
    {
        super(gridId);
        this.gridId = gridId;
        this.gridDisplayId = gridDisplayId;
    }

    @Override
    public void execute()
    {
        super.execute();
        GWTTestUtil.clickButtonWithID(BrowserGridPagingToolBar.CONFIG_BUTTON_ID + gridId);
        GWTTestUtil.selectTabItemWithId(ColumnSettingsDialog.TAB_PANEL_ID_PREFIX + gridDisplayId,
                ColumnSettingsDialog.FILTERS_TAB + gridDisplayId);
    }
}
