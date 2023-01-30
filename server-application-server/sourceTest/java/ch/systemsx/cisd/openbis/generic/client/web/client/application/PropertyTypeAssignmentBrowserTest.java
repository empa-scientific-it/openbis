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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.CheckPropertyTypeAssignmentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeAssignmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Property Type Assignment Browser</i>.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentBrowserTest extends AbstractGWTTestCase
{

    public final void testListAssignments()
    {
        loginAndInvokeAction(ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS);
        CheckPropertyTypeAssignmentTable table = new CheckPropertyTypeAssignmentTable();
        table.expectedRow(new Row()
                .withCell(PropertyTypeAssignmentGridColumnIDs.PROPERTY_TYPE_CODE, "DESCRIPTION")
                .withCell(PropertyTypeAssignmentGridColumnIDs.ASSIGNED_TO, "CONTROL_LAYOUT")
                .withCell(PropertyTypeAssignmentGridColumnIDs.TYPE_OF, "Sample"));
        remoteConsole.prepare(table.expectedSize(35));

        launchTest();
    }
}
