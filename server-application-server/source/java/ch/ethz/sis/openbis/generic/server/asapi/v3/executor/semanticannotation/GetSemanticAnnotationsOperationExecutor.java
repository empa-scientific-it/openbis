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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.get.GetSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.get.GetSemanticAnnotationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation.ISemanticAnnotationTranslator;

/**
 * @author pkupczyk
 */
@Component
public class GetSemanticAnnotationsOperationExecutor
        extends GetObjectsOperationExecutor<ISemanticAnnotationId, SemanticAnnotation, SemanticAnnotationFetchOptions>
        implements IGetSemanticAnnotationsOperationExecutor
{

    @Autowired
    private IMapSemanticAnnotationTechIdByIdExecutor mapExecutor;

    @Autowired
    private ISemanticAnnotationTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<ISemanticAnnotationId, SemanticAnnotationFetchOptions>> getOperationClass()
    {
        return GetSemanticAnnotationsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<ISemanticAnnotationId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, SemanticAnnotation, SemanticAnnotationFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<ISemanticAnnotationId, SemanticAnnotation> getOperationResult(
            Map<ISemanticAnnotationId, SemanticAnnotation> objectMap)
    {
        return new GetSemanticAnnotationsOperationResult(objectMap);
    }

}
