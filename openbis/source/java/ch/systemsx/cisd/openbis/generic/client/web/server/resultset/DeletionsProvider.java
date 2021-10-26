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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DeletionGridColumnIDs.DELETER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DeletionGridColumnIDs.DELETION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DeletionGridColumnIDs.ENTITIES;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DeletionGridColumnIDs.REASON;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Table model provider of {@link Deletion} instances.
 * 
 * @author Piotr Buczek
 */
public class DeletionsProvider extends AbstractCommonTableModelProvider<Deletion>
{
    public DeletionsProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<Deletion> createTableModel()
    {
        List<Deletion> deletions = commonServer.listOriginalDeletions(sessionToken);
        TypedTableModelBuilder<Deletion> builder = new TypedTableModelBuilder<Deletion>();
        builder.addColumn(DELETION_DATE).withDefaultWidth(300);
        builder.addColumn(DELETER).withDefaultWidth(200);
        builder.addColumn(ENTITIES).withDefaultWidth(300);
        builder.addColumn(REASON).withDefaultWidth(500);
        for (Deletion deletion : deletions)
        {
            builder.addRow(deletion);
            builder.column(DELETION_DATE).addDate(deletion.getRegistrationDate());
            builder.column(DELETER).addPerson(deletion.getRegistrator());
            builder.column(REASON).addString(deletion.getReason());
            if (deletion.getDeletedEntities().isEmpty() == false)
            {
                builder.column(ENTITIES).addString(DeletionUtils.createDescriptionOfDeletedEntities(deletion));
            }
        }
        return builder.getModel();
    }

}
