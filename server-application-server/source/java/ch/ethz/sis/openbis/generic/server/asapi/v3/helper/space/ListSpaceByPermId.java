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

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ListSpaceByPermId extends AbstractListObjectById<SpacePermId, SpacePE>
{

    private ISpaceDAO spaceDAO;

    public ListSpaceByPermId(ISpaceDAO spaceDAO)
    {
        this.spaceDAO = spaceDAO;
    }

    @Override
    public Class<SpacePermId> getIdClass()
    {
        return SpacePermId.class;
    }

    @Override
    public SpacePermId createId(SpacePE space)
    {
        return new SpacePermId(space.getCode());
    }

    @Override
    public List<SpacePE> listByIds(IOperationContext context, List<SpacePermId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (SpacePermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return spaceDAO.tryFindSpaceByCodes(permIds);
    }

}
