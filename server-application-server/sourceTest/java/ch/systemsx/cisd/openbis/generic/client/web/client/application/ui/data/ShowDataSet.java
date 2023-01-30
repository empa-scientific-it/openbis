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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * A {@link AbstractDefaultTestCommand} extension for showing detail view of a data set with given code. View is displayed because of simulation of a
 * click on show details button in a toolbar of a data set search results browser.
 * 
 * @author Piotr Buczek
 */
public class ShowDataSet extends AbstractDefaultTestCommand
{
    private final String code;

    public ShowDataSet(final String code)
    {
        this.code = code;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute()
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(DataSetSearchHitGrid.GRID_ID);
        assertTrue(widget instanceof Grid<?>);
        final Grid<BaseEntityModel<AbstractExternalData>> table =
                (Grid<BaseEntityModel<AbstractExternalData>>) widget;
        GridTestUtils.fireSelectRow(table, ExperimentBrowserGridColumnIDs.CODE, code);
        GWTTestUtil.clickButtonWithID(DataSetSearchHitGrid.BROWSER_ID
                + AbstractExternalDataGrid.SHOW_DETAILS_BUTTON_ID_SUFFIX);
    }
}
