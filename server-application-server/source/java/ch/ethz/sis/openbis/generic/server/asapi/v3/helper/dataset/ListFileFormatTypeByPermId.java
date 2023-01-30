/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.dataset;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * @author pkupczyk
 */
public class ListFileFormatTypeByPermId extends AbstractListObjectById<FileFormatTypePermId, FileFormatTypePE>
{

    private IFileFormatTypeDAO typeDAO;

    public ListFileFormatTypeByPermId(IFileFormatTypeDAO typeDAO)
    {
        this.typeDAO = typeDAO;
    }

    @Override
    public Class<FileFormatTypePermId> getIdClass()
    {
        return FileFormatTypePermId.class;
    }

    @Override
    public FileFormatTypePermId createId(FileFormatTypePE type)
    {
        return new FileFormatTypePermId(type.getCode());
    }

    @Override
    public List<FileFormatTypePE> listByIds(IOperationContext context, List<FileFormatTypePermId> ids)
    {
        List<FileFormatTypePE> types = new ArrayList<FileFormatTypePE>();

        for (FileFormatTypePermId id : ids)
        {
            FileFormatTypePE type = typeDAO.tryToFindFileFormatTypeByCode(id.getPermId());
            if (type != null)
            {
                types.add(type);
            }
        }
        return types;
    }

}
