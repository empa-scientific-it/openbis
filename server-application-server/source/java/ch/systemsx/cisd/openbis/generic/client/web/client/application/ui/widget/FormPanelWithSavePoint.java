/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ContainerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;

/**
 * @author pkupczyk
 */

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class FormPanelWithSavePoint extends ClickableFormPanel
{

    private List<Listener<DirtyChangeEvent>> dirtyChangeListeners =
            new ArrayList<Listener<DirtyChangeEvent>>();

    private Set<Field<?>> dirtyCheckIgnoredFields = new HashSet<Field<?>>();

    private Map<String, Object> dirtyCheckSavePointValues = new HashMap<String, Object>();

    public FormPanelWithSavePoint()
    {
        addListener(Events.Add, new Listener<ContainerEvent>()
            {
                @Override
                public void handleEvent(ContainerEvent ce)
                {
                    List<Field<?>> fields = new ArrayList<Field<?>>();
                    getDirtyCheckFields(ce.getItem(), fields);

                    for (Field<?> field : fields)
                    {
                        field.addListener(Events.Change, new Listener<FieldEvent>()
                            {
                                @Override
                                public void handleEvent(FieldEvent fe)
                                {
                                    DirtyChangeEvent dirtyChangedEvent =
                                            new DirtyChangeEvent(FormPanelWithSavePoint.this);

                                    for (Listener<DirtyChangeEvent> dirtyChangeListener : dirtyChangeListeners)
                                    {
                                        dirtyChangeListener.handleEvent(dirtyChangedEvent);
                                    }
                                }
                            });
                    }
                }
            });
    }

    public void setSavePoint()
    {
        dirtyCheckSavePointValues.clear();

        for (Field field : getDirtyCheckFields())
        {
            setSavePointValue(field);
        }
    }

    @Override
    public void reset()
    {
        super.reset();
        setSavePoint();
    }

    public void resetToSavePoint()
    {
        for (Field field : getDirtyCheckFields())
        {
            field.setValue(getSavePointValue(field));
        }
    }

    private void setSavePointValue(Field<?> field)
    {
        dirtyCheckSavePointValues.put(field.getId(), field.getValue());
    }

    private Object getSavePointValue(Field<?> field)
    {
        if (dirtyCheckSavePointValues.containsKey(field.getId()))
        {
            return dirtyCheckSavePointValues.get(field.getId());
        } else
        {
            return field.getOriginalValue();
        }
    }

    private List<Field<?>> getDirtyCheckFields()
    {
        List<Field<?>> fields = new ArrayList<Field<?>>();
        getDirtyCheckFields(this, fields);
        return fields;
    }

    private void getDirtyCheckFields(Component component, List<Field<?>> fields)
    {
        if (component instanceof MultiField<?>)
        {
            MultiField<?> multifield = (MultiField<?>) component;
            for (Component item : multifield.getAll())
            {
                getDirtyCheckFields(item, fields);
            }
        } else if (component instanceof Field<?>)
        {
            fields.add((Field<?>) component);
        } else if (component instanceof Container<?>)
        {
            Container<Component> container = (Container<Component>) component;
            for (Component item : container.getItems())
            {
                getDirtyCheckFields(item, fields);
            }
        }
    }

    public void addDirtyCheckIgnoredField(Field<?> field)
    {
        dirtyCheckIgnoredFields.add(field);
    }

    public boolean isDirtyForSavePoint()
    {
        for (Field<?> field : getDirtyCheckFields())
        {
            if (dirtyCheckIgnoredFields.contains(field) == false && field.isVisible())
            {
                Object currentValue = field.getValue();
                Object savePointValue = getSavePointValue(field);

                if (currentValue instanceof String && Util.isEmptyString((String) currentValue))
                {
                    currentValue = null;
                }
                if (savePointValue instanceof String && Util.isEmptyString((String) savePointValue))
                {
                    savePointValue = null;
                }
                if (Util.equalWithNull(currentValue, savePointValue) == false)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void addDirtyChangeListener(Listener<DirtyChangeEvent> listener)
    {
        dirtyChangeListeners.add(listener);
    }

    public class DirtyChangeEvent extends BaseEvent
    {

        private boolean isDirtyForSavePoint;

        public DirtyChangeEvent(FormPanelWithSavePoint form)
        {
            super(form);
            this.isDirtyForSavePoint = form.isDirtyForSavePoint();
        }

        public boolean isDirtyForSavePoint()
        {
            return isDirtyForSavePoint;
        }

    }

}
