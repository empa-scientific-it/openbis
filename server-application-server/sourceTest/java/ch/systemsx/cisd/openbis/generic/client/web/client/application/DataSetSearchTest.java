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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetSearchRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Data Set Search</i>.
 * 
 * @author Izabela Adamczyk
 */
@DoNotRunWith(Platform.HtmlUnitUnknown)
public class DataSetSearchTest extends AbstractGWTTestCase
{

    private static final int TOTAL_NUM_OF_COLUMNS = 25;

    public final void testSearchByDataSetProperty()
    {
        loginAndGotoSearchTab();
        FillSearchCriteria fillCriteriaCmd = new FillSearchCriteria();
        fillCriteriaCmd.addPropertyCriterion("Comment", "no comment");
        remoteConsole.prepare(fillCriteriaCmd);

        final CheckTableCommand checkResultTableCmd = createCheckSearchGridCmd();
        checkResultTableCmd.expectedSize(10);
        DataSetSearchRow row = new DataSetSearchRow();
        row.withCell(ExternalDataGridColumnIDs.LOCATION, "a/1");
        row.withPropertyCell("comment", "no comment");
        checkResultTableCmd.expectedRow(row);
        checkResultTableCmd.expectedColumnsNumber(TOTAL_NUM_OF_COLUMNS);
        remoteConsole.prepare(checkResultTableCmd);

        launchTest();
    }

    public final void testSearchForFileType()
    {
        loginAndGotoSearchTab();
        FillSearchCriteria fillCriteriaCmd = new FillSearchCriteria();
        fillCriteriaCmd.addAttributeCriterion(DataSetAttributeSearchFieldKind.FILE_TYPE, "tiff");

        remoteConsole.prepare(fillCriteriaCmd);

        final CheckTableCommand checkResultTableCmd = createCheckSearchGridCmd();
        checkResultTableCmd.expectedSize(2);
        Row row1 = createTiffRow().withCell(ExternalDataGridColumnIDs.LOCATION, "xxx/yyy/zzz");
        checkResultTableCmd.expectedRow(row1);
        Row row2 = createTiffRow().withCell(ExternalDataGridColumnIDs.LOCATION, "a/1");
        checkResultTableCmd.expectedRow(row2);

        remoteConsole.prepare(checkResultTableCmd);

        launchTest();
    }

    private Row createTiffRow()
    {
        return new DataSetSearchRow().withCell(ExternalDataGridColumnIDs.FILE_FORMAT_TYPE, "TIFF");
    }

    private static CheckTableCommand createCheckSearchGridCmd()
    {
        return new CheckTableCommand(DataSetSearchHitGrid.GRID_ID);
    }

    private void loginAndGotoSearchTab()
    {
        loginAndInvokeAction(ActionMenuKind.DATA_SET_MENU_SEARCH);
    }
}
