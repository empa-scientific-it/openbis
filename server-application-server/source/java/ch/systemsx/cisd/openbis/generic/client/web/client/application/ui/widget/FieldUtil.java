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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;

/**
 * Utilities for {@link Field} class.
 * 
 * @author Tomasz Pylak
 */
public class FieldUtil
{

    private static final int[] INFO_LINK_FIELD_OFFSETS = new int[]
    { 20, 3 };

    private static final String INFO_LINK_POSITION = "tl-tr";

    private static final String MANDATORY_LABEL_SEPARATOR = ":&nbsp;*";

    public static void setMandatoryFlag(Field<?> field, boolean isMandatory)
    {
        if (isMandatory)
        {
            markAsMandatory(field);
        }
    }

    public static void setMandatoryFlag(TextField<?> field, boolean isMandatory)
    {
        if (isMandatory)
        {
            markAsMandatory(field);
        } else
        {
            field.setAllowBlank(true);
        }
    }

    public static void markAsMandatory(TextField<?> field)
    {
        markAsMandatory((Field<?>) field);
        field.setAllowBlank(false);
    }

    public static void markAsMandatory(Field<?> field)
    {
        field.setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
    }

    /** makes all given fields visible or invisible including validation dependent on given value */
    public static void setVisibility(boolean visible, Field<?>... fields)
    {
        for (Field<?> field : fields)
        {
            setVisibility(field, visible);
        }
    }

    /** makes field visible or invisible including validation dependent on given value */
    private static void setVisibility(Field<?> field, boolean visible)
    {
        field.setEnabled(visible);
        field.setVisible(visible);
        field.syncSize();
        if (visible == false)
        {
            // deletion mark is not removed automatically when we make field invisible
            field.clearInvalid();
        } else if (field.isDirty())
        {
            // validate only if something have been modified and field is shown
            field.validate();
        }
    }

    /** sets field value without invoking any events (especially Events.Change event) */
    public static <T> void setValueWithoutEvents(Field<T> field, T value)
    {
        field.enableEvents(false);
        field.setValue(value);
        field.enableEvents(true);
    }

    public static void setValueWithUnescaping(Field<String> field, String value)
    {
        field.setValue(StringEscapeUtils.unescapeHtml(value));
    }

    public static void setValueWithUnescaping(LabelField field, String value)
    {
        field.setValue(StringEscapeUtils.unescapeHtml(value));
    }

    //
    // info icon support
    // WORKAROUND: Based on GXT functionality for displaying error icon, until there is no
    // dedicated mechanism for showing info icons in GXT
    //

    /**
     * Adds '?' to the field and allows to display chosen message in the info box.
     * 
     * @param image
     */
    public static void addInfoIcon(final Field<?> field, final String message, final Image image)
    {
        addInfoIcon(field, message, image, INFO_LINK_FIELD_OFFSETS);
    }

    public static void addInfoIcon(final Field<?> field, final String message, final Image image,
            int xOffset, int yOffset)
    {
        addInfoIcon(field, message, image, new int[] { xOffset, yOffset });
    }

    private static void addInfoIcon(final Field<?> field, final String message, final Image image,
            final int[] infoLinkFieldOffsets)
    {
        final WidgetComponent infoIcon = createInfoIcon(message, image);

        final IDelegatedAction alignInfoIcon = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    infoIcon.el().alignTo(field.getElement(), INFO_LINK_POSITION,
                            infoLinkFieldOffsets);
                }
            };

        field.addListener(Events.Render, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    Element parent = field.el().getParent().dom;
                    infoIcon.render(parent);
                    infoIcon.setStyleAttribute("display", "block");
                    infoIcon.el().makePositionable(true);
                    if (!infoIcon.isAttached())
                    {
                        ComponentHelper.doAttach(infoIcon);
                    }
                    GWTUtils.executeDelayed(alignInfoIcon);
                    if (GXT.isIE || GXT.isOpera)
                    {
                        GWTUtils.executeDelayed(alignInfoIcon);
                    }
                    field.el().repaint();
                }
            });
        field.addListener(Events.Show, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    if (field.isRendered())
                    {
                        GWTUtils.executeDelayed(alignInfoIcon);
                        if (GXT.isIE || GXT.isOpera)
                        {
                            GWTUtils.executeDelayed(alignInfoIcon);
                        }
                    }
                }
            });
    }

    private static WidgetComponent createInfoIcon(final String message, final Image image)
    {
        final WidgetComponent info = new WidgetComponent(image);
        info.setStyleName("hands");
        info.setStyleAttribute("cursor", "hand");
        GWTUtils.setToolTip(info, message);
        info.sinkEvents(Events.OnClick.getEventCode());
        info.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent e)
                {
                    MessageBox.info("Info", message, null);
                }
            });
        return info;
    }

}
