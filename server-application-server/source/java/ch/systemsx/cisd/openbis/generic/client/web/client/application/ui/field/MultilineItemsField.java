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

import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * A {@link MultilineVarcharField} extension with support of handling list of items (Strings).
 * 
 * @author Piotr Buczek
 */
public class MultilineItemsField extends MultilineVarcharField
{

    private DivElement itemCounter;

    private Timer itemCounterRefreshTimer;

    /** Constructor for default sized field (5 lines). */
    public MultilineItemsField(final String label, final boolean mandatory)
    {
        super(label, mandatory);
        initItemCounter();
    }

    /** Constructor for multiline field with given number of lines. */
    public MultilineItemsField(final String label, final boolean mandatory, int lines)
    {
        super(label, mandatory, lines);
        initItemCounter();
    }

    /**
     * @return null if the area has not been modified, the list of all items (separated by comma or a new line) otherwise
     */
    public final String[] tryGetModifiedItemList()
    {
        if (isDirty() == false)
        {
            return null;
        }
        return getItems();
    }

    public final String[] getItems()
    {
        String text = getValue();
        if (StringUtils.isBlank(text) == false)
        {
            return text.split(GenericConstants.ITEMS_TEXTAREA_REGEX);
        } else
        {
            return new String[0];
        }
    }

    public final void setItems(List<String> items)
    {
        String textValue = createTextValue(items);
        setValue(textValue);
        setOriginalValue(textValue);
        refreshItemCounter();
    }

    public final void appendItem(String item)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getValue() == null ? "" : getValue());
        appendItem(sb, item);
        setValue(sb.toString());
        refreshItemCounter();
    }

    private void initItemCounter()
    {
        itemCounter = Document.get().createDivElement();
        itemCounter.setClassName("textarea-item-counter");

        addListener(Events.Change, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    scheduleItemCounterRefresh();
                }
            });

        addListener(Events.KeyUp, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    scheduleItemCounterRefresh();
                }
            });

        refreshItemCounter();
    }

    private void scheduleItemCounterRefresh()
    {
        if (itemCounterRefreshTimer == null)
        {
            itemCounterRefreshTimer = new Timer()
                {
                    @Override
                    public void run()
                    {
                        refreshItemCounter();
                        itemCounterRefreshTimer = null;
                    }
                };
            itemCounterRefreshTimer.schedule(300);
        }
    }

    private void refreshItemCounter()
    {
        String[] items = getItems();
        int count = 0;

        for (String item : items)
        {
            if (!StringUtils.isBlank(item))
            {
                count++;
            }
        }

        itemCounter.setInnerText("(" + count + ")");
    }

    private static String createTextValue(List<String> items)
    {
        StringBuilder sb = new StringBuilder();
        for (String item : items)
        {
            appendItem(sb, item);
        }
        return sb.toString();
    }

    private static final void appendItem(StringBuilder sb, String item)
    {
        if (sb.length() > 0)
        {
            sb.append(GenericConstants.ITEMS_TEXTAREA_DEFAULT_SEPARATOR);
        }
        sb.append(item);
    }

    @Override
    protected void onRender(Element target, int index)
    {
        if (el() == null)
        {
            Document doc = Document.get();
            DivElement wrapper = doc.createDivElement();
            TableElement table = doc.createTableElement();
            table.setCellPadding(0);
            table.setCellSpacing(0);
            TableRowElement row = doc.createTRElement();
            TextAreaElement textArea = doc.createTextAreaElement();

            TableCellElement textAreaCell = doc.createTDElement();
            textAreaCell.appendChild(textArea);
            TableCellElement itemCounterCell = doc.createTDElement();
            itemCounterCell.appendChild(itemCounter);

            row.appendChild(textAreaCell);
            row.appendChild(itemCounterCell);
            table.appendChild(row);
            wrapper.appendChild(table);

            setElement((Element) wrapper.cast(), target, index);
            input = el().firstChild().firstChild().firstChild().firstChild();
        }

        super.onRender(target, index);
    }

}
