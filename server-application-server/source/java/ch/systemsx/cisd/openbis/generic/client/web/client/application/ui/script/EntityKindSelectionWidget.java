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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Izabela Adamczyk
 */
class EntityKindSelectionWidget extends SimpleComboBox<LabeledItem<EntityKind>>
{
    static LabeledItem<EntityKind> createLabeledItemForAll()
    {
        return new LabeledItem<EntityKind>(null, GenericConstants.ALL_ENTITY_KINDS);
    }
    
    static LabeledItem<EntityKind> createLabeledItem(EntityKind entityKind, IMessageProvider viewContext)
    {
        return new LabeledItem<EntityKind>(entityKind, EntityTypeUtils.translatedEntityKindForUI(viewContext, entityKind));
    }
    
    public EntityKindSelectionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKindOrNull, boolean enabled, boolean withAll)
    {
        if (withAll)
        {
            add(createLabeledItemForAll());
        }
        if (entityKindOrNull != null)
        {
            add(createLabeledItem(entityKindOrNull, viewContext));
        } else
        {
            for (EntityKind val : EntityKind.values())
            {
                add(createLabeledItem(val, viewContext));
            }
        }
        setFieldLabel(viewContext.getMessage(Dict.ENTITY_KIND));
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setEditable(false);
        setAllowBlank(false);
        FieldUtil.markAsMandatory(this);
        setEnabled(enabled);
    }

    public EntityKind tryGetEntityKind()
    {
        if (getSelectedIndex() == -1)
        {
            return null;
        }
        return getSimpleValue().getItem();
    }
}