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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvokeActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ChooseTypeOfNewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleRegistrationForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericSampleRegistrationTest extends AbstractGWTTestCase
{
    /**
     * Don't use directly - use {@link #getFormID()}.
     * <p>
     * NOTE: Cannot set value statically - tests construction fails.
     */
    @Deprecated
    private static String FORM_ID;

    private static String getFormID()
    {
        if (FORM_ID == null)
        {
            FORM_ID = AbstractGenericEntityRegistrationForm.createId((TechId) null, EntityKind.SAMPLE);
        }
        return FORM_ID;
    }

    private static final String PLATE_GEOMETRY = "$PLATE_GEOMETRY";

    private static final String CONTROL_LAYOUT = "CONTROL_LAYOUT";

    private static final String CELL_PLATE = "CELL_PLATE";

    private static final String GROUP_CL = "GROUP_CL";

    private static final String SHARED_CL = "SHARED_CL";

    private final void loginAndPreprareRegistration(final String sampleType)
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewSample(sampleType));
    }

    public final void testRegisterGroupSample()
    {
        final String sampleTypeCode = CONTROL_LAYOUT;
        loginAndPreprareRegistration(sampleTypeCode);
        String propertyFieldId = getFormID() + GWTUtils.escapeToFormId(PLATE_GEOMETRY);
        FillSampleRegistrationForm addProperty =
                new FillSampleRegistrationForm("CISD", GROUP_CL).addProperty(new PropertyField(
                        propertyFieldId, "1536_WELLS_32X48"));
        remoteConsole.prepare(addProperty);
        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE));
        remoteConsole.prepare(new ListSamples("CISD", sampleTypeCode));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(GROUP_CL)
                .identifier("CISD", "CISD")));
        launchTest();
    }

    /**
     * Tests that authorization annotations of {@link IGenericClientService#registerSample} are obeyed.
     */
    public final void testRegisterSampleByAnUnauthorizedUser()
    {
        loginAndInvokeAction("observer", "observer", ActionMenuKind.SAMPLE_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewSample(CONTROL_LAYOUT));
        remoteConsole.prepare(new FillSampleRegistrationForm("TESTGROUP", GROUP_CL + "1")
                .addProperty(new PropertyField(getFormID()
                        + GWTUtils.escapeToFormId(PLATE_GEOMETRY), "1536_WELLS_32X48")));
        FailureExpectation failureExpectation =
                new FailureExpectation(GenericSampleRegistrationForm.RegisterSampleCallback.class)
                        .with("Authorization failure: None of method roles "
                                + "'[SPACE_USER, SPACE_POWER_USER, SPACE_ADMIN, INSTANCE_ADMIN]' "
                                + "could be found in roles of user 'observer'.");
        remoteConsole.prepare(failureExpectation);
        launchTest();
    }

    public final void testRegisterGroupSampleWithExperiment()
    {
        final String sampleCode = "cp-with-exp";
        final String sampleTypeCode = CELL_PLATE;
        loginAndPreprareRegistration(sampleTypeCode);
        remoteConsole.prepare(new FillSampleRegistrationForm("CISD", sampleCode)
                .experiment("/CISD/NEMO/EXP1"));
        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE));
        remoteConsole.prepare(new ListSamples("CISD", sampleTypeCode));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(sampleCode
                .toUpperCase()).identifier("CISD", "CISD").experiment("CISD", "NEMO", "EXP1")));
        launchTest();
    }

    public final void testRegisterSharedSample()
    {
        final String sampleTypeCode = CONTROL_LAYOUT;
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewSample(sampleTypeCode));
        final String description = "A very nice control layout.";
        remoteConsole.prepare(new FillSampleRegistrationForm(
                SpaceSelectionWidget.SHARED_SPACE_CODE, SHARED_CL).addProperty(
                new PropertyField(getFormID() + "description", description)).addProperty(
                new PropertyField(getFormID() + GWTUtils.escapeToFormId(PLATE_GEOMETRY),
                        "1536_WELLS_32X48")));
        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE));
        remoteConsole.prepare(new ListSamples(SpaceSelectionWidget.SHARED_SPACE_CODE,
                sampleTypeCode));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(SHARED_CL)
                .identifier("CISD").withUserPropertyCell("DESCRIPTION", description)));
        launchTest(DEFAULT_TIMEOUT * 2);
    }
}
