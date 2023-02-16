/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * A {@link KeyListener} implementation which does something when enter key is entered.
 * 
 * @author Christian Ribeaud
 */
public abstract class EnterKeyListener extends KeyListener
{
    protected abstract void onEnterKey();

    //
    // KeyListener
    //

    @Override
    public final void componentKeyUp(final ComponentEvent event)
    {
        if (event.getKeyCode() == KeyCodes.KEY_ENTER)
        {
            onEnterKey();
            event.stopEvent();
            event.setCancelled(true);
        }
    }
}
