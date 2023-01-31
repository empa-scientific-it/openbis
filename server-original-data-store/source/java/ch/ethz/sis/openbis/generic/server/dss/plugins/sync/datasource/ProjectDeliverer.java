/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;

/**
 * @author Franz-Josef Elmer
 *
 */
public class ProjectDeliverer extends AbstractEntityWithPermIdDeliverer
{

    ProjectDeliverer(DeliveryContext context)
    {
        super(context, "project", "projects");
    }

    @Override
    protected void deliverEntities(DeliveryExecutionContext context, List<String> projectPermIds) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        String sessionToken = context.getSessionToken();
        Set<String> spaces = context.getSpaces();
        IApplicationServerApi v3api = getV3Api();
        List<ProjectPermId> permIds = projectPermIds.stream().map(ProjectPermId::new).collect(Collectors.toList());
        Collection<Project> fullProjects = v3api.getProjects(sessionToken, permIds, createFullFetchOptions()).values();
        int count = 0;
        for (Project project : fullProjects)
        {
            if (spaces.contains(project.getSpace().getCode()))
            {
                String permId = project.getPermId().getPermId();
                startUrlElement(writer, "PROJECT", permId, project.getModificationDate());
                startXdElement(writer);
                writer.writeAttribute("code", project.getCode());
                addAttributeAndExtractFilePaths(context, writer, "desc", project.getDescription());
                addAttributeIfSet(writer, "frozen", project.isFrozen());
                addAttributeIfSet(writer, "frozenForExperiments", project.isFrozenForExperiments());
                addAttributeIfSet(writer, "frozenForSamples", project.isFrozenForSamples());
                addKind(writer, "PROJECT");
                addModifier(writer, project);
                addRegistrationDate(writer, project);
                addRegistrator(writer, project);
                addSpace(writer, project.getSpace());
                addAttachments(writer, project.getAttachments());
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
            }
        }
        operationLog.info(count + " of " + projectPermIds.size() + " projects have been delivered.");
    }

    private ProjectFetchOptions createFullFetchOptions()
    {
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withAttachments();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        return fetchOptions;
    }

}
