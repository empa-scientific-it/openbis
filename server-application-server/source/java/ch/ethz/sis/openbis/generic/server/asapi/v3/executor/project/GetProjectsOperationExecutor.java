/*
 * Copyright 2016 ETH Zuerich, CISD
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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.get.GetProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.get.GetProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project.IProjectTranslator;

/**
 * @author pkupczyk
 */
@Component
public class GetProjectsOperationExecutor extends GetObjectsOperationExecutor<IProjectId, Project, ProjectFetchOptions>
        implements IGetProjectsOperationExecutor
{

    @Autowired
    private IMapProjectTechIdByIdExecutor mapExecutor;

    @Autowired
    private IProjectTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IProjectId, ProjectFetchOptions>> getOperationClass()
    {
        return GetProjectsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IProjectId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Project, ProjectFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IProjectId, Project> getOperationResult(Map<IProjectId, Project> objectMap)
    {
        return new GetProjectsOperationResult(objectMap);
    }

}
