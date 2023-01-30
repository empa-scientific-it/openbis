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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CommonUtils
{

    public static Set<Long> asSet(Collection<Long> ids)
    {
        return ids instanceof Set ? (Set<Long>) ids : new HashSet<>(ids);
    }

    public static List<PersonPE> listPersons(IDAOFactory daoFactory, Collection<Long> ids)
    {
        Set<Long> idSet = asSet(ids);
        List<PersonPE> result = new ArrayList<>();
        List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        for (PersonPE person : persons)
        {
            if (idSet.contains(person.getId()))
            {
                result.add(person);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public
    static Map<String, String> asMap(String json)
    {
        if (json == null)
        {
            return new HashMap<>();
        }
        try
        {
            return new ObjectMapper().readValue(json.getBytes("UTF-8"), HashMap.class);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

}
