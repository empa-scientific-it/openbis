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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.deletion;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;

/**
 * @author pkupczyk
 */
public class ListDeletionByTechId extends AbstractListObjectById<DeletionTechId, DeletionPE>
{

    private IDeletionDAO deletionDAO;

    public ListDeletionByTechId(IDeletionDAO deletionDAO)
    {
        this.deletionDAO = deletionDAO;
    }

    @Override
    public Class<DeletionTechId> getIdClass()
    {
        return DeletionTechId.class;
    }

    @Override
    public DeletionTechId createId(DeletionPE deletion)
    {
        return new DeletionTechId(deletion.getId());
    }

    @Override
    public List<DeletionPE> listByIds(IOperationContext context, List<DeletionTechId> ids)
    {
        List<Long> techIds = new LinkedList<Long>();

        for (DeletionTechId id : ids)
        {
            techIds.add(id.getTechId());
        }

        return deletionDAO.findAllById(techIds);
    }

}
