/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.semanticannotation;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISemanticAnnotationDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
public class ListSemanticAnnotationByPermId extends AbstractListObjectById<SemanticAnnotationPermId, SemanticAnnotationPE>
{

    private ISemanticAnnotationDAO semanticAnnotationDAO;

    public ListSemanticAnnotationByPermId(ISemanticAnnotationDAO semanticAnnotationDAO)
    {
        this.semanticAnnotationDAO = semanticAnnotationDAO;
    }

    @Override
    public Class<SemanticAnnotationPermId> getIdClass()
    {
        return SemanticAnnotationPermId.class;
    }

    @Override
    public SemanticAnnotationPermId createId(SemanticAnnotationPE semanticAnnotation)
    {
        return new SemanticAnnotationPermId(semanticAnnotation.getPermId());
    }

    @Override
    public List<SemanticAnnotationPE> listByIds(IOperationContext context, List<SemanticAnnotationPermId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (SemanticAnnotationPermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return semanticAnnotationDAO.findByPermIds(permIds);
    }

}
