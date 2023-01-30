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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.event.dom.client.KeyCodes;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;

/**
 * A {@link TextArea} extension for registering multiline text with adjustable height.
 * 
 * @author Piotr Buczek
 */
public class MultilineVarcharField extends TextArea
{
    private static final double DEFAULT_LINE_HEIGHT = 1.6; // in em

    public static final int EM_TO_PIXEL = 10;

    protected static final int DEFAULT_LINES = 5;

    /** Constructor for default sized field (5 lines). */
    public MultilineVarcharField(final String label, final boolean mandatory)
    {
        this(label, mandatory, DEFAULT_LINES);
    }

    /** Constructor for multiline field with given number of lines. */
    public MultilineVarcharField(final String label, final boolean mandatory, int lines)
    {
        this.setFieldLabel(label);
        this.setValidateOnBlur(true);
        this.setAutoValidate(true);
        this.setFireChangeEventOnSetValue(true);
        FieldUtil.setMandatoryFlag(this, mandatory);
        setHeightInLines(lines);
    }

    private void setHeightInLines(int lines)
    {
        // WORKAROUND: GXT does not correctly interpret heights set in em's. Switch to pixels.
        // setHeight(lines * DEFAULT_LINE_HEIGHT + "em");
        setHeight(linesToPixelHeight(lines));
    }

    public static int linesToPixelHeight(int lines)
    {
        return (int) (lines * DEFAULT_LINE_HEIGHT * EM_TO_PIXEL);
    }

    /**
     * By default tab key will cause change of focus to next field. Call this method to change the behaviour to act like a text editor where tab key
     * appends a tab.
     */
    public void treatTabKeyAsInput()
    {
        addListener(Events.OnKeyDown, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    if (be.getKeyCode() == KeyCodes.KEY_TAB)
                    {
                        be.preventDefault();
                        String t = getValue();
                        int cp = getCursorPos();
                        String start = t.substring(0, cp);
                        String end = t.substring(cp, t.length());
                        setValue(start + ((char) KeyCodes.KEY_TAB) + end);
                        setCursorPos(cp + 1);
                    }
                }
            });
    }

}
