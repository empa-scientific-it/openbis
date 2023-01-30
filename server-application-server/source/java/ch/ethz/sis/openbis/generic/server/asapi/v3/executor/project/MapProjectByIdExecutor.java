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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.project.ListProjectByIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.project.ListProjectByPermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class MapProjectByIdExecutor extends AbstractMapObjectByIdExecutor<IProjectId, ProjectPE> implements IMapProjectByIdExecutor
{

    private IProjectDAO projectDAO;

    @Autowired
    private IProjectAuthorizationExecutor authorizationExecutor;

    @SuppressWarnings("unused")
    private MapProjectByIdExecutor()
    {
    }

    public MapProjectByIdExecutor(IProjectDAO projectDAO, IProjectAuthorizationExecutor authorizationExecutor)
    {
        this.projectDAO = projectDAO;
        this.authorizationExecutor = authorizationExecutor;
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canGet(context);
    }

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends IProjectId, ProjectPE>> listers)
    {
        listers.add(new ListProjectByIdentifier(projectDAO));
        listers.add(new ListProjectByPermId(projectDAO));
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        projectDAO = daoFactory.getProjectDAO();
    }

}
