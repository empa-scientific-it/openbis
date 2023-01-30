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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CheckTabNotExists;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ShowExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericExperimentViewer}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentViewerTest extends AbstractGWTTestCase
{
    private static final class GetExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {
        public GetExperimentInfoCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Experiment result)
        {
            fail("Failure expected.");
        }
    }

    private static final String DEFAULT = "DEFAULT (CISD)";

    private static final String EXP_REUSE = "EXP-REUSE";

    private static final String EXP_REUSE_ID = "/CISD/DEFAULT/EXP-REUSE";

    private static final String EXP_REUSE_PERM_ID = "200811050940555-1032";

    private static final String EXP_X = "EXP-X";

    private static final String EXP_X_ID = "/CISD/DEFAULT/EXP-X";

    private static final String EXP_X_PERM_ID = "200811050937246-1031";

    private static final String EXP_TEST_1 = "EXP-TEST-1";

    private static final String EXP_TEST_1_ID = "/CISD/NEMO/EXP-TEST-1";

    private static final String EXP_TEST_1_PERM_ID = "200902091239077-1033";

    private static final String A_SIMPLE_EXPERIMENT = "A simple experiment";

    private static final String DOE_JOHN = "Doe, John";

    private static final String CISD_CISD_NEMO = "/CISD/NEMO";

    private static final String SIRNA_HCS = "SIRNA_HCS";

    private static final String COMPOUND_HCS = "COMPOUND_HCS";

    private static final String NEMO = "NEMO (CISD)";

    private static final String EXP1 = "EXP1";

    private static final String EXP1_ID = "/CISD/NEMO/EXP1";

    private static final String EXP1_PERM_ID = "200811050951882-1028";

    private static final String CELL_PLATE = "CELL_PLATE";

    private static final String REINFECT_PLATE = "REINFECT_PLATE";

    private static final TechId WILDCARD_ID = TechId.createWildcardTechId();

    private static final String createSectionsTabPanelId()
    {
        return GenericExperimentViewer.createId(WILDCARD_ID)
                + SectionsPanel.SECTIONS_TAB_PANEL_ID_SUFFIX;
    }

    private static final String createSectionId(IDisplayTypeIDGenerator generator)
    {
        return TabContent.createId(WILDCARD_ID.toString(), generator)
                + SectionsPanel.SECTION_ID_SUFFIX;
    }

    /**
     * Tests that authorization annotations of
     * {@link ICommonServer#getExperimentInfo(String, ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier)} are obeyed. This
     * is done by a direct invocation of {@link ICommonClientServiceAsync#getExperimentInfo(String,com.google.gwt.user.client.rpc.AsyncCallback)}
     * because the normal GUI only list experiments which are accessible by the user.
     */
    public final void testDirectInvocationOfGetExperimentInfoByAnUnauthorizedUser()
    {
        remoteConsole.prepare(new Login("observer", "observer"));
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                @Override
                public void execute()
                {
                    IViewContext<ICommonClientServiceAsync> viewContext =
                            client.tryToGetViewContext();
                    viewContext.getService().getExperimentInfo(CISD_CISD_NEMO + "/" + EXP1,
                            new GetExperimentInfoCallback(viewContext));
                }
            });
        remoteConsole.prepare(new FailureExpectation(GetExperimentInfoCallback.class)
                .with("Authorization failure: User 'observer' does not have enough privileges."));

        launchTest();
    }

    public final void testShowExperimentDetails()
    {
        prepareShowExperiment(NEMO, SIRNA_HCS, EXP1);
        final CheckExperiment checkExperiment = new CheckExperiment();
        checkExperiment.property("Experiment").asString(EXP1_ID);
        checkExperiment.property("PermID").matchingPattern(
                ".*<a .* href=\".*permId=" + EXP1_PERM_ID + ".*>" + EXP1_PERM_ID + "</a>.*");
        checkExperiment.property("Experiment Type").asCode(SIRNA_HCS);
        checkExperiment.property("Registrator").asPerson(DOE_JOHN);
        checkExperiment.property("Description").asProperty(A_SIMPLE_EXPERIMENT);
        checkExperiment.property("Gender").asProperty("MALE");
        remoteConsole.prepare(checkExperiment);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.ATTACHMENT_SECTION));
        final CheckTableCommand attachmentsTable =
                checkExperiment.createAttachmentsTableCheck().expectedSize(1);
        attachmentsTable.expectedRow(new Row().withCell(AttachmentGridColumnIDs.FILE_NAME,
                "exampleExperiments.txt").withCell(AttachmentGridColumnIDs.VERSION,
                versionCellTextWithoutAnchor(4)));
        remoteConsole.prepare(attachmentsTable);

        launchTest();
    }

    public final void testShowDeletedExperimentDetails()
    {
        prepareShowExperiment(DEFAULT, SIRNA_HCS, EXP_X);
        final CheckExperiment checkExperiment = new CheckExperiment();
        checkExperiment.property("Experiment").asString(EXP_X_ID);
        checkExperiment.property("PermID").matchingPattern(
                ".*<a .* href=\".*permId=" + EXP_X_PERM_ID + ".*>" + EXP_X_PERM_ID + "</a>.*");
        checkExperiment.property("Experiment Type").asCode(SIRNA_HCS);
        checkExperiment.property("Registrator").asPerson(DOE_JOHN);
        checkExperiment.property("Deletion").by(new IValueAssertion<Deletion>()
            {
                @Override
                public void assertValue(final Deletion deletion)
                {
                    assertEquals("Doe", deletion.getRegistrator().getLastName());
                    assertNull(deletion.getReason());
                }
            });
        checkExperiment.property("Description").asProperty(A_SIMPLE_EXPERIMENT);
        remoteConsole.prepare(checkExperiment);

        launchTest();
    }

    public final void testWebConfigurationHidingAttachmentSection()
    {
        prepareShowExperiment(NEMO, COMPOUND_HCS, EXP_TEST_1);
        final CheckExperiment checkExperiment = new CheckExperiment();
        checkExperiment.property("Experiment").asString(EXP_TEST_1_ID);
        checkExperiment.property("PermID").matchingPattern(
                ".*<a .* href=\".*permId=" + EXP_TEST_1_PERM_ID + ".*>" + EXP_TEST_1_PERM_ID
                        + "</a>.*");
        checkExperiment.property("Experiment Type").asCode(COMPOUND_HCS);
        remoteConsole.prepare(checkExperiment);

        remoteConsole.prepare(new CheckTabNotExists(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.ATTACHMENT_SECTION)));

        launchTest();
    }

    @DoNotRunWith(Platform.HtmlUnitUnknown)
    public final void testListOfAttachments()
    {
        prepareShowExperiment(DEFAULT, SIRNA_HCS, EXP_REUSE);
        final CheckExperiment checkExperiment = new CheckExperiment();
        checkExperiment.property("Experiment").asString(EXP_REUSE_ID);
        checkExperiment.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + EXP_REUSE_PERM_ID + ".*>" + EXP_REUSE_PERM_ID + "</a>.*");
        remoteConsole.prepare(checkExperiment);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.ATTACHMENT_SECTION));
        final CheckTableCommand attachmentsTable =
                checkExperiment.createAttachmentsTableCheck().expectedSize(2);
        attachmentsTable.expectedRow(new Row().withCell(AttachmentGridColumnIDs.FILE_NAME,
                "exampleExperiments.txt").withCell(AttachmentGridColumnIDs.VERSION,
                versionCellText(1)));
        attachmentsTable.expectedRow(new Row().withCell(AttachmentGridColumnIDs.FILE_NAME,
                "cellPlates.txt").withCell(AttachmentGridColumnIDs.VERSION, versionCellText(1)));
        remoteConsole.prepare(attachmentsTable);

        launchTest();
    }

    @DoNotRunWith(Platform.HtmlUnitUnknown)
    public final void testListOfSamples()
    {
        prepareShowExperiment(DEFAULT, SIRNA_HCS, EXP_REUSE);
        final CheckExperiment checkExperiment = new CheckExperiment();
        checkExperiment.property("Experiment").asString(EXP_REUSE_ID);
        checkExperiment.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + EXP_REUSE_PERM_ID + ".*>" + EXP_REUSE_PERM_ID + "</a>.*");
        remoteConsole.prepare(checkExperiment);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.EXPERIMENT_SAMPLES_SECTION));
        final CheckTableCommand sampleTable =
                checkExperiment.createSampleTableCheck().expectedSize(7);
        sampleTable.expectedRow(new SampleRow("CP1-A1", CELL_PLATE)
                .derivedFromAncestors("CISD:/CISD/DP1-A"));
        sampleTable.expectedRow(new SampleRow("CP1-A2", CELL_PLATE)
                .derivedFromAncestors("CISD:/CISD/DP1-A"));
        sampleTable.expectedRow(new SampleRow("CP1-B1", CELL_PLATE)
                .derivedFromAncestors("CISD:/CISD/DP1-B"));
        sampleTable.expectedRow(new SampleRow("CP2-A1", CELL_PLATE)
                .derivedFromAncestors("CISD:/CISD/DP2-A"));
        sampleTable.expectedRow(new SampleRow("RP1-A2X", REINFECT_PLATE)
                .derivedFromAncestors("CISD:/CISD/CP1-A2"));
        sampleTable.expectedRow(new SampleRow("RP1-B1X", REINFECT_PLATE)
                .derivedFromAncestors("CISD:/CISD/CP1-B1"));
        sampleTable.expectedRow(new SampleRow("RP2-A1X", REINFECT_PLATE)
                .derivedFromAncestors("CISD:/CISD/CP2-A1"));
        remoteConsole.prepare(sampleTable);

        launchTest();
    }

    @DoNotRunWith(Platform.HtmlUnitUnknown)
    public final void testListOfDataSets()
    {
        prepareShowExperiment(NEMO, SIRNA_HCS, EXP1);
        final CheckExperiment checkExperiment = new CheckExperiment();
        checkExperiment.property("Experiment").asString(EXP1_ID);
        checkExperiment.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + EXP1_PERM_ID + ".*>" + EXP1_PERM_ID + "</a>.*");
        remoteConsole.prepare(checkExperiment);

        activateTab(createSectionsTabPanelId(),
                createSectionId(DisplayTypeIDGenerator.DATA_SETS_SECTION));
        final CheckTableCommand datasetTable =
                checkExperiment.createDataSetTableCheck().expectedSize(2);
        datasetTable.expectedRow(new DataSetRow("20081105092159188-3").valid().derived());
        datasetTable.expectedRow(new DataSetRow("20081105092158673-1").deleted()
                .withSample("CISD:/CISD/3VCP1").withSampleType("CELL_PLATE").notDerived()
                .withIsComplete(null));
        datasetTable.expectedColumnsNumber(25);
        final String commentColIdent = GridTestUtils.getPropertyColumnIdentifier("COMMENT", false);
        datasetTable.expectedColumnHidden(commentColIdent, true);
        remoteConsole.prepare(datasetTable);

        launchTest();
    }

    private void prepareShowExperiment(final String projectName, final String experimentTypeName,
            final String experimentCode)
    {
        loginAndInvokeAction(ActionMenuKind.EXPERIMENT_MENU_BROWSE);
        remoteConsole.prepare(new ListExperiments(projectName, experimentTypeName));
        remoteConsole.prepare(new ShowExperiment(experimentCode));
    }

    private final static String versionCellTextWithoutAnchor(int version)
    {
        return version + " (show all versions)";
    }

    private final static String versionCellText(int version)
    {
        return version + " (<a href=\"#\">show all versions</a>)";
    }
}
