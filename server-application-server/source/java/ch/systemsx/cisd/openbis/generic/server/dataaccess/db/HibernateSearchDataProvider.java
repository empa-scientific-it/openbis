/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.SpaceTranslator;

/**
 * Encapsulates data provided to {@link HibernateSearchDAO} by other DAOs.
 * 
 * @author Piotr Buczek
 */
public class HibernateSearchDataProvider
{
    private Map<String, Space> groupsById;

    public HibernateSearchDataProvider(IDAOFactory factory)
    {
        List<SpacePE> groups = factory.getSpaceDAO().listSpaces();
        groupsById = new HashMap<String, Space>();
        for (SpacePE group : groups)
        {
            groupsById.put(group.getId().toString(), SpaceTranslator.translate(group));
        }
    }

    public Map<String, Space> getGroupsById()
    {
        return groupsById;
    }
}
