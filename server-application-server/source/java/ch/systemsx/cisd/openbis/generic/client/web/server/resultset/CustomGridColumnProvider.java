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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.CODE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of {@link GridCustomColumn} instances.
 *
 * @author Franz-Josef Elmer
 */
public class CustomGridColumnProvider extends AbstractExpressionProvider<GridCustomColumn>
{

    public CustomGridColumnProvider(ICommonServer commonServer, String sessionToken, String gridId)
    {
        super(commonServer, sessionToken, gridId);
    }

    @Override
    protected List<GridCustomColumn> listExpressions()
    {
        return commonServer.listGridCustomColumns(sessionToken, gridId);
    }

    @Override
    protected void addAdditionalColumn(TypedTableModelBuilder<GridCustomColumn> builder)
    {
        builder.addColumn(CODE);
    }

    @Override
    protected void addAdditionalColumnValue(TypedTableModelBuilder<GridCustomColumn> builder,
            GridCustomColumn expression)
    {
        builder.column(CODE).addString(expression.getCode());
    }

}
