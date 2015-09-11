/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.sql;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.IProjectTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class ProjectSqlTranslator implements IProjectSqlTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IProjectTranslator projectTranslator;

    @Override
    public Project translate(TranslationContext context, Long object, ProjectFetchOptions fetchOptions)
    {
        return translate(context, Collections.singleton(object), fetchOptions).get(object);
    }

    @Override
    public Map<Long, Project> translate(TranslationContext context, Collection<Long> objects, ProjectFetchOptions fetchOptions)
    {
        IProjectDAO projectDao = daoFactory.getProjectDAO();
        List<ProjectPE> projects = projectDao.listByIDs(objects);

        Map<ProjectPE, Project> peMap = projectTranslator.translate(context, projects, fetchOptions);
        Map<Long, Project> idMap = new LinkedHashMap<Long, Project>();

        for (Map.Entry<ProjectPE, Project> entry : peMap.entrySet())
        {
            idMap.put(entry.getKey().getId(), entry.getValue());
        }

        return idMap;
    }

}
