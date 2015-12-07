/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.IFileFormatTypeId;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.dataset.ListFileFormatTypeByPermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * @author pkupczyk
 */
@Component
public class MapFileFormatTypeByIdExecutor extends AbstractMapObjectByIdExecutor<IFileFormatTypeId, FileFormatTypePE> implements
        IMapFileFormatTypeByIdExecutor
{

    private IFileFormatTypeDAO typeDAO;

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends IFileFormatTypeId, FileFormatTypePE>> listers)
    {
        listers.add(new ListFileFormatTypeByPermId(typeDAO));
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        typeDAO = daoFactory.getFileFormatTypeDAO();
    }

}
