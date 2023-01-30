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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.AUTHOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.PROPERTY_TYPE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.PROPERTY_TYPE_LABEL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.RELATION_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.VALID_FROM_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.VALID_UNTIL_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityHistoryGridColumnIDs.VALUE;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityHistoryCriteria;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class EntityHistoryProvider extends
        AbstractCommonTableModelProvider<EntityHistory>
{
    private final ListEntityHistoryCriteria criteria;

    public EntityHistoryProvider(ICommonServer commonServer, String sessionToken,
            ListEntityHistoryCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    @Override
    protected TypedTableModel<EntityHistory> createTableModel()
    {
        List<EntityHistory> history =
                commonServer.listEntityHistory(sessionToken, criteria.getEntityKind(),
                        criteria.getEntityID());
        Collections.sort(history, new Comparator<EntityHistory>()
            {
                @Override
                public int compare(EntityHistory e1, EntityHistory e2)
                {
                    long d1 = e1.getValidUntilDate().getTime();
                    long d2 = e2.getValidUntilDate().getTime();
                    return d1 < d2 ? 1 : (d1 > d2 ? -1 : 0);
                }
            });
        TypedTableModelBuilder<EntityHistory> builder =
                new TypedTableModelBuilder<EntityHistory>();
        builder.addColumn(PROPERTY_TYPE_CODE).hideByDefault();
        builder.addColumn(PROPERTY_TYPE_LABEL);
        builder.addColumn(RELATION_TYPE);
        builder.addColumn(VALUE).linkEntitiesOnly();
        builder.addColumn(AUTHOR);
        builder.addColumn(VALID_FROM_DATE);
        builder.addColumn(VALID_UNTIL_DATE);
        for (EntityHistory entry : history)
        {
            builder.addRow(entry);
            if (entry.getPropertyType() != null)
            {
                builder.column(PROPERTY_TYPE_CODE).addString(entry.getPropertyType().getCode());
                builder.column(PROPERTY_TYPE_LABEL).addString(entry.getPropertyType().getLabel());
            }
            builder.column(RELATION_TYPE).addString(entry.tryGetRelationType());
            String value = entry.getValue();
            if (value == null)
            {
                value = entry.getMaterial();
                if (value == null)
                {
                    value = entry.getVocabularyTerm();
                }
            }

            if (value == null && entry.tryGetRelatedProject() != null)
            {
                value = entry.tryGetRelatedProject().getIdentifier();
            }

            if (value == null && entry.tryGetRelatedEntity() != null)
            {
                builder.column(VALUE).addEntityLink(
                        Collections.singleton(entry.tryGetRelatedEntity()));
            } else if (value == null)
            {
                builder.column(VALUE).addString(entry.tryGetRelatedEntityPermId());
            } else
            {
                builder.column(VALUE).addString(value);
            }
            builder.column(AUTHOR).addPerson(entry.getAuthor());
            builder.column(VALID_FROM_DATE).addDate(entry.getValidFromDate());
            builder.column(VALID_UNTIL_DATE).addDate(entry.getValidUntilDate());
        }
        return builder.getModel();
    }
}
