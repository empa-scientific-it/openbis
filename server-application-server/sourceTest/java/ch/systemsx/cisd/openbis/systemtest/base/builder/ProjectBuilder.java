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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class ProjectBuilder extends Builder<Project>
{
    private static int number;

    private Space space;

    private String code;

    public ProjectBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
        this.code = "P" + number++;
    }

    @SuppressWarnings("hiding")
    public ProjectBuilder inSpace(Space space)
    {
        this.space = space;
        return this;
    }

    @SuppressWarnings("hiding")
    public ProjectBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public Project create()
    {
        ProjectIdentifier projectId =
                new ProjectIdentifier(space.getCode(), this.code);
        commonServer.registerProject(sessionToken, projectId, "description", "system",
                new ArrayList<NewAttachment>());

        return getProject(this.code);
    }

    private Project getProject(String projectCode)
    {
        for (Project project : commonServer.listProjects(sessionToken))
        {
            if (project.getCode().equalsIgnoreCase(projectCode))
            {
                return project;
            }
        }
        throw new IllegalArgumentException("Project " + projectCode + " does not exist");
    }

}
