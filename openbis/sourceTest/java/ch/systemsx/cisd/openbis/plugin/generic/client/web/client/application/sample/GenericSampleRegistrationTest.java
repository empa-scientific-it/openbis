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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ChooseTypeOfNewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ShowSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;
import ch.systemsx.cisd.openbis.generic.shared.IPluginCommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.SampleTypeCode;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleRegistrationForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericSampleRegistrationTest extends AbstractGWTTestCase
{

    private static final String GROUP_CL = "GROUP_CL";

    private static final String SHARED_CL = "SHARED_CL";

    private final void loginAndPreprareRegistration(final String sampleType)
    {
        loginAndGotoTab(ActionMenuKind.SAMPLE_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewSample(sampleType));
    }

    public final void testRegisterGroupSample()
    {
        final String sampleTypeCode = SampleTypeCode.CONTROL_LAYOUT.getCode();
        loginAndPreprareRegistration(sampleTypeCode);
        remoteConsole.prepare(new FillSampleRegistrationForm("CISD", GROUP_CL)
                .addProperty(new PropertyField(GenericSampleRegistrationForm.ID + "plate-geometry",
                        "1536_WELLS_32X48")));
        remoteConsole.prepare(new OpenTab(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples("CISD", sampleTypeCode));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(GROUP_CL)
                .identifier("CISD", "CISD")));
        launchTest(20000);
    }

    /**
     * Tests that authorization annotations of
     * {@link IPluginCommonServer#registerSample(String, NewSample, List)} are obeyed.
     */
    public final void testRegisterSampleByAnUnauthorizedUser()
    {
        loginAndGotoTab("observer", "observer", ActionMenuKind.SAMPLE_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewSample(SampleTypeCode.CONTROL_LAYOUT.getCode()));
        remoteConsole.prepare(new FillSampleRegistrationForm("TESTGROUP", GROUP_CL + "1")
                .addProperty(new PropertyField(GenericSampleRegistrationForm.ID + "plate-geometry",
                        "1536_WELLS_32X48")));
        FailureExpectation failureExpectation =
                new FailureExpectation(GenericSampleRegistrationForm.RegisterSampleCallback.class)
                        .with("Authorization failure: None of method roles "
                                + "'[GROUP.USER, GROUP.ADMIN, INSTANCE.ADMIN]' "
                                + "could be found in roles of user 'observer'.");
        remoteConsole.prepare(failureExpectation);
        launchTest(20000);
    }

    public final void testRegisterGroupSampleWithParent()
    {
        final String sampleCode = "dp4";
        final String sampleTypeCode = SampleTypeCode.DILUTION_PLATE.getCode();
        loginAndPreprareRegistration(sampleTypeCode);
        remoteConsole.prepare(new FillSampleRegistrationForm("CISD", sampleCode)
                .parent("MP1-MIXED"));
        remoteConsole.prepare(new OpenTab(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples("CISD", sampleTypeCode));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(sampleCode
                .toUpperCase()).identifier("CISD", "CISD")));
        launchTest(20000);
    }

    public final void testRegisterGroupSampleWithContainer()
    {
        final String sampleCode = "W12";
        final String sampleTypeCode = SampleTypeCode.WELL.getCode();
        final String containerCode = "3VCP5";
        loginAndPreprareRegistration(sampleTypeCode);
        remoteConsole.prepare(new FillSampleRegistrationForm("CISD", sampleCode)
                .container(containerCode));
        remoteConsole.prepare(new OpenTab(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples("CISD", SampleTypeCode.CELL_PLATE.getCode()));
        remoteConsole.prepare(new ShowSample(containerCode));
        final CheckSample checkSample =
                new CheckSample(GenericSampleViewerTest.GROUP_IDENTIFIER, containerCode);
        final CheckTableCommand componentsTable = checkSample.componentsTable().expectedSize(1);
        final String sampleCodeFieldIdent = CommonSampleColDefKind.CODE.id();
        componentsTable.expectedRow(new Row().withCell(sampleCodeFieldIdent, sampleCode));
        remoteConsole.prepare(checkSample);
        launchTest(60000);
    }

    public final void testRegisterSharedSample()
    {
        final String sampleTypeCode = SampleTypeCode.CONTROL_LAYOUT.getCode();
        loginAndGotoTab(ActionMenuKind.SAMPLE_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewSample(sampleTypeCode));
        final String description = "A very nice control layout.";
        remoteConsole.prepare(new FillSampleRegistrationForm(
                GroupSelectionWidget.SHARED_GROUP_CODE, SHARED_CL).addProperty(
                new PropertyField(GenericSampleRegistrationForm.ID + "user-description",
                        description)).addProperty(
                new PropertyField(GenericSampleRegistrationForm.ID + "plate-geometry",
                        "1536_WELLS_32X48")));
        remoteConsole.prepare(new OpenTab(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples(GroupSelectionWidget.SHARED_GROUP_CODE,
                sampleTypeCode));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(SHARED_CL)
                .identifier("CISD").property("DESCRIPTION", false, description)));
        launchTest(30000);
    }
}
