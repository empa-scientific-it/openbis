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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link AuthorizationGroup} &lt;---&gt; {@link AuthorizationGroupPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class AuthorizationGroupTranslator
{
    private AuthorizationGroupTranslator()
    {
    }

    public final static List<AuthorizationGroup> translate(final List<AuthorizationGroupPE> groups)
    {
        final List<AuthorizationGroup> result = new ArrayList<AuthorizationGroup>();
        for (final AuthorizationGroupPE group : groups)
        {
            result.add(AuthorizationGroupTranslator.translate(group));
        }
        return result;
    }

    public static AuthorizationGroup translate(final AuthorizationGroupPE group)
    {
        if (group == null)
        {
            return null;
        }
        final AuthorizationGroup result = new AuthorizationGroup();
        result.setId(HibernateUtils.getId(group));
        result.setCode(group.getCode());
        result.setDescription(group.getDescription());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate());
        result.setRegistrationDate(group.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(group.getRegistrator()));
        result.setModificationDate(group.getModificationDate());
        return result;
    }

}
