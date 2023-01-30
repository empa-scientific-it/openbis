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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.semanticannotation.ListSemanticAnnotationTechIdByPermId;

/**
 * @author pkupczyk
 */
@Component
public class MapSemanticAnnotationTechIdByIdExecutor extends AbstractMapObjectByIdExecutor<ISemanticAnnotationId, Long>
        implements IMapSemanticAnnotationTechIdByIdExecutor
{

    @Autowired
    private ISemanticAnnotationAuthorizationExecutor authorizationExecutor;

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canGet(context);
    }

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends ISemanticAnnotationId, Long>> listers)
    {
        listers.add(new ListSemanticAnnotationTechIdByPermId());
    }

}
