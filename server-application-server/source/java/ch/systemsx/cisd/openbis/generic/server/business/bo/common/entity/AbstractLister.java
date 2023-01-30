/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Franz-Josef Elmer
 */
public class AbstractLister
{
    private final SecondaryEntityDAO referencedEntityDAO;

    private final Long2ObjectMap<Person> persons = new Long2ObjectOpenHashMap<Person>();

    protected AbstractLister(SecondaryEntityDAO referencedEntityDAO)
    {
        this.referencedEntityDAO = referencedEntityDAO;
    }

    protected Person getOrCreateActor(Long personId)
    {
        if (personId == null)
        {
            return null;
        }
        Person registrator = persons.get(personId);
        if (registrator == null)
        {
            registrator = referencedEntityDAO.getPerson(personId);
            persons.put(personId, registrator);
        }
        return registrator;
    }
}
