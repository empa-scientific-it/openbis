/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class SpaceAuthorizationValidator implements ISpaceAuthorizationValidator
{

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> spaceIds)
    {
        SpaceQuery query = QueryTool.getManagedQuery(SpaceQuery.class);
        List<SpaceAuthorizationRecord> records = query.getAuthorizations(new LongOpenHashSet(spaceIds));
        SimpleSpaceValidator validator = new SimpleSpaceValidator();
        Set<Long> result = new HashSet<Long>();

        for (SpaceAuthorizationRecord record : records)
        {
            final SpaceAuthorizationRecord theRecord = record;

            if (validator.doValidation(person, new ICodeHolder()
                {
                    @Override
                    public String getCode()
                    {
                        return theRecord.code;
                    }
                }))
            {
                result.add(record.id);
            }
        }

        return result;
    }

}