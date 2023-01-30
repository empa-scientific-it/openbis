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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.CheckMaterialTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.ListMaterials;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test material browser.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialBrowserTest extends AbstractGWTTestCase
{

    public final void testListMaterials()
    {
        loginAndInvokeAction(ActionMenuKind.MATERIAL_MENU_BROWSE);

        remoteConsole.prepare(new ListMaterials("BACTERIUM"));
        CheckMaterialTable table = new CheckMaterialTable();
        table.expectedRow(new MaterialRow("BACTERIUM-X").withUserPropertyCell("ORGANISM", "FLY"));
        table.expectedRow(new MaterialRow("BACTERIUM-Y"));
        table.expectedRow(new MaterialRow("BACTERIUM1"));
        table.expectedRow(new MaterialRow("BACTERIUM2"));
        remoteConsole.prepare(table.expectedSize(4));

        launchTest();
    }
}
