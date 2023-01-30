/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListTechIdById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.TechIdStringIdentifierRecord;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
public class ListProjectTechIdByIdentifier extends AbstractListTechIdById<ProjectIdentifier>
{

    @Override
    public Class<ProjectIdentifier> getIdClass()
    {
        return ProjectIdentifier.class;
    }

    @Override
    protected Map<Long, ProjectIdentifier> createIdsByTechIdsMap(IOperationContext context, List<ProjectIdentifier> ids)
    {
        Map<String, Map<String, ProjectIdentifier>> groupedIdentifiers = new HashMap<>();
        for (ProjectIdentifier projectIdentifier : ids)
        {
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier projectIdentifier2 =
                    ProjectIdentifierFactory.parse(projectIdentifier.getIdentifier());
            String spaceCode = CodeConverter.tryToDatabase(projectIdentifier2.getSpaceCode());
            String projectCode = CodeConverter.tryToDatabase(projectIdentifier2.getProjectCode());
            Map<String, ProjectIdentifier> identifiersByCode = groupedIdentifiers.get(spaceCode);
            if (identifiersByCode == null)
            {
                identifiersByCode = new HashMap<>();
                groupedIdentifiers.put(spaceCode, identifiersByCode);
            }
            identifiersByCode.put(projectCode, projectIdentifier);
        }
        Map<Long, ProjectIdentifier> result = new HashMap<>();
        ProjectQuery query = QueryTool.getManagedQuery(ProjectQuery.class);
        for (Entry<String, Map<String, ProjectIdentifier>> entry : groupedIdentifiers.entrySet())
        {
            String spaceCode = entry.getKey();
            Map<String, ProjectIdentifier> identifiersByCode = entry.getValue();
            Set<String> codes = identifiersByCode.keySet();
            String[] codesAsArray = codes.toArray(new String[codes.size()]);
            for (TechIdStringIdentifierRecord record : query.listProjectTechIdsByCodes(spaceCode, codesAsArray))
            {
                result.put(record.id, identifiersByCode.get(record.identifier));
            }
        }
        return result;
    }

}
