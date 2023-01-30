/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * <i>Data Access Object</i> for {@link ProjectPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IProjectDAO extends IGenericDAO<ProjectPE>
{

    /**
     * Lists all projects.
     */
    public List<ProjectPE> listProjects();

    /**
     * Lists projects from given space.
     */
    public List<ProjectPE> listProjects(SpacePE space);

    /**
     * Lists projects by ids.
     */
    public List<ProjectPE> listByIDs(Collection<Long> ids);

    /**
     * Lists projects by perm ids.
     */
    public List<ProjectPE> listByPermID(Collection<String> permIds);

    /**
     * Returns project for given perm id or null if such a project does not exist.
     */
    public ProjectPE tryGetByPermID(String permId);

    /**
     * Returns project for given database instance code, space code and project code or null if such a project does not exist.
     */
    public ProjectPE tryFindProject(String spaceCode, String projectCode);

    /**
     * Returns projects for the given project identifiers or an empty list if none of the projects exists.
     */
    public List<ProjectPE> tryFindProjects(List<ProjectIdentifier> projectIdentifiers);

    /**
     * Creates a new project.
     */
    public void createProject(ProjectPE project, final PersonPE modifier);

}
