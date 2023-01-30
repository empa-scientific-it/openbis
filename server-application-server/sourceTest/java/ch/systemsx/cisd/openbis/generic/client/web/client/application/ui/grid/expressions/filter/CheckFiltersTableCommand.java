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

import com.extjs.gxt.ui.client.widget.Dialog;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * After the filters have been loaded closes the settings dialog.
 * 
 * @author Izabela Adamczyk
 */
public class CheckFiltersTableCommand extends CheckTableCommand
{

    private final String gridDisplayId;

    public CheckFiltersTableCommand(String gridDisplayId)
    {
        super(GridCustomFilterGrid.createGridId(gridDisplayId));
        this.gridDisplayId = gridDisplayId;
    }

    @Override
    public void execute()
    {
        super.execute();
        GWTTestUtil.clickButtonWithID(Dialog.OK + gridDisplayId);
    }
}
