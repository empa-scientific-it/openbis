/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Space} &lt;---&gt; {@link SpacePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class SpaceTranslator
{
    private SpaceTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Space> translate(final List<SpacePE> spaces)
    {
        final List<Space> result = new ArrayList<Space>();
        for (final SpacePE space : spaces)
        {
            result.add(SpaceTranslator.translate(space));
        }
        return result;
    }

    public static Space translate(final SpacePE space)
    {
        if (space == null)
        {
            return null;
        }
        final Space result = new Space();
        result.setId(HibernateUtils.getId(space));
        result.setCode(space.getCode());
        result.setDescription(space.getDescription());
        result.setRegistrationDate(space.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(space.getRegistrator()));
        result.setModificationDate(space.getModificationDate());
        result.setIdentifier(IdentifierHelper.createGroupIdentifier(space).toString());
        return result;
    }
}
