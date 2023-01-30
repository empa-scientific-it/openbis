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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypeGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypeGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypeGridColumnIDs.VALIDATION_SCRIPT;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Abstract super class of providers of {@link EntityType} instances.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class EntityTypeProvider<T extends EntityType> extends
        AbstractCommonTableModelProvider<T>
{
    public EntityTypeProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<T> createTableModel()
    {
        List<T> types = listTypes();
        TypedTableModelBuilder<T> builder = new TypedTableModelBuilder<T>();
        builder.addColumn(CODE);
        builder.addColumn(DESCRIPTION).withDefaultWidth(300);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(VALIDATION_SCRIPT).hideByDefault();
        addMoreColumns(builder);
        for (T type : types)
        {
            builder.addRow(type);
            builder.column(CODE).addString(type.getCode());
            builder.column(DESCRIPTION).addString(type.getDescription());
            builder.column(MODIFICATION_DATE).addDate(type.getModificationDate());
            builder.column(VALIDATION_SCRIPT).addString(
                    type.getValidationScript() != null ? type.getValidationScript().getName() : "");
            addMoreCells(builder, type);
        }
        return builder.getModel();
    }

    protected abstract List<T> listTypes();

    protected void addMoreColumns(TypedTableModelBuilder<T> builder)
    {
    }

    protected void addMoreCells(TypedTableModelBuilder<T> builder, T type)
    {
    }

}
