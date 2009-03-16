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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SessionContextCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.LeftMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder.MenuCategoryKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder.MenuElementKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for choosing a roles menu element.
 * 
 * @author Izabela Adamczyk
 */
public final class OpenTab extends AbstractDefaultTestCommand
{
    private String category;

    private String option;

    // TODO 2009-03-16, Piotr Buczek: change to final and remove category/option
    @SuppressWarnings("unused")
    private String action;

    public OpenTab(final MenuCategoryKind category, final MenuElementKind option,
            final Class<? extends AsyncCallback<?>> callbackClass)
    {
        if (callbackClass == null)
        {
            addCallbackClass(SessionContextCallback.class);
        } else
        {
            addCallbackClass(callbackClass);
        }
        this.category = category.name();
        this.option = option.name();
    }

    public OpenTab(final MenuCategoryKind category, final MenuElementKind option)
    {
        this(category, option, null);
    }

    public OpenTab(final ActionMenuKind action,
            final Class<? extends AsyncCallback<?>> callbackClass)
    {
        if (callbackClass == null)
        {
            addCallbackClass(SessionContextCallback.class);
        } else
        {
            addCallbackClass(callbackClass);
        }
        this.action = action.name();
    }

    public OpenTab(final ActionMenuKind action)
    {
        this(action, null);
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        // TODO 2009-03-16, Piotr Buczek: change to use TopMenu
        // GWTTestUtil.selectTopMenuWithID(TopMenu.ID, action);
        GWTTestUtil.selectMenuCategoryWithID(LeftMenu.ID, category);
        GWTTestUtil.selectMenuWithID(LeftMenu.ID, category, option);
    }
}
