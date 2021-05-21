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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.space;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpaceTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

import java.util.LinkedList;
import java.util.List;

/**
 * @author pkupczyk
 */
public class ListSpaceByTechId extends AbstractListObjectById<SpaceTechId, SpacePE>
{

    private ISpaceDAO spaceDAO;

    public ListSpaceByTechId(ISpaceDAO spaceDAO)
    {
        this.spaceDAO = spaceDAO;
    }

    @Override
    public Class<SpaceTechId> getIdClass()
    {
        return SpaceTechId.class;
    }

    @Override
    public SpaceTechId createId(SpacePE space)
    {
        return new SpaceTechId(space.getId());
    }

    @Override
    public List<SpacePE> listByIds(IOperationContext context, List<SpaceTechId> ids)
    {
        List<Long> techIds = new LinkedList<Long>();

        for (SpaceTechId id : ids)
        {
            techIds.add(id.getTechId());
        }

        return spaceDAO.listByIDs(techIds);
    }

}
