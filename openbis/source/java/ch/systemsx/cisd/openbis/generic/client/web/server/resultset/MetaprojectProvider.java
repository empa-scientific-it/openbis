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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs.CREATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs.NAME;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author pkupczyk
 */
public class MetaprojectProvider extends AbstractCommonTableModelProvider<Metaproject>
{

    public MetaprojectProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<Metaproject> createTableModel()
    {
        List<Metaproject> metaprojects = commonServer.listMetaprojects(sessionToken);
        TypedTableModelBuilder<Metaproject> builder = new TypedTableModelBuilder<Metaproject>();
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(CREATION_DATE).withDefaultWidth(300);
        for (Metaproject metaproject : metaprojects)
        {
            builder.addRow(metaproject);
            builder.column(NAME).addString(metaproject.getName());
            builder.column(DESCRIPTION).addString(metaproject.getDescription());
            builder.column(CREATION_DATE).addDate(metaproject.getCreationDate());
        }
        return builder.getModel();
    }

}
