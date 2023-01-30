/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.DOM;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Info handler which shows info in a popup dialog.
 * 
 * @author Franz-Josef Elmer
 */
public class PopupDialogBasedInfoHandler extends Dialog implements IInfoHandler
{

    private static final int WIDTH = 550;

    private static final int HEIGHT = 400;

    private IMessageProvider messageProvider;

    private InfoType type;

    private String text;

    private Label label;

    private TextArea textArea;

    public PopupDialogBasedInfoHandler(IMessageProvider messageProvider)
    {
        this.messageProvider = messageProvider;

        textArea = new TextArea();
        textArea.setReadOnly(true);

        label = new Label();
        label.setVisible(false);

        setBodyStyle("padding: 10px");
        setSize(WIDTH, HEIGHT);
        setScrollMode(Scroll.NONE);
        setButtons(Dialog.CLOSE);
        setHideOnButtonClick(true);
        setModal(true);
        setLayout(new FitLayout());
        add(textArea);
        add(label);
    }

    @Override
    public void displayInfo(String aText)
    {
        display(InfoType.INFO, aText);
    }

    @Override
    public void displayError(String aText)
    {
        display(InfoType.ERROR, aText);
    }

    @Override
    public void displayProgress(String aText)
    {
        display(InfoType.PROGRESS, aText);
    }

    public void display(InfoType aType, String aText)
    {
        this.type = aType;
        this.text = aText;
        show();
    }

    @Override
    protected void onAttach()
    {
        super.onAttach();

        setHeading(messageProvider.getMessage(type.getMessageKey()));

        // The requested text may contain some HTML tags, e.g. "<b>some text</b><br>more text".
        // Because a text area does not interpret HTML code and displays it as a plain text we
        // have to get rid of all the tags. As we want to keep the line breaks untouched we replace
        // all <br> tags with new line characters. We do all this in the onAttach method because
        // the trick with setInnerHTML and getInnerText requires the label element to be already
        // rendered.

        String textWithNewLines = text.replace("<br>", "\n");
        label.setText(textWithNewLines);
        String textWithoutTags = DOM.getInnerText(label.getElement());
        textArea.setValue(textWithoutTags);

        layout();
    }

    @Override
    public void displayInfo(List<? extends IMessageElement> elements)
    {
        StringBuilder html = new StringBuilder();
        for (IMessageElement element : elements)
        {
            html.append(element.toString());
        }
        displayInfo(html.toString());
    }

}
