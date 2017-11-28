/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class MapPersonIdByIdExecutor implements IMapPersonIdByIdExecutor
{
    @Autowired
    private IMapPersonByIdExecutor executor;

    @Override
    public Map<IPersonId, Long> map(IOperationContext context, Collection<? extends IPersonId> ids)
    {
        Map<IPersonId, PersonPE> map = executor.map(context, ids);
        return translate(map);
    }

    @Override
    public Map<IPersonId, Long> map(IOperationContext context, Collection<? extends IPersonId> ids, boolean checkAccess)
    {
        Map<IPersonId, PersonPE> map = executor.map(context, ids, checkAccess);
        return translate(map);
    }
    
    private Map<IPersonId, Long> translate(Map<IPersonId, PersonPE> map)
    {
        Map<IPersonId, Long> result = new HashMap<>();
        for (Entry<IPersonId, PersonPE> entry : map.entrySet())
        {
            PersonPE person = entry.getValue();
            result.put(entry.getKey(), person == null ? null : person.getId());
        }
        return result;
    }

}
