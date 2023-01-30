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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ShowSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractEntityDataSetsSection;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleViewer}.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericSampleViewerTest extends AbstractGWTTestCase
{

    private static final String CISD_ID_PREFIX = "CISD:/CISD/";

    private static final String CONTROL_LAYOUT_EXAMPLE = "CL1";

    private static final String CONTROL_LAYOUT_EXAMPLE_ID = CISD_ID_PREFIX + CONTROL_LAYOUT_EXAMPLE;

    private static final String CONTROL_LAYOUT_EXAMPLE_PERM_ID = "200811050919915-8";

    private static final String CELL_PLATE_EXAMPLE = "3VCP1";

    private static final String CELL_PLATE_EXAMPLE_ID = CISD_ID_PREFIX + CELL_PLATE_EXAMPLE;

    private static final String CELL_PLATE_EXAMPLE_PERM_ID = "200811050946559-983";

    private static final String CELL_PLATE_EXAMPLE_EXPERIMENT_ID = "/CISD/NEMO/EXP1";

    private static final String DIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092158673-1";

    private static final String INDIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092159188-3";

    private static final TechId WILDCARD_ID = TechId.createWildcardTechId();

    private static final String createSectionsTabPanelId()
    {
        return GenericSampleViewer.createId(WILDCARD_ID)
                + SectionsPanel.SECTIONS_TAB_PANEL_ID_SUFFIX;
    }

    private static final String createSectionId(IDisplayTypeIDGenerator generator)
    {
        return TabContent.createId(WILDCARD_ID.toString(), generator)
                + SectionsPanel.SECTION_ID_SUFFIX;
    }

    public final void testShowMasterPlateView()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CONTROL_LAYOUT"));
        remoteConsole.prepare(new ShowSample(CONTROL_LAYOUT_EXAMPLE));
        final CheckSample checkSample = new CheckSample();
        checkSample.property("Sample").asString(CONTROL_LAYOUT_EXAMPLE_ID);
        checkSample.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + CONTROL_LAYOUT_EXAMPLE_PERM_ID + ".*>"
                        + CONTROL_LAYOUT_EXAMPLE_PERM_ID + "</a>.*");
        checkSample.property("Sample Type").asCode("CONTROL_LAYOUT");
        checkSample.property("Registrator").asPerson("Doe, John");
        checkSample.property("Plate Geometry").asProperty("384_WELLS_16X24");
        checkSample.property("Description").asProperty("test control layout");
        remoteConsole.prepare(checkSample);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.CONTAINER_SAMPLES_SECTION));
        final CheckTableCommand checkComponentsTable =
                checkSample.createComponentsTableCheck().expectedSize(2);
        final String sampleSubcodeFieldIdent = SampleGridColumnIDs.SUBCODE;
        checkComponentsTable.expectedRow(new SampleRow(CONTROL_LAYOUT_EXAMPLE + ":A01", "WELL")
                .identifier("CISD", "CISD").partOfContainer(CONTROL_LAYOUT_EXAMPLE_ID)
                .withCell(sampleSubcodeFieldIdent, "A01"));
        checkComponentsTable.expectedRow(new SampleRow(CONTROL_LAYOUT_EXAMPLE + ":A03", "WELL")
                .identifier("CISD", "CISD").partOfContainer(CONTROL_LAYOUT_EXAMPLE_ID)
                .withCell(sampleSubcodeFieldIdent, "A03"));
        remoteConsole.prepare(checkComponentsTable);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.DATA_SETS_SECTION));
        remoteConsole.prepare(checkSample.createDataTableCheck().expectedSize(0));

        launchTest();
    }

    public final void testShowCellPlateView()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));

        final String parentCode1 = "3V-123";

        final CheckSample checkSample = new CheckSample();
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE_ID);
        checkSample.property("Experiment").asString(CELL_PLATE_EXAMPLE_EXPERIMENT_ID);
        checkSample.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + CELL_PLATE_EXAMPLE_PERM_ID + ".*>"
                        + CELL_PLATE_EXAMPLE_PERM_ID + "</a>.*");
        checkSample.property("Sample Type").asCode("CELL_PLATE");
        checkSample.property("Deletion").by(new IValueAssertion<Deletion>()
            {
                @Override
                public void assertValue(final Deletion deletion)
                {
                    assertEquals("Doe", deletion.getRegistrator().getLastName());
                    assertEquals("wrong-code", deletion.getReason());
                }
            });
        checkSample.property("Parent").asCode(parentCode1);
        checkSample.property("Parent").asDeletedEntity();
        remoteConsole.prepare(checkSample);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.DERIVED_SAMPLES_SECTION));
        final CheckTableCommand checkChildrenTable =
                checkSample.createChildrenTableCheck().expectedSize(2);
        checkChildrenTable.expectedRow(new SampleRow("3VRP1A", "REINFECT_PLATE").identifier("CISD",
                "CISD").derivedFromAncestors(CELL_PLATE_EXAMPLE_ID));
        checkChildrenTable.expectedRow(new SampleRow("3VRP1B", "REINFECT_PLATE").identifier("CISD",
                "CISD").derivedFromAncestors(CELL_PLATE_EXAMPLE_ID));
        remoteConsole.prepare(checkChildrenTable);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.DATA_SETS_SECTION));
        final CheckTableCommand checkDataTable = checkSample.createDataTableCheck().expectedSize(1);
        checkDataTable.expectedRow(new DataSetRow(DIRECTLY_CONNECTED_DATA_SET_CODE).deleted()
                .withFileFormatType("TIFF"));
        checkDataTable.expectedColumnsNumber(26);
        final String commentColIdent = GridTestUtils.getPropertyColumnIdentifier("COMMENT", false);
        checkDataTable.expectedColumnHidden(commentColIdent, true);
        remoteConsole.prepare(checkDataTable);

        launchTest();
    }

    public final void testShowDirectlyConnectedDataSets()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));

        // simplified sample properties check
        final CheckSample checkSample = new CheckSample();
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE_ID);
        checkSample.property("Experiment").asString(CELL_PLATE_EXAMPLE_EXPERIMENT_ID);
        remoteConsole.prepare(checkSample);

        // show data set section tab
        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.DATA_SETS_SECTION));

        // check directly connected datasets
        final CheckTableCommand checkDirectlyConnectedDataTable =
                new CheckTableCommand(SampleDataSetBrowser.createGridId(WILDCARD_ID));
        checkDirectlyConnectedDataTable.expectedSize(1);
        checkDirectlyConnectedDataTable
                .expectedRow(new DataSetRow(DIRECTLY_CONNECTED_DATA_SET_CODE).deleted()
                        .withFileFormatType("TIFF").withSample(CELL_PLATE_EXAMPLE_ID)
                        .withExperiment(CELL_PLATE_EXAMPLE_EXPERIMENT_ID));
        remoteConsole.prepare(checkDirectlyConnectedDataTable);

        // show indirectly connected data sets
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                @Override
                public void execute()
                {
                    String showOnlyDirectlyConnectedCheckBoxId =
                            GenericSampleViewer.createId(WILDCARD_ID)
                                    + AbstractEntityDataSetsSection.SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX;
                    GWTTestUtil.clickCheckBoxWithID(showOnlyDirectlyConnectedCheckBoxId);
                }
            });

        // check indirectly connected datasets
        final CheckTableCommand checkIndirectlyConnectedDataTable =
                new CheckTableCommand(SampleDataSetBrowser.createGridId(WILDCARD_ID));
        checkIndirectlyConnectedDataTable.expectedSize(6);
        checkIndirectlyConnectedDataTable
                .expectedRow(new DataSetRow(DIRECTLY_CONNECTED_DATA_SET_CODE).deleted()
                        .withFileFormatType("TIFF").withSample(CELL_PLATE_EXAMPLE_ID)
                        .withExperiment(CELL_PLATE_EXAMPLE_EXPERIMENT_ID));
        checkIndirectlyConnectedDataTable.expectedRow(new DataSetRow(
                INDIRECTLY_CONNECTED_DATA_SET_CODE).valid().withLocation("analysis/result")
                .withSample("").withExperiment(CELL_PLATE_EXAMPLE_EXPERIMENT_ID));
        // datasets connected to a different experiment
        final String differentExperimentIdentifier = "/CISD/DEFAULT/EXP-REUSE";
        assert differentExperimentIdentifier.equals(CELL_PLATE_EXAMPLE_EXPERIMENT_ID) == false;
        final String[] indirectlyConnectedDataSetLocations =
        { "xml/result-9", "xml/result-10", "xml/result-11", "xml/result-12" };
        for (String location : indirectlyConnectedDataSetLocations)
        {
            checkIndirectlyConnectedDataTable.expectedRow(new DataSetRow().valid().withSample("")
                    .withExperiment(differentExperimentIdentifier).withLocation(location));
        }
        remoteConsole.prepare(checkIndirectlyConnectedDataTable);

        launchTest();
    }

}
