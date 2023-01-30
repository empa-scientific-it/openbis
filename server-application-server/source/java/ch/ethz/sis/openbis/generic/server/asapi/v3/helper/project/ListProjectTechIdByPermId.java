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

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListTechIdByPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.TechIdStringIdentifierRecord;

import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
public class ListProjectTechIdByPermId extends AbstractListTechIdByPermId<ProjectPermId>
{

    @Override
    public Class<ProjectPermId> getIdClass()
    {
        return ProjectPermId.class;
    }

    @Override
    protected List<TechIdStringIdentifierRecord> queryTechIds(String[] permIds)
    {
        ProjectQuery query = QueryTool.getManagedQuery(ProjectQuery.class);
        return query.listProjectTechIdsByPermIds(permIds);
    }

    @Override
    protected ProjectPermId createPermId(String permIdAsString)
    {
        return new ProjectPermId(permIdAsString);
    }

}
