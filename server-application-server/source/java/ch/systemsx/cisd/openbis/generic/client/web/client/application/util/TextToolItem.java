/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * TODO 2010-01-03, IA: temporary solution to fix compiler errors - to be removed
 * 
 * @author Izabela Adamczyk
 */
public class TextToolItem extends Button
{

    public TextToolItem(String message, SelectionListener<ButtonEvent> selectionListener)
    {
        super(message);
        addSelectionListener(selectionListener);
    }

    public TextToolItem(String message)
    {
        super(message);
    }

}
