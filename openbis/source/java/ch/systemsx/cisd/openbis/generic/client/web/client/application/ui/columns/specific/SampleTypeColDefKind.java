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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Columns definition for browsing grid of {@link SampleType}s.
 * 
 * @author Piotr Buczek
 */
public enum SampleTypeColDefKind implements IColumnDefinitionKind<EntityType>
{

    // Copy & Paste from EntityTypeColDefKind (cannot extend an enum)

    CODE(new AbstractColumnDefinitionKind<SampleType>(Dict.CODE)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<SampleType>(Dict.DESCRIPTION, 300)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return entity.getDescription();
            }
        }),

    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<SampleType>(Dict.DATABASE_INSTANCE, true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return entity.getDatabaseInstance().getCode();
            }
        }),

    // specific Sample Type columns

    IS_LISTABLE(new AbstractColumnDefinitionKind<SampleType>(Dict.IS_LISTABLE, true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return SimpleYesNoRenderer.render(entity.isListable());
            }
        }),

    GENERATED_FROM_HIERARCHY_DEPTH(new AbstractColumnDefinitionKind<SampleType>(
            Dict.GENERATED_FROM_HIERARCHY_DEPTH, 200, true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return Integer.toString(entity.getGeneratedFromHierarchyDepth());
            }
        }),

    PART_OF_HIERARCHY_DEPTH(new AbstractColumnDefinitionKind<SampleType>(
            Dict.PART_OF_HIERARCHY_DEPTH, 200, true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return Integer.toString(entity.getPartOfHierarchyDepth());
            }
        });

    private final AbstractColumnDefinitionKind<SampleType> columnDefinitionKind;

    private SampleTypeColDefKind(AbstractColumnDefinitionKind<SampleType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<EntityType> getDescriptor()
    {
        // cannot cast: (AbstractColumnDefinitionKind<EntityType>) columnDefinitionKind;
        return new AbstractColumnDefinitionKind<EntityType>(columnDefinitionKind.getHeaderMsgKey(),
                columnDefinitionKind.getWidth(), columnDefinitionKind.isHidden())
            {
                @Override
                public String tryGetValue(EntityType entity)
                {
                    return columnDefinitionKind.tryGetValue((SampleType) entity);
                }
            };
    }
}
