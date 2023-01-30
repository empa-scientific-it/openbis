/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;

/**
 * Check-box group where each field has a value behind it.
 * 
 * @author Tomasz Pylak
 */
public class CheckBoxGroupWithModel<T> extends CheckBoxGroup
{
    public static interface CheckBoxGroupListner<T>
    {
        void onChange(Set<T> selected);
    }

    private final static class CheckBoxWithModel<T> extends CheckBox
    {
        private final T item;

        public CheckBoxWithModel(LabeledItem<T> item, Boolean isSelected)
        {
            this.item = item.getItem();
            setBoxLabel(item.toString());
            setValue(isSelected);
        }

        public T getItem()
        {
            return item;
        }
    }

    private final Set<T> selected;

    private final Set<CheckBoxGroupListner<T>> listeners;

    /**
     * Create a group of check-boxes, one for each item. Use {@link #getSelected()} to fetch items for which the check-boxes are selected.
     */
    public CheckBoxGroupWithModel(List<LabeledItem<T>> items)
    {
        this.selected = new HashSet<T>();
        this.listeners = new HashSet<CheckBoxGroupListner<T>>();

        for (LabeledItem<T> item : items)
        {
            addCheckBox(item);
        }
    }

    public void addCheckBox(LabeledItem<T> item)
    {
        final CheckBoxWithModel<T> checkBox = new CheckBoxWithModel<T>(item, false);
        checkBox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    T changedItem = checkBox.getItem();
                    if (checkBox.getValue())
                    {
                        selected.add(changedItem);
                    } else
                    {
                        selected.remove(changedItem);
                    }
                    notifyListeners();
                }
            });
        add(checkBox);
    }

    private void notifyListeners()
    {
        for (CheckBoxGroupListner<T> listener : listeners)
        {
            listener.onChange(selected);
        }
    }

    public void addListener(CheckBoxGroupListner<T> listener)
    {
        listeners.add(listener);
    }

    public Set<T> getSelected()
    {
        return selected;
    }
}
