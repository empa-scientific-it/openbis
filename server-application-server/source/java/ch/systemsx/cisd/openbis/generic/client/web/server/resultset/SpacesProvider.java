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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SpaceGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SpaceGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SpaceGridColumnIDs.REGISTRATOR;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class SpacesProvider extends AbstractCommonTableModelProvider<Space>
{
    public SpacesProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<Space> createTableModel()
    {
        List<Space> spaces = commonServer.listSpaces(sessionToken);
        TypedTableModelBuilder<Space> builder = new TypedTableModelBuilder<Space>();
        builder.addColumn(CODE);
        builder.addColumn(DESCRIPTION).withDefaultWidth(200);
        builder.addColumn(REGISTRATOR).withDefaultWidth(200);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        for (Space space : spaces)
        {
            builder.addRow(space);
            builder.column(CODE).addString(space.getCode());
            builder.column(DESCRIPTION).addString(space.getDescription());
            builder.column(REGISTRATOR).addPerson(space.getRegistrator());
            builder.column(MODIFICATION_DATE).addDate(space.getRegistrationDate());
        }
        return builder.getModel();
    }

}
