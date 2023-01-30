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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvokeActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.CheckExperimentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ChooseTypeOfNewExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns.ExperimentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;
import ch.systemsx.cisd.openbis.systemtest.plugin.generic.ExperimentRegistrationTest;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericExperimentRegistrationForm}.
 * 
 * @author Izabela Adamczyk
 * @see ExperimentRegistrationTest
 */
public class GenericExperimentRegistrationTest extends AbstractGWTTestCase
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
            FORM_ID =
                    AbstractGenericEntityRegistrationForm
                            .createId((TechId) null, EntityKind.EXPERIMENT);
        }
        return FORM_ID;
    }

    private final void loginAndPreprareRegistration(final String sampleType)
    {
        loginAndInvokeAction(ActionMenuKind.EXPERIMENT_MENU_NEW);
        remoteConsole.prepare(new ChooseTypeOfNewExperiment(sampleType));
    }

    public final void testRegister()
    {
        final String experimentTypeCode = "SIRNA_HCS";
        loginAndPreprareRegistration(experimentTypeCode);
        remoteConsole.prepare(new FillExperimentRegistrationForm("DEFAULT", "NEW_EXP_1", "")
                .addProperty(
                        new PropertyField(getFormID() + "user-description",
                                "New test experiment description.")).addProperty(
                        new PropertyField(getFormID() + "user-gender", "MALE")).addProperty(
                        new PropertyField(getFormID() + "user-purchase-date", "2008-12-17")));
        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.EXPERIMENT_MENU_BROWSE));
        remoteConsole.prepare(new ListExperiments("DEFAULT", experimentTypeCode));
        remoteConsole.prepare(new CheckExperimentTable()
                .expectedRow(new ExperimentRow("NEW_EXP_1")));
        launchTest();
    }

    public final void testRegisterExperimentWithSamples()
    {
        final String experimentTypeCode = "SIRNA_HCS";
        final String experimentCode = "NEW_EXP_WITH_SAMPLES";
        final String sampleCode = "3VCP8";
        final String project = "DEFAULT";
        loginAndPreprareRegistration(experimentTypeCode);

        remoteConsole.prepare(new FillExperimentRegistrationForm(project, experimentCode,
                sampleCode).addProperty(
                new PropertyField(getFormID() + "user-description",
                        "New test experiment with samples.")).addProperty(
                new PropertyField(getFormID() + "user-gender", "MALE")).addProperty(
                new PropertyField(getFormID() + "user-purchase-date", "2008-12-18")));
        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE));
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow(sampleCode).identifier("CISD", "CISD").valid().experiment(
                "CISD", project, experimentCode));
        launchTest();
    }
}
