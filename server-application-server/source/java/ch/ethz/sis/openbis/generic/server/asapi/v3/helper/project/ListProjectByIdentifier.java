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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.project;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author pkupczyk
 */
public class ListProjectByIdentifier extends AbstractListObjectById<ProjectIdentifier, ProjectPE>
{

    private IProjectDAO projectDAO;

    public ListProjectByIdentifier(IProjectDAO projectDAO)
    {
        this.projectDAO = projectDAO;
    }

    @Override
    public Class<ProjectIdentifier> getIdClass()
    {
        return ProjectIdentifier.class;
    }

    @Override
    public ProjectIdentifier createId(ProjectPE project)
    {
        return new ProjectIdentifier(project.getIdentifier());
    }

    @Override
    public List<ProjectPE> listByIds(IOperationContext context, List<ProjectIdentifier> ids)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier> identifiers =
                new LinkedList<ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier>();

        for (ProjectIdentifier id : ids)
        {
            identifiers.add(ProjectIdentifierFactory.parse(id.getIdentifier()));
        }

        return projectDAO.tryFindProjects(identifiers);
    }

}
