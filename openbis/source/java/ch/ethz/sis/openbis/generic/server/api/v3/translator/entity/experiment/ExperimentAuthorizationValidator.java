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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentAuthorizationValidator implements IExperimentAuthorizationValidator
{

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> experimentIds)
    {
        ExperimentQuery query = QueryTool.getManagedQuery(ExperimentQuery.class);
        List<ExperimentAuthorizationRecord> records = query.getAuthorizations(new LongOpenHashSet(experimentIds));
        ExperimentByIdentiferValidator validator = new ExperimentByIdentiferValidator();
        Set<Long> result = new HashSet<Long>();

        for (ExperimentAuthorizationRecord record : records)
        {
            final ExperimentAuthorizationRecord theRecord = record;

            if (validator.doValidation(person, new IIdentifierHolder()
                {
                    @Override
                    public String getIdentifier()
                    {
                        return new ExperimentIdentifier(theRecord.spaceCode, theRecord.projectCode, theRecord.code).getIdentifier();
                    }
                }))
            {
                result.add(record.id);
            }
        }

        return result;
    }

}
