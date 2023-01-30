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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetTypeGridColumnIDs.DELETION_DISALLOW;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetTypeGridColumnIDs.MAIN_DATA_SET_PATH;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetTypeGridColumnIDs.MAIN_DATA_SET_PATTERN;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of {@link DataSetType} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetTypeProvider extends EntityTypeProvider<DataSetType>
{
    public DataSetTypeProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected List<DataSetType> listTypes()
    {
        return commonServer.listDataSetTypes(sessionToken);
    }

    @Override
    protected void addMoreColumns(TypedTableModelBuilder<DataSetType> builder)
    {
        builder.addColumn(DELETION_DISALLOW).hideByDefault();
        builder.addColumn(MAIN_DATA_SET_PATH).hideByDefault();
        builder.addColumn(MAIN_DATA_SET_PATTERN).hideByDefault();
    }

    @Override
    protected void addMoreCells(TypedTableModelBuilder<DataSetType> builder, DataSetType type)
    {
        builder.column(DELETION_DISALLOW).addString(
                SimpleYesNoRenderer.render(type.isDeletionDisallow()));
        builder.column(MAIN_DATA_SET_PATH).addString(type.getMainDataSetPath());
        builder.column(MAIN_DATA_SET_PATTERN).addString(type.getMainDataSetPattern());
    }

}
