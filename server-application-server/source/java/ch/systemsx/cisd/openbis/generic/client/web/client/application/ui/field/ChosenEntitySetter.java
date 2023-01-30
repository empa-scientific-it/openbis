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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.form.TriggerField;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

public abstract class ChosenEntitySetter<T> extends TriggerField<String> implements
        IChosenEntitiesSetter<T>
{
    private static final int TEXT_CHOOSER_FIELD_WIDTH = 342;

    private final Set<IChosenEntitiesListener<T>> listeners =
            new LinkedHashSet<IChosenEntitiesListener<T>>();

    protected ChosenEntitySetter()
    {
        setWidth(TEXT_CHOOSER_FIELD_WIDTH);
        setTriggerStyle("x-form-trigger-generate");
        setHideTrigger(false);
        setFireChangeEventOnSetValue(true);
    }

    public void addChosenEntityListener(IChosenEntitiesListener<T> listener)
    {
        listeners.add(listener);
    }

    @Override
    public void setChosenEntities(List<T> entityOrNull)
    {
        if (entityOrNull != null)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (T entity : entityOrNull)
            {
                builder.append(renderEntity(entity));
            }
            setValue(builder.toString());
        }
        for (IChosenEntitiesListener<T> listener : listeners)
        {
            listener.entitiesChosen(entityOrNull);
        }
    }

    abstract String renderEntity(T entity);
}
