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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * A {@link AbstractDefaultTestCommand} extension for deleting an authorization group with given code.
 * 
 * @author Izabela Adamczyk
 */
public class RemovePerson extends AbstractDefaultTestCommand
{
    private final String person;

    private final AuthorizationGroup authGroup;

    public RemovePerson(AuthorizationGroup authGroup, final String personId)
    {
        this.authGroup = authGroup;
        this.person = personId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute()
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(PersonGrid.createGridId(authGroup));
        assertTrue(widget instanceof Grid);
        final Grid<BaseEntityModel<Person>> table = (Grid<BaseEntityModel<Person>>) widget;
        GridTestUtils.fireSelectRow(table, PersonGridColumnIDs.USER_ID, person);
        GWTTestUtil.clickButtonWithID(PersonGrid.createRemoveButtonId(authGroup));
    }
}
