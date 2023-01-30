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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvokeActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.CheckProjectTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.FillProjectRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Project Registration</i>.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectRegistrationTest extends AbstractGWTTestCase
{
    private static final String DESCRIPTION_PROJECT_007 = "Project No. 007";

    private static final String PROJECT_CODE_HER_MAJESTY = "H.E.R-MAJESTY";

    public final void testRegisterProject()
    {
        loginAndInvokeAction(TopMenu.ActionMenuKind.PROJECT_MENU_NEW);
        remoteConsole.prepare(new FillProjectRegistrationForm(PROJECT_CODE_HER_MAJESTY, "CISD",
                DESCRIPTION_PROJECT_007));

        remoteConsole.prepare(new InvokeActionMenu(TopMenu.ActionMenuKind.PROJECT_MENU_BROWSE));
        final CheckProjectTable table = new CheckProjectTable();
        table.expectedRow(new Row().withCell(ProjectGridColumnIDs.CODE, PROJECT_CODE_HER_MAJESTY)
                .withCell(ProjectGridColumnIDs.DESCRIPTION, DESCRIPTION_PROJECT_007));
        remoteConsole.prepare(table.expectedSize(5));

        launchTest();
    }
}
