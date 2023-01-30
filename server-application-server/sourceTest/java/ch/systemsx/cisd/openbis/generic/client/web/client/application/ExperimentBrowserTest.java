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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.CheckExperimentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns.ExperimentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * A {@link AbstractGWTTestCase} extension to test experiment browser.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentBrowserTest extends AbstractGWTTestCase
{

    public final void testListAllExperiments()
    {
        loginAndInvokeAction(ActionMenuKind.EXPERIMENT_MENU_BROWSE);
        remoteConsole.prepare(new ListExperiments("NEMO (CISD)", EntityType.ALL_TYPES_CODE));
        CheckExperimentTable table = new CheckExperimentTable();

        // Test that there are two experiments displayed that have different types, and a proper
        // value is displayed in property columns that are assigned only to one of these types
        // (union of property values is displayed).
        table.expectedRow(new ExperimentRow("EXP-TEST-1", "COMPOUND_HCS").withUserPropertyCell(
                "COMMENT", "cmnt")); // 'COMMENT' is assigned only to 'COMPOUND_HCS' experiment type
        table.expectedRow(new ExperimentRow("EXP-TEST-2", "SIRNA_HCS").withUserPropertyCell(
                "GENDER", "FEMALE")); // 'GENDER' is assigned only to 'SIRNA_HCS' experiment type

        table.expectedColumnsNumber(15);
        remoteConsole.prepare(table.expectedSize(5));

        launchTest();
    }

    public final void testListExperiments()
    {
        loginAndInvokeAction(ActionMenuKind.EXPERIMENT_MENU_BROWSE);
        remoteConsole.prepare(new ListExperiments("DEFAULT (CISD)", "SIRNA_HCS"));
        CheckExperimentTable table = new CheckExperimentTable();
        table.expectedRow(new ExperimentRow("EXP-REUSE").valid());
        table.expectedRow(new ExperimentRow("EXP-X").deleted());
        table.expectedColumnsNumber(14);
        remoteConsole.prepare(table.expectedSize(2));

        launchTest();
    }
}