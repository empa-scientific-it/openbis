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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CallbackClassCondition;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.ITestCommandWithCondition;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CheckGroupCommand extends CallbackClassCondition implements ITestCommandWithCondition<Object>
{

    private final String groupCode;

    public CheckGroupCommand(String groupCode)
    {
        super(AddGroupDialog.RegisterGroupCallback.class);
        this.groupCode = groupCode;
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        Widget widget = GWTTestUtil.getWidgetWithID(GroupsView.TABLE_ID);
        Assert.assertTrue(widget instanceof Grid);
        Grid<GroupModel> table = (Grid<GroupModel>) widget;
        ListStore<GroupModel> store = table.getStore();
        for (int i = 0, n = store.getCount(); i < n; i++)
        {
            GroupModel groupModel = store.getAt(i);
            Object value = groupModel.get(GroupModel.CODE);
            System.out.println(i+":"+value+" "+groupCode);
        }
    }

}
