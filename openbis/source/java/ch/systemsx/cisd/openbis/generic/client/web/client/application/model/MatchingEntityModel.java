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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;

/**
 * A {@link ModelData} implementation for {@link MatchingEntity}.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntityModel extends AbstractEntityModel<MatchingEntity>
{
    private static final long serialVersionUID = 1L;

    public MatchingEntityModel(final MatchingEntity entity)
    {
        super(entity, createColumnsSchema(null));

        // override registrator column adding a link
        set(MatchingEntityColumnKind.REGISTRATOR.id(), PersonRenderer.createPersonAnchor(entity
                .getRegistrator()));
    }

    public static List<IColumnDefinitionUI<MatchingEntity>> createColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsSchemaFrom(MatchingEntityColumnKind.values(), msgProviderOrNull);
    }

    public final static List<MatchingEntityModel> convert(
            final List<MatchingEntity> matchingEntities)
    {
        final List<MatchingEntityModel> list = new ArrayList<MatchingEntityModel>();
        for (final MatchingEntity matchingEntity : matchingEntities)
        {
            list.add(new MatchingEntityModel(matchingEntity));
        }
        return list;
    }

    public enum MatchingEntityColumnKind implements IColumnDefinitionKind<MatchingEntity>
    {
        ENTITY_KIND(new AbstractColumnDefinitionKind<MatchingEntity>(
                ModelDataPropertyNames.ENTITY_KIND, Dict.ENTITY_KIND)
            {
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getEntityKind().getDescription();
                }
            }),

        ENTITY_TYPE(new AbstractColumnDefinitionKind<MatchingEntity>(
                ModelDataPropertyNames.ENTITY_TYPE, Dict.ENTITY_TYPE)
            {
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getEntityType().getCode();
                }
            }),

        IDENTIFIER(new AbstractColumnDefinitionKind<MatchingEntity>(
                ModelDataPropertyNames.IDENTIFIER, Dict.IDENTIFIER, 140, false)
            {
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getIdentifier();
                }
            }),

        REGISTRATOR(new AbstractColumnDefinitionKind<MatchingEntity>(
                ModelDataPropertyNames.REGISTRATOR, Dict.REGISTRATOR)
            {
                public String tryGetValue(MatchingEntity entity)
                {
                    return renderRegistrator(entity.getRegistrator());
                }
            }),

        MATCHING_FIELD(new AbstractColumnDefinitionKind<MatchingEntity>(
                ModelDataPropertyNames.MATCHING_FIELD, Dict.MATCHING_FIELD, 140, false)
            {
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getFieldDescription();
                }
            }),

        MATCHING_TEXT(new AbstractColumnDefinitionKind<MatchingEntity>(
                ModelDataPropertyNames.MATCHING_TEXT, Dict.MATCHING_TEXT, 200, false)
            {
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getTextFragment();
                }
            });

        private final IColumnDefinitionKind<MatchingEntity> columnDefinitionKind;

        private MatchingEntityColumnKind(IColumnDefinitionKind<MatchingEntity> columnDefinitionKind)
        {
            this.columnDefinitionKind = columnDefinitionKind;
        }

        public String getHeaderMsgKey()
        {
            return columnDefinitionKind.getHeaderMsgKey();
        }

        public int getWidth()
        {
            return columnDefinitionKind.getWidth();
        }

        public String id()
        {
            return columnDefinitionKind.id();
        }

        public boolean isHidden()
        {
            return columnDefinitionKind.isHidden();
        }

        public String tryGetValue(MatchingEntity entity)
        {
            return columnDefinitionKind.tryGetValue(entity);
        }
    }

}
