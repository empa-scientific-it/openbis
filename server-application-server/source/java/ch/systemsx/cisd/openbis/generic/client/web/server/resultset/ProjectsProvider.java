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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs.SPACE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Table model provider of {@link Space} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class ProjectsProvider extends AbstractCommonTableModelProvider<Project>
{
    public ProjectsProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<Project> createTableModel()
    {
        List<Project> projects = commonServer.listProjects(sessionToken);
        TypedTableModelBuilder<Project> builder = new TypedTableModelBuilder<Project>();
        builder.addColumn(CODE);
        builder.addColumn(SPACE);
        builder.addColumn(DESCRIPTION).withDefaultWidth(200);
        builder.addColumn(REGISTRATOR).withDefaultWidth(200);
        builder.addColumn(MODIFIER).withDefaultWidth(200).hideByDefault();
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        for (Project project : projects)
        {
            builder.addRow(project);
            builder.column(CODE).addString(project.getCode());
            builder.column(SPACE).addString(project.getSpace().getCode());
            builder.column(DESCRIPTION).addString(project.getDescription());
            builder.column(REGISTRATOR).addPerson(project.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(project.getRegistrationDate());
            builder.column(MODIFIER).addPerson(project.getModifier());
            builder.column(MODIFICATION_DATE).addDate(project.getModificationDate());
        }
        return builder.getModel();
    }

}
