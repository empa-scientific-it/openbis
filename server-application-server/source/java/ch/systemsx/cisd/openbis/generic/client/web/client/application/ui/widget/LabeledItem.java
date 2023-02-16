/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

/**
 * Item with label which can be conveniently used in {@link SimpleComboBox} or {@link CheckBoxGroupWithModel}.
 * 
 * @author Tomasz Pylak
 */
public class LabeledItem<T>
{
    public static final String LABEL_FIELD = "label";

    public static final String TOOLTIP_FIELD = "tooltip";

    private final T item;

    private final String label;

    private final String tooltip;

    public LabeledItem(T item, String label)
    {
        this(item, label, label);
    }

    public LabeledItem(T item, String label, String tooltip)
    {
        this.item = item;
        this.label = label;
        this.tooltip = tooltip;
    }

    public T getItem()
    {
        return item;
    }

    public String getLabel()
    {
        return label;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    @Override
    public String toString()
    {
        return label;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        LabeledItem<T> other = (LabeledItem<T>) obj;
        if (item == null)
        {
            if (other.item != null)
                return false;
        } else if (!item.equals(other.item))
            return false;
        return true;
    }

}