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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericDataSetViewer}.
 * 
 * @author Piotr Buczek
 */
@DoNotRunWith(Platform.HtmlUnitUnknown)
public class GenericDataSetViewerTest extends AbstractGWTTestCase
{

    private static final String PARENT_DATASET_CODE_WITH_SAMPLE = "20081105092159111-1";

    private static final String CHILD_DATASET_CODE = "20081105092259000-9";

    private static final String EXP_TEST_1 = "/CISD/NEMO/EXP-TEST-1";

    private static final String EXP_REUSE_ID = "/CISD/DEFAULT/EXP-REUSE";

    private static final String SAMPLE_TEST_1 = "CP-TEST-1";

    private static final String DS_TYPE_HCS_IMAGE = "HCS_IMAGE";

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

    public final void testShowDataSetWithDetailsAndSample()
    {
        final String parentCodes[] = {};
        final String childCodes[] =
        { "20081105092259000-9" };

        prepareShowDataSet(PARENT_DATASET_CODE_WITH_SAMPLE);

        final CheckDataSet checkDataSet = new CheckDataSet();
        // attributes
        checkDataSet.property("Data Set Type").asCode(DS_TYPE_HCS_IMAGE);
        checkDataSet.property("Source Type").asString("MEASUREMENT");
        checkDataSet.property("Location").asString("a/1");
        checkDataSet.property("Data Store").asCode("STANDARD");
        checkDataSet.property("File Type").asString("TIFF");
        // properties
        checkDataSet.property("Comment").asProperty("no comment");
        checkDataSet.property("Gender").asProperty("FEMALE");
        checkDataSet.property("any_material").asProperty("1000_C (SIRNA)");
        checkDataSet.property("bacterium").asProperty("BACTERIUM1 (BACTERIUM)");
        // high level entity relationships
        checkDataSet.property("Experiment").asString(EXP_TEST_1);
        checkDataSet.property("Sample").asCode(SAMPLE_TEST_1);

        checkDataSetRelationships(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION,
                checkDataSet.createParentsTableCheck(), parentCodes);
        checkDataSetRelationships(DisplayTypeIDGenerator.DATA_SET_CHILDREN_SECTION,
                checkDataSet.createChildrenTableCheck(), childCodes);

        remoteConsole.prepare(checkDataSet);

        launchTest();
    }

    public final void testShowExperimentDataSetWithParents()
    {
        final String parentCodes[] =
        { "20081105092158673-1", "20081105092159111-1", "20081105092159222-2",
                "20081105092159333-3" };
        final String childCodes[] =
        { "20081105092259900-0", "20081105092259900-1" };

        prepareShowDataSet(CHILD_DATASET_CODE);

        final CheckDataSet checkDataSet = new CheckDataSet();
        // attributes
        checkDataSet.property("Data Set Type").asCode(DS_TYPE_HCS_IMAGE);
        checkDataSet.property("Source Type").asString("DERIVED");
        checkDataSet.property("Location").asString("xml/result-9");
        checkDataSet.property("Data Store").asCode("STANDARD");
        checkDataSet.property("File Type").asString("XML");
        // properties
        checkDataSet.property("Comment").asProperty("no comment");
        // high level entity relationships
        checkDataSet.property("Experiment").asString(EXP_REUSE_ID);

        checkDataSetRelationships(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION,
                checkDataSet.createParentsTableCheck(), parentCodes);
        checkDataSetRelationships(DisplayTypeIDGenerator.DATA_SET_CHILDREN_SECTION,
                checkDataSet.createChildrenTableCheck(), childCodes);

        remoteConsole.prepare(checkDataSet);

        launchTest();
    }

    private void checkDataSetRelationships(IDisplayTypeIDGenerator sectionIdGenerator,
            CheckTableCommand checkTable, String[] expectedCodes)
    {
        activateTab(createSectionsTabPanelId(), createSectionId(sectionIdGenerator));
        checkTable.expectedSize(expectedCodes.length);
        for (String code : expectedCodes)
        {
            checkTable.expectedRow(new DataSetRow(code));
        }
        remoteConsole.prepare(checkTable);
    }

    private void prepareShowDataSet(String dataSetCode)
    {
        // Open data set viewer by simulating 2 steps:
        // - search for data set with given code,
        // - click on a result row and then on 'show details' button.
        loginAndInvokeAction(ActionMenuKind.DATA_SET_MENU_SEARCH);
        remoteConsole.prepare(FillSearchCriteria.searchForDataSetWithCode(dataSetCode));
        remoteConsole.prepare(new ShowDataSet(dataSetCode));
    }

}
