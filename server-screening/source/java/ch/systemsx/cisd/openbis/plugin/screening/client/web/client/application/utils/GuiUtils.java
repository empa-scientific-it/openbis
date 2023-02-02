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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomasz Pylak
 */
public class GuiUtils
{
    public static void replaceLastItem(LayoutContainer container, Widget newLastWidget)
    {
        int lastItemIx = container.getItemCount() - 1;
        if (lastItemIx >= 0)
        {
            container.remove(container.getWidget(lastItemIx));
            container.insert(newLastWidget, lastItemIx);
        } else
        {
            container.insert(newLastWidget, 0);
        }
        container.layout();
    }

    public static Widget withLabel(Widget component, String label)
    {
        return withLabel(component, label, 0);
    }

    public static Component withLabel(Widget component, String label, int margin, int width)
    {
        LayoutContainer c = new LayoutContainer();
        c.setLayout(new TableLayout(2));
        TableData cellLayout = new TableData();
        cellLayout.setMargin(margin);
        Text labelWidget = new Text(label);

        if (width != -1)
        {
            labelWidget.setWidth(width);
        } else
        {
            labelWidget.setWidth(Math.max(label.length() * 9, 80));
        }

        c.add(labelWidget, cellLayout);
        c.add(component);
        return c;
    }

    public static Component withLabel(Widget component, String label, int margin)
    {
        return withLabel(component, label, margin, -1);
    }

    public static Component renderInRow(Widget... widgets)
    {
        LayoutContainer c = new LayoutContainer();
        c.setLayout(new TableLayout(widgets.length * 2 - 1));
        TableData cellLayout = new TableData();
        cellLayout.setMargin(5);

        for (Widget widget : widgets)
        {
            if (c.getItems().size() > 0)
            {
                Html separator = new Html();
                separator.setWidth(10);
                c.add(separator);
            }
            c.add(widget, cellLayout);
        }
        return c;
    }

    public static Rectangle calculateBounds(Element element)
    {
        Rectangle rectangle = null;
        if (element != null)
        {
            El el = new El(element);
            rectangle = el.getBounds(false, false);
            for (int i = 0, n = element.getChildCount(); i < n; i++)
            {
                rectangle = merge(rectangle, calculateBounds(el.getChildElement(i)));
            }
        }
        return rectangle;
    }

    private static Rectangle merge(Rectangle r1OrNull, Rectangle r2OrNull)
    {
        if (r1OrNull == null)
        {
            return r2OrNull == null ? null : r2OrNull;
        }
        if (r2OrNull == null)
        {
            return r1OrNull;
        }
        int minX1 = r1OrNull.x;
        int minY1 = r1OrNull.y;
        int maxX1 = minX1 + r1OrNull.width;
        int maxY1 = minY1 + r1OrNull.height;
        int minX2 = r2OrNull.x;
        int minY2 = r2OrNull.y;
        int maxX2 = minX2 + r2OrNull.width;
        int maxY2 = minY2 + r2OrNull.height;
        int minX = Math.min(minX1, minX2);
        int maxX = Math.max(maxX1, maxX2);
        int minY = Math.min(minY1, minY2);
        int maxY = Math.max(maxY1, maxY2);
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
}
