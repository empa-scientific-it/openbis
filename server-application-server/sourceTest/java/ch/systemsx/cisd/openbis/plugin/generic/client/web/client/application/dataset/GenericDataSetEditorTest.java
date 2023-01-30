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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSetEditor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericDataSetEditForm}.
 * 
 * @author Piotr Buczek
 */
@DoNotRunWith(Platform.HtmlUnitUnknown)
public class GenericDataSetEditorTest extends AbstractGWTTestCase
{
    // private static final String DS_WITH_DIRECT_SAMPLE_CONNECTION_CODE = "20081105092158673-1";

    private static final String DS_WITH_MANY_PARENTS_CODE = "20081105092259000-9";

    private static final TechId WILDCARD_ID = TechId.createWildcardTechId();

    private static final String createSectionsTabPanelId()
    {
        return GenericDataSetViewer.createId(WILDCARD_ID)
                + SectionsPanel.SECTIONS_TAB_PANEL_ID_SUFFIX;
    }

    private static final String createSectionId(IDisplayTypeIDGenerator generator)
    {
        return TabContent.createId(WILDCARD_ID.toString(), generator)
                + SectionsPanel.SECTION_ID_SUFFIX;
    }

    public final void testEditDataSetWithParents()
    {
        final String modifiedDataSetCode = DS_WITH_MANY_PARENTS_CODE;
        // Remove three parents and add one in one go. Old parent codes:
        // 20081105092158673-1, 20081105092159111-1, 20081105092159222-2, 20081105092159333-3
        final String newParentCode = "20081105092159188-3";
        final String oldParentCode = "20081105092159111-1";
        // modify property value
        final String newCommentColumnValue = "new comment";

        prepareShowDataSetEditor(modifiedDataSetCode);

        final FillDataSetEditForm fillForm = new FillDataSetEditForm();
        fillForm.modifyParents(newParentCode + ", " + oldParentCode);
        fillForm.addProperty(new PropertyField("comment", newCommentColumnValue));
        remoteConsole.prepare(fillForm);
        remoteConsole.prepare(new ShowUpdatedDataSet());

        final CheckDataSet checkDataSet = new CheckDataSet();
        checkDataSet.property("Comment").asProperty(newCommentColumnValue);
        remoteConsole.prepare(checkDataSet);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION));
        final CheckTableCommand checkParents =
                checkDataSet.createParentsTableCheck().expectedSize(2);
        checkParents.expectedRow(new DataSetRow(newParentCode));
        checkParents.expectedRow(new DataSetRow(oldParentCode));
        remoteConsole.prepare(checkParents);

        launchTest();
    }

    // could be removed when we implement BO unit test or merged with first test
    public final void testEditDataSetParentsFailWithCycleRelationships()
    {
        final String modifiedDataSetCode = DS_WITH_MANY_PARENTS_CODE;
        final String descendantCode = "20081105092359990-2";

        prepareShowDataSetEditor(modifiedDataSetCode);

        remoteConsole.prepare(new FillDataSetEditForm().modifyParents(descendantCode));
        FailureExpectation failureExpectation =
                new FailureExpectation(GenericDataSetEditForm.UpdateDataSetCallback.class)
                        .with("Data Set '" + modifiedDataSetCode + "' is an ancestor of Data Set '"
                                + descendantCode
                                + "' and cannot be at the same time set as its child.");
        remoteConsole.prepare(failureExpectation);

        launchTest();
    }

    private void prepareShowDataSetEditor(String dataSetCode)
    {
        // Open data set editor by simulating 3 steps:
        // - search for data set with given code,
        // - click on a result row and then on 'show details' button,
        // - click on edit button in data set detail view.
        // It could be also done without 3rd step and with clicking on 'edit' button
        // in 2nd step instead.
        loginAndInvokeAction(ActionMenuKind.DATA_SET_MENU_SEARCH);
        remoteConsole.prepare(FillSearchCriteria.searchForDataSetWithCode(dataSetCode));
        remoteConsole.prepare(new ShowDataSet(dataSetCode));
        remoteConsole.prepare(new ShowDataSetEditor());
    }

    private class ShowUpdatedDataSet extends AbstractDefaultTestCommand
    {
        @Override
        public void execute()
        {
            String tabItemId =
                    GenericDataSetViewer.createId(TechId.createWildcardTechId())
                            + MainTabPanel.TAB_SUFFIX;
            GWTTestUtil.selectTabItemWithId(MainTabPanel.ID, tabItemId);
        }
    }

}
