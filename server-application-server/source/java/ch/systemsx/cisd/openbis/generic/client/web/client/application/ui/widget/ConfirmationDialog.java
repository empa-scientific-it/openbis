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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Element;

/**
 * Dialog with YES and NO buttons.
 * 
 * @author Izabela Adamczyk
 */
public abstract class ConfirmationDialog extends Dialog
{

    public ConfirmationDialog(String heading, String message)
    {
        setHeading(heading);
        setButtons(Dialog.YESNO);
        addText(message);
        setHideOnButtonClick(true);
        setModal(true);
        setId("confirmation_dialog");
    }

    @Override
    protected void onButtonPressed(Button button)
    {
        super.onButtonPressed(button);
        if (button.getItemId().equals(Dialog.YES))
        {
            onYes();
        } else
        {
            onNo();
        }
    }

    @Override
    protected void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
    }

    abstract protected void onYes();

    protected void onNo()
    {
    }
}
