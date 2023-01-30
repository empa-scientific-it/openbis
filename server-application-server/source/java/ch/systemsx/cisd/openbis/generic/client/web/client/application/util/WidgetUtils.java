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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility methods for widgets.
 * 
 * @author Izabela Adamczyk
 */
public class WidgetUtils
{
    public static RadioGroup createAllOrSelectedRadioGroup(Radio selected, Radio all, String label,
            long selectedSize, final IDelegatedAction onChangeActionOrNull)
    {
        final RadioGroup result = new RadioGroup();
        result.setFieldLabel(label);
        result.setSelectionRequired(true);
        result.setOrientation(Orientation.HORIZONTAL);
        if (selectedSize > 0)
        {
            result.add(selected);
        }
        result.add(all);
        result.setValue(selectedSize > 0 ? selected : all);
        result.setAutoHeight(true);
        if (onChangeActionOrNull != null)
        {
            result.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        onChangeActionOrNull.execute();
                    }
                });
        }
        return result;
    }

    public static RadioGroup createAllOrSelectedRadioGroup(Radio selected, Radio all, String label,
            long selectedSize)
    {
        return createAllOrSelectedRadioGroup(selected, all, label, selectedSize, null);
    }

    public static final Radio createRadio(final String label)
    {
        Radio result = new Radio();
        result.setBoxLabel(label);
        return result;
    }

    public static boolean isSelected(Radio radioOrNull)
    {
        if (radioOrNull == null)
        {
            return false;
        } else
        {
            return radioOrNull.getValue();
        }
    }

    /**
     * Returns true and stops event propagation and default behavior if the special key is pressed.
     */
    public static final boolean ifSpecialKeyPressed(NativeEvent e)
    {
        // Note: using getMetaKey() would allow using Apple Key (Windows Key), but then
        // preventDefault() might not work in all browsers
        boolean result = e.getAltKey();
        if (result)
        {
            e.stopPropagation();
            e.preventDefault();
        }
        return result;
    }

    public static LayoutContainer inRow(Widget... containers)
    {
        return asTable(containers.length, containers);
    }

    public static LayoutContainer asTable(int columns, Widget... containers)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(columns));
        container.setBorders(false);
        container.setScrollMode(Scroll.AUTO);
        for (Widget w : containers)
        {
            container.add(w, new TableData(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        }
        return container;
    }

}
