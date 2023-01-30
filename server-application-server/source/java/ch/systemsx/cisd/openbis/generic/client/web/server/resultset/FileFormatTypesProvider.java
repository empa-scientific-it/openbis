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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.FileFormatTypeGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.FileFormatTypeGridColumnIDs.DESCRIPTION;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Kaloyan Enimanev
 */
public class FileFormatTypesProvider extends AbstractCommonTableModelProvider<FileFormatType>
{

    public FileFormatTypesProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<FileFormatType> createTableModel()
    {
        List<FileFormatType> fileFormats = commonServer.listFileFormatTypes(sessionToken);

        TypedTableModelBuilder<FileFormatType> builder =
                new TypedTableModelBuilder<FileFormatType>();
        builder.addColumn(CODE).withDefaultWidth(150);
        builder.addColumn(DESCRIPTION).withDefaultWidth(300);

        for (FileFormatType fileFormat : fileFormats)
        {
            builder.addRow(fileFormat);
            builder.column(CODE).addString(fileFormat.getCode());
            builder.column(DESCRIPTION).addString(fileFormat.getDescription());
        }
        return builder.getModel();
    }

}
